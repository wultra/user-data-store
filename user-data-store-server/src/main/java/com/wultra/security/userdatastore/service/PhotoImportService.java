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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    private PhotoImportResultDto importPhoto(final PhotoImportDto photo) {
        final FetchResult result = switch(photo.photoDataType()) {
            case "raw" -> {
                final FetchResult rawResult = fetchPhoto(photo.photoData());
                if (rawResult.error != null) {
                    yield rawResult;
                }
                yield new FetchResult(photo.photoData(), Base64.getEncoder().encode(rawResult.photo), null);
            }
            case "base64" -> fetchPhoto(photo.photoData());
            case "base64_inline" -> new FetchResult(null, photo.photoData().getBytes(StandardCharsets.UTF_8), null);
            default -> throw new InvalidRequestException();
        };

        if (result.error != null) {
            persistImportResult(handleError(photo.userId(), photo.photoType(), result.error));
        }

        return createNewPhoto(photo.userId(), photo.photoType(), result.importPath, result.photo);
    }

    private PhotoImportResultDto createNewPhoto(final String userId, final String photoType, final String importPath, final byte[] photoBase64) {
        final EmbeddedPhotoCreateRequest photoCreateRequest = EmbeddedPhotoCreateRequest.builder()
                .photoType(photoType)
                .photoData(new String(photoBase64, StandardCharsets.UTF_8))
                .build();
        final DocumentCreateRequest documentCreateRequest = DocumentCreateRequest.builder()
                .userId(userId)
                .documentType("photo")
                .dataType("image_base64")
                .documentData("{}")
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


    private FetchResult fetchPhoto(final String photoData) {
        if (photoData.startsWith("http")) {
            final HttpClient client = HttpClient.newHttpClient();
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(photoData))
                    .build();
            try {
                final HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() == 200) {
                    return new FetchResult(photoData, response.body(), null);
                }
                return new FetchResult(photoData, null, "HTTP status code: " + response.statusCode());
            } catch (IOException | InterruptedException e) {
                logger.info("Error occurred while downloading file: {}", e.getMessage());
                logger.debug(e.getMessage(), e);
                return new FetchResult(photoData, null, "IO error: " + e.getMessage());
            }
        }
        try {
            final byte[] photoBytes = Files.readAllBytes(Paths.get(photoData));
            return new FetchResult(photoData, photoBytes, null);
        } catch (IOException e) {
            logger.info("Error occurred while reading file: {}", e.getMessage());
            logger.debug(e.getMessage(), e);
            return new FetchResult(photoData, null, "IO error: " + e.getMessage());

        }
    }

    private record FetchResult (
            String importPath,
            byte[] photo,
            String error
    ) {}
}
