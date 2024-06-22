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
import com.wultra.security.userdatastore.client.model.dto.AttachmentDto;
import com.wultra.security.userdatastore.client.model.request.AttachmentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.EmbeddedAttachmentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.AttachmentUpdateRequest;
import com.wultra.security.userdatastore.client.model.response.AttachmentCreateResponse;
import com.wultra.security.userdatastore.client.model.response.AttachmentResponse;
import com.wultra.security.userdatastore.converter.AttachmentConverter;
import com.wultra.security.userdatastore.model.entity.AttachmentEntity;
import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import com.wultra.security.userdatastore.model.error.ResourceNotFoundException;
import com.wultra.security.userdatastore.model.repository.AttachmentRepository;
import com.wultra.security.userdatastore.model.repository.DocumentRepository;
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
 * Service for document attachments.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
@Slf4j
@AllArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final DocumentRepository documentRepository;
    private final Audit audit;
    private final EncryptionService encryptionService;
    private final AttachmentConverter attachmentConverter;

    @Transactional
    public AttachmentCreateResponse createAttachment(final AttachmentCreateRequest request) {
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
        final LocalDateTime timestamp = LocalDateTime.now();
        final AttachmentEntity attachmentEntity = new AttachmentEntity();
        attachmentEntity.setId(UUID.randomUUID().toString());
        attachmentEntity.setDocument(documentEntity);
        attachmentEntity.setUserId(userId);
        attachmentEntity.setAttachmentType(request.attachmentType());
        attachmentEntity.setExternalId(request.externalId());
        attachmentEntity.setTimestampCreated(timestamp);
        encryptionService.encryptAttachment(attachmentEntity, request.attachmentData());
        documentEntity.setTimestampLastUpdated(timestamp);

        attachmentRepository.save(attachmentEntity);

        return new AttachmentCreateResponse(attachmentEntity.getId(), documentEntity.getId());
    }

    @Transactional
    public AttachmentCreateResponse createAttachment(final EmbeddedAttachmentCreateRequest request, final DocumentEntity documentEntity) {
        final AttachmentEntity attachmentEntity = new AttachmentEntity();
        attachmentEntity.setId(UUID.randomUUID().toString());
        attachmentEntity.setDocument(documentEntity);
        attachmentEntity.setUserId(documentEntity.getUserId());
        attachmentEntity.setAttachmentType(request.attachmentType());
        attachmentEntity.setExternalId(request.externalId());
        attachmentEntity.setTimestampCreated(LocalDateTime.now());
        encryptionService.encryptAttachment(attachmentEntity, request.attachmentData());

        attachmentRepository.save(attachmentEntity);

        return new AttachmentCreateResponse(attachmentEntity.getId(), documentEntity.getId());
    }

    public void updateAttachment(final String attachmentId, final AttachmentUpdateRequest request) {
        final AttachmentEntity attachmentEntity = attachmentRepository.findById(attachmentId).orElseThrow(() ->
               new ResourceNotFoundException("Attachment not found, ID: '%s'".formatted(attachmentId)));
        final LocalDateTime timestamp = LocalDateTime.now();
        attachmentEntity.setAttachmentType(request.attachmentType());
        attachmentEntity.setExternalId(request.externalId());
        attachmentEntity.setTimestampLastUpdated(timestamp);
        encryptionService.encryptAttachment(attachmentEntity, request.attachmentData());
        final DocumentEntity documentEntity = attachmentEntity.getDocument();
        documentEntity.setTimestampLastUpdated(timestamp);
        attachmentRepository.save(attachmentEntity);
    }

    @Transactional(readOnly = true)
    public AttachmentResponse fetchAttachments(final String userId, final Optional<String> documentId) {
        if (documentId.isPresent()) {
            final Optional<DocumentEntity> documentEntityOptional = documentRepository.findById(documentId.get());
            if (documentEntityOptional.isEmpty()) {
                return new AttachmentResponse(Collections.emptyList());
            }
            final DocumentEntity documentEntity = documentEntityOptional.get();
            final List<AttachmentEntity> attachmentEntities = attachmentRepository.findAllByUserIdAndDocument(userId, documentEntity);
            attachmentEntities.forEach(encryptionService::decryptAttachment);
            final List<AttachmentDto> attachments = attachmentEntities.stream().map(attachmentConverter::toAttachment).toList();
            audit("Retrieved attachments for document ID: {}", documentId.get());
            return new AttachmentResponse(attachments);
        }
        final List<AttachmentEntity> attachmentEntities = attachmentRepository.findAllByUserId(userId);
        attachmentEntities.forEach(encryptionService::decryptAttachment);
        final List<AttachmentDto> attachments = attachmentEntities.stream().map(attachmentConverter::toAttachment).toList();
        audit("Retrieved attachments for user ID: {}", userId);
        return new AttachmentResponse(attachments);
    }

    @Transactional
    public void deleteAttachments(final String userId, final Optional<String> documentId) {
        if (documentId.isPresent()) {
            final Optional<DocumentEntity> documentEntityOptional = documentRepository.findById(documentId.get());
            if (documentEntityOptional.isEmpty()) {
                return;
            }
            attachmentRepository.deleteAllByUserIdAndDocument(userId, documentEntityOptional.get());
            audit("Deleted attachments for document ID: {}", documentId.get());
            return;
        }
        attachmentRepository.deleteAllByUserId(userId);
        audit("Deleted attachments for user ID: {}", userId);
    }

    private void audit(final String message, final String userId) {
        final String loggedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        final AuditDetail auditDetail = AuditDetail.builder()
                .type("attachment")
                .param("userId", userId)
                .param("actorId", loggedUsername)
                .build();
        audit.info(message, auditDetail, userId);
    }

}
