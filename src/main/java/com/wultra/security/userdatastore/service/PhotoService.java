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
import com.wultra.security.userdatastore.converter.PhotoConverter;
import com.wultra.security.userdatastore.model.dto.DocumentDto;
import com.wultra.security.userdatastore.model.dto.PhotoDto;
import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import com.wultra.security.userdatastore.model.entity.PhotoEntity;
import com.wultra.security.userdatastore.model.error.ResourceNotFoundException;
import com.wultra.security.userdatastore.model.repository.DocumentRepository;
import com.wultra.security.userdatastore.model.repository.PhotoRepository;
import com.wultra.security.userdatastore.model.request.PhotoCreateRequest;
import com.wultra.security.userdatastore.model.response.DocumentResponse;
import com.wultra.security.userdatastore.model.response.PhotoCreateResponse;
import com.wultra.security.userdatastore.model.response.PhotoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final DocumentRepository documentRepository;
    private final Audit audit;
    private final EncryptionService encryptionService;
    private final PhotoConverter photoConverter;

    @Autowired
    PhotoService(final PhotoRepository photoRepository, final DocumentRepository documentRepository, final Audit audit, final EncryptionService encryptionService, final PhotoConverter photoConverter) {
        this.photoRepository = photoRepository;
        this.documentRepository = documentRepository;
        this.audit = audit;
        this.encryptionService = encryptionService;
        this.photoConverter = photoConverter;
    }

    @Transactional(readOnly = true)
    public PhotoResponse fetchPhotos(final String userId, final String documentId) {
        if (documentId != null) {
            final Optional<DocumentEntity> documentEntityOptional = documentRepository.findById(documentId);
            if (documentEntityOptional.isEmpty()) {
                return new PhotoResponse();
            }
            final DocumentEntity documentEntity = documentEntityOptional.get();
            final List<PhotoEntity> photoEntities = photoRepository.findAllByUserIdAndDocument(userId, documentEntity);
            photoEntities.forEach(encryptionService::decryptPhoto);
            final List<PhotoDto> photos = photoEntities.stream().map(photoConverter::toPhoto).toList();
            audit("Retrieved photos for document ID: {}", documentId);
            final PhotoResponse response = new PhotoResponse();
            response.addAll(photos);
            return response;
        }
        final List<PhotoEntity> photoEntities = photoRepository.findAllByUserId(userId);
        photoEntities.forEach(encryptionService::decryptPhoto);
        final List<PhotoDto> photos = photoEntities.stream().map(photoConverter::toPhoto).toList();
        audit("Retrieved photos for user ID: {}", userId);
        final PhotoResponse response = new PhotoResponse();
        response.addAll(photos);
        return response;

    }

    @Transactional
    public PhotoCreateResponse createPhoto(final String userId, final String documentId, final PhotoCreateRequest request) {
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
        photoEntity.setPhotoType(request.photoType());
        photoEntity.setPhotoData(request.photoData());
        photoEntity.setTimestampCreated(LocalDateTime.now());

        photoRepository.save(photoEntity);

        final PhotoCreateResponse response = new PhotoCreateResponse();
        response.setId(photoEntity.getId());
        response.setDocumentId(documentEntity.getId());
        return response;
    }

    @Transactional
    public void deletePhotos(final String userId, final String documentId) {
        if (documentId == null) {
            photoRepository.deleteAllByUserId(userId);
            audit("Deleted photos for user ID: {}", userId);
            return;
        }
        final Optional<DocumentEntity> documentEntityOptional = documentRepository.findById(documentId);
        if (documentEntityOptional.isEmpty()) {
            return;
        }

        photoRepository.deleteAllByUserIdAndDocument(userId, documentEntityOptional.get());
        audit("Deleted photos for document ID: {}", documentId);
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
