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

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import com.wultra.security.userdatastore.client.model.dto.PhotoDto;
import com.wultra.security.userdatastore.client.model.dto.PhotoImportDto;
import com.wultra.security.userdatastore.client.model.dto.PhotoImportResultDto;
import com.wultra.security.userdatastore.client.model.request.*;
import com.wultra.security.userdatastore.client.model.response.EmbeddedPhotoImportResponse;
import com.wultra.security.userdatastore.client.model.response.PhotoCreateResponse;
import com.wultra.security.userdatastore.client.model.response.PhotoResponse;
import com.wultra.security.userdatastore.client.model.response.PhotosImportResponse;
import com.wultra.security.userdatastore.converter.PhotoConverter;
import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import com.wultra.security.userdatastore.model.entity.PhotoEntity;
import com.wultra.security.userdatastore.model.error.ResourceNotFoundException;
import com.wultra.security.userdatastore.model.repository.DocumentRepository;
import com.wultra.security.userdatastore.model.repository.PhotoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for photos.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
@Slf4j
@AllArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final DocumentRepository documentRepository;
    private final Audit audit;
    private final EncryptionService encryptionService;
    private final PhotoConverter photoConverter;
    private final PhotoImportService photoImportService;

    @Transactional(readOnly = true)
    public PhotoResponse fetchPhotos(final String userId, final Optional<String> documentId) {
        if (documentId.isPresent()) {
            final DocumentEntity documentEntity = documentRepository.findById(documentId.get()).orElseThrow(
                    () -> new ResourceNotFoundException("Document not found, ID: '%s'".formatted(documentId)));
            final List<PhotoEntity> photoEntities = photoRepository.findAllByUserIdAndDocument(userId, documentEntity);
            final List<PhotoDto> photos = photoEntities.stream().map(photoConverter::toPhoto).toList();
            audit("action: fetchPhotos, userId: {}, documentId: {}", userId, documentId.get());
            return new PhotoResponse(photos);
        }
        final List<PhotoEntity> photoEntities = photoRepository.findAllByUserId(userId);
        final List<PhotoDto> photos = photoEntities.stream().map(photoConverter::toPhoto).toList();
        audit("action: fetchPhotos, userId: {}", userId, null);
        return new PhotoResponse(photos);
    }

    @Transactional
    public PhotoCreateResponse createPhoto(final PhotoCreateRequest request) {
        final String userId = request.userId();
        final String documentId = request.documentId();
        final DocumentEntity documentEntity = documentRepository.findById(documentId).orElseThrow(
                () -> new ResourceNotFoundException("Document not found, ID: '%s'".formatted(documentId)));
        if (!documentEntity.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("User reference not valid, ID: '%s'".formatted(userId));
        }
        final LocalDateTime timestamp = LocalDateTime.now();
        final PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setId(UUID.randomUUID().toString());
        photoEntity.setDocument(documentEntity);
        photoEntity.setUserId(userId);
        photoEntity.setPhotoType(request.photoType());
        photoEntity.setExternalId(request.externalId());
        photoEntity.setTimestampCreated(timestamp);
        encryptionService.encryptPhoto(photoEntity, request.photoData());
        documentEntity.setTimestampLastUpdated(timestamp);

        photoRepository.save(photoEntity);
        audit("action: createPhoto, userId: {}, documentId: {}", userId, documentId);

        return new PhotoCreateResponse(photoEntity.getId(), documentEntity.getId());
    }

    @Transactional
    public PhotoCreateResponse createPhoto(final EmbeddedPhotoCreateRequest request, final DocumentEntity documentEntity) {
        final PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setId(UUID.randomUUID().toString());
        photoEntity.setDocument(documentEntity);
        photoEntity.setUserId(documentEntity.getUserId());
        photoEntity.setPhotoType(request.photoType());
        photoEntity.setExternalId(request.externalId());
        photoEntity.setTimestampCreated(LocalDateTime.now());
        encryptionService.encryptPhoto(photoEntity, request.photoData());

        photoRepository.save(photoEntity);
        audit("action: createPhoto, userId: {}, documentId: {}", photoEntity.getUserId(), documentEntity.getId());

        return new PhotoCreateResponse(photoEntity.getId(), documentEntity.getId());
    }

    @Transactional
    public void updatePhoto(final String photoId, final PhotoUpdateRequest request) {
        final PhotoEntity photoEntity = photoRepository.findById(photoId).orElseThrow(() ->
                new ResourceNotFoundException("Photo not found, ID: '%s'".formatted(photoId)));
        final LocalDateTime timestamp = LocalDateTime.now();
        photoEntity.setPhotoType(request.photoType());
        photoEntity.setExternalId(request.externalId());
        photoEntity.setTimestampLastUpdated(timestamp);
        encryptionService.encryptPhoto(photoEntity, request.photoData());
        final DocumentEntity documentEntity = photoEntity.getDocument();
        documentEntity.setTimestampLastUpdated(timestamp);

        photoRepository.save(photoEntity);
        audit("action: updatePhoto, userId: {}, documentId: {}", photoEntity.getUserId(), documentEntity.getId());
    }

    @Transactional
    public void deletePhotos(final String userId, final Optional<String> documentId) {
        if (documentId.isPresent()) {
            final DocumentEntity documentEntity = documentRepository.findById(documentId.get()).orElseThrow(
                    () -> new ResourceNotFoundException("Document not found, ID: '%s'".formatted(documentId)));
            photoRepository.deleteAllByUserIdAndDocument(userId, documentEntity);
            audit("action: deletePhotos, userId: {}, documentId: {}", userId, documentId.get());
            return;
        }
        photoRepository.deleteAllByUserId(userId);
        audit("action: deletePhotos, userId: {}", userId, null);
    }

    @Transactional
    public PhotosImportResponse importPhotos(PhotosImportRequest requestObject) {
        final List<PhotoImportDto> photos = requestObject.photos().stream()
                .map(photoConverter::toPhotoImport)
                .toList();
        final List<PhotoImportResultDto> result = photoImportService.importPhotos(photos);
        final List<EmbeddedPhotoImportResponse> responsePhotos = result.stream()
                .map(photoConverter::toPhotoImportResponse)
                .toList();
        return PhotosImportResponse.builder()
                .photos(responsePhotos)
                .build();
    }

    @Transactional
    @Async
    public void importPhotosCsv(PhotosImportCsvRequest requestObject) {
        photoImportService.importPhotosCsv(requestObject.importPaths());
    }

    private void audit(final String message, final String userId, final String documentId) {
        final String loggedUsername = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElse(null);
        final AuditDetail auditDetail = AuditDetail.builder()
                .type("photo")
                .param("userId", userId)
                .param("documentId", documentId)
                .param("actorId", loggedUsername)
                .build();
        audit.info(message, auditDetail, userId);
    }

}
