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
import com.wultra.security.userdatastore.client.model.request.EmbeddedPhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.request.PhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.response.PhotoCreateResponse;
import com.wultra.security.userdatastore.client.model.response.PhotoResponse;
import com.wultra.security.userdatastore.converter.PhotoConverter;
import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import com.wultra.security.userdatastore.model.entity.PhotoEntity;
import com.wultra.security.userdatastore.model.error.ResourceNotFoundException;
import com.wultra.security.userdatastore.model.repository.DocumentRepository;
import com.wultra.security.userdatastore.model.repository.PhotoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
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

    @Transactional(readOnly = true)
    public PhotoResponse fetchPhotos(final String userId, final Optional<String> documentId) {
        if (documentId.isPresent()) {
            final Optional<DocumentEntity> documentEntityOptional = documentRepository.findById(documentId.get());
            if (documentEntityOptional.isEmpty()) {
                return new PhotoResponse(Collections.emptyList());
            }
            final DocumentEntity documentEntity = documentEntityOptional.get();
            final List<PhotoEntity> photoEntities = photoRepository.findAllByUserIdAndDocument(userId, documentEntity);
            photoEntities.forEach(encryptionService::decryptPhoto);
            final List<PhotoDto> photos = photoEntities.stream().map(photoConverter::toPhoto).toList();
            audit("Retrieved photos for document ID: {}", documentId.get());
            return new PhotoResponse(photos);
        }
        final List<PhotoEntity> photoEntities = photoRepository.findAllByUserId(userId);
        photoEntities.forEach(encryptionService::decryptPhoto);
        final List<PhotoDto> photos = photoEntities.stream().map(photoConverter::toPhoto).toList();
        audit("Retrieved photos for user ID: {}", userId);
        return new PhotoResponse(photos);
    }

    @Transactional
    public PhotoCreateResponse createPhoto(final PhotoCreateRequest request) {
        final String userId = request.userId();
        final String documentId = request.documentId();
        final Optional<DocumentEntity> documentEntityOptional = documentRepository.findById(documentId);
        if (documentEntityOptional.isEmpty()) {
            throw new ResourceNotFoundException("Document not found, ID: '%s'".formatted(documentId));
        }
        final DocumentEntity documentEntity = documentEntityOptional.get();
        if (!documentEntity.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("User reference not valid, ID: '%s'".formatted(userId));
        }
        final PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setId(UUID.randomUUID().toString());
        photoEntity.setDocument(documentEntity);
        photoEntity.setUserId(userId);
        photoEntity.setPhotoType(request.photoType());
        photoEntity.setExternalId(request.externalId());
        photoEntity.setTimestampCreated(LocalDateTime.now());
        encryptionService.encryptPhoto(photoEntity, request.photoData());

        photoRepository.save(photoEntity);

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

        return new PhotoCreateResponse(photoEntity.getId(), documentEntity.getId());
    }

    @Transactional
    public void deletePhotos(final String userId, final Optional<String> documentId) {
        if (documentId.isPresent()) {
            final Optional<DocumentEntity> documentEntityOptional = documentRepository.findById(documentId.get());
            if (documentEntityOptional.isEmpty()) {
                return;
            }

            photoRepository.deleteAllByUserIdAndDocument(userId, documentEntityOptional.get());
            audit("Deleted photos for document ID: {}", documentId.get());
            return;
        }
        photoRepository.deleteAllByUserId(userId);
        audit("Deleted photos for user ID: {}", userId);
    }


    private void audit(final String message, final String userId) {
        final String loggedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        final AuditDetail auditDetail = AuditDetail.builder()
                .type("photo")
                .param("userId", userId)
                .param("actorId", loggedUsername)
                .build();
        audit.info(message, auditDetail, userId);
    }
}
