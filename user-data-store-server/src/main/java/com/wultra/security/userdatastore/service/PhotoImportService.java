/*
 * User Data Store
 * Copyright (C) 2024 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wultra.security.userdatastore.service;

import com.wultra.security.userdatastore.client.model.dto.PhotoImportDto;
import com.wultra.security.userdatastore.client.model.dto.PhotoImportResultDto;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.EmbeddedPhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.response.DocumentCreateResponse;
import com.wultra.security.userdatastore.model.entity.ImportResultEntity;
import com.wultra.security.userdatastore.model.error.InvalidRequestException;
import com.wultra.security.userdatastore.model.repository.ImportResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for importing photos.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
@Slf4j
public class PhotoImportService {

    private final DocumentService documentService;
    private final ImportResultRepository importResultRepository;

    @Autowired
    public PhotoImportService(@Lazy DocumentService documentService, ImportResultRepository importResultRepository) {
        this.documentService = documentService;
        this.importResultRepository = importResultRepository;
    }

    public List<PhotoImportResultDto> importPhotos(final List<PhotoImportDto> photos) {
        return photos.stream().map(this::importPhoto).toList();
    }

    public void importPhotosCsv(final List<String> csvPaths, final Map<String, Object> attributes) {
        csvPaths.forEach(path -> importCsv(path, attributes));
    }

    private void importCsv(final String csvPath, Map<String, Object> attributes) {
        final FetchResult result = fetchFromPath(csvPath);
        if (result.error == null) {
            final List<List<String>> parsedData = parseCsv(result.data);
            if (parsedData != null) {
                parsedData.forEach(data -> importCsvRow(data, attributes));
            }
        }
    }

    private void importCsvRow(final List<String> csvRow, final Map<String, Object> attributes) {
        if (csvRow.size() != 4) {
            logger.warn("Invalid CSV import format");
            return;
        }
        final PhotoImportDto photo = PhotoImportDto.builder()
                .userId(csvRow.get(0))
                .photoDataType(csvRow.get(1))
                .photoType(csvRow.get(2))
                .photoData(csvRow.get(3))
                .attributes(attributes)
                .build();
        importPhoto(photo);
    }

    private PhotoImportResultDto importPhoto(final PhotoImportDto photo) {
        final FetchResult result = switch(photo.photoDataType()) {
            case "raw" -> {
                final FetchResult rawResult = fetchFromPath(photo.photoData());
                if (rawResult.error != null) {
                    yield rawResult;
                }
                yield new FetchResult(photo.photoData(), Base64.getEncoder().encode(rawResult.data), null);
            }
            case "base64" -> fetchFromPath(photo.photoData());
            case "base64_inline" -> new FetchResult(null, photo.photoData().getBytes(StandardCharsets.UTF_8), null);
            default -> throw new InvalidRequestException();
        };

        if (result.error != null) {
            persistImportResult(handleError(photo.userId(), photo.photoType(), result.error));
        }

        return createNewPhoto(photo.userId(), photo.photoType(), result.importPath, result.data, photo.attributes());
    }

    public static List<List<String>> parseCsv(byte[] csvData) {
        final List<List<String>> parsedData = new ArrayList<>();
        try (
                ByteArrayInputStream is = new ByteArrayInputStream(csvData);
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)
        ) {
            final Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(isr);
            for (CSVRecord record : records) {
                final List<String> row = new ArrayList<>();
                record.forEach(row::add);
                parsedData.add(row);
            }
        } catch (Exception e) {
            logger.warn("CSV parsing failed, error: {}", e.getMessage());
            logger.debug(e.getMessage(), e);
            return null;
        }
        return parsedData;
    }

    private PhotoImportResultDto createNewPhoto(final String userId, final String photoType, final String importPath, final byte[] photoBase64, final Map<String, Object> attributes) {
        final EmbeddedPhotoCreateRequest photoCreateRequest = EmbeddedPhotoCreateRequest.builder()
                .photoType(photoType)
                .photoData(new String(photoBase64, StandardCharsets.UTF_8))
                .build();
        final DocumentCreateRequest documentCreateRequest = DocumentCreateRequest.builder()
                .userId(userId)
                .documentType("data")
                .dataType("image_base64")
                .documentData("{}")
                .attributes(attributes)
                .photos(Collections.singletonList(photoCreateRequest))
                .build();
        final DocumentCreateResponse response = documentService.createDocument(documentCreateRequest);
        final PhotoImportResultDto result = PhotoImportResultDto.builder()
                .userId(userId)
                .photoType(photoType)
                .importPath(importPath)
                .documentId(response.id())
                .photoId(response.photos().get(0).id())
                .imported(true)
                .build();
        persistImportResult(result);
        return result;
    }

    private void persistImportResult(final PhotoImportResultDto result) {
        final ImportResultEntity resultEntity = new ImportResultEntity();
        resultEntity.setId(UUID.randomUUID().toString());
        resultEntity.setUserId(result.userId());
        resultEntity.setImportPath(result.importPath());
        resultEntity.setDocumentId(result.documentId());
        resultEntity.setPhotoId(result.photoId());
        resultEntity.setImported(result.imported());
        resultEntity.setError(result.error());
        resultEntity.setImported(result.imported());
        resultEntity.setTimestampCreated(LocalDateTime.now());
        importResultRepository.save(resultEntity);
    }

    private PhotoImportResultDto handleError(final String userId, final String photoType, final String error) {
        return PhotoImportResultDto.builder()
                .userId(userId)
                .photoType(photoType)
                .imported(false)
                .error(error)
                .build();
    }

    private FetchResult fetchFromPath(final String path) {
        if (path.startsWith("http")) {
            final HttpClient client = HttpClient.newHttpClient();
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(path))
                    .build();
            try {
                final HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() == 200) {
                    return new FetchResult(path, response.body(), null);
                }
                return new FetchResult(path, null, "HTTP status code: " + response.statusCode());
            } catch (IOException | InterruptedException e) {
                logger.info("Error occurred while downloading file: {}", e.getMessage());
                logger.debug(e.getMessage(), e);
                return new FetchResult(path, null, "IO error: " + e.getMessage());
            }
        }
        try {
            final byte[] dataBytes = Files.readAllBytes(Paths.get(path));
            return new FetchResult(path, dataBytes, null);
        } catch (IOException e) {
            logger.info("Error occurred while reading file: {}", e.getMessage());
            logger.debug(e.getMessage(), e);
            return new FetchResult(path, null, "IO error: " + e.getMessage());
        }
    }

    private record FetchResult (
            String importPath,
            byte[] data,
            String error
    ) {}
}
