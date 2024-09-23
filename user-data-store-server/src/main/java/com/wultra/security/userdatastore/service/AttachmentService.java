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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Transactional(readOnly = true)
    public AttachmentResponse fetchAttachments(final String userId, final Optional<String> documentId) {
        if (documentId.isPresent()) {
            final DocumentEntity documentEntity = documentRepository.findById(documentId.get()).orElseThrow(
                    () -> new ResourceNotFoundException("Document not found, ID: '%s'".formatted(documentId)));
            final List<AttachmentEntity> attachmentEntities = attachmentRepository.findAllByUserIdAndDocument(userId, documentEntity);
            final List<AttachmentDto> attachments = attachmentEntities.stream().map(attachmentConverter::toAttachment).toList();
            audit("action: fetchAttachments, userId: {}, documentId: {}", userId, documentId.get());
            return new AttachmentResponse(attachments);
        }
        final List<AttachmentEntity> attachmentEntities = attachmentRepository.findAllByUserId(userId);
        final List<AttachmentDto> attachments = attachmentEntities.stream().map(attachmentConverter::toAttachment).toList();
        audit("action: fetchAttachments, userId: {}", userId, null);
        return new AttachmentResponse(attachments);
    }

    @Transactional
    public AttachmentCreateResponse createAttachment(final AttachmentCreateRequest request) {
        final String userId = request.userId();
        final String documentId = request.documentId();
        final DocumentEntity documentEntity = documentRepository.findById(documentId).orElseThrow(
                () -> new ResourceNotFoundException("Document not found, ID: '%s'".formatted(documentId)));
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
        audit("action: createAttachment, userId: {}, documentId: {}", userId, documentId);

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
        audit("action: createAttachment, userId: {}, documentId: {}", attachmentEntity.getUserId(), documentEntity.getId());

        return new AttachmentCreateResponse(attachmentEntity.getId(), documentEntity.getId());
    }

    @Transactional
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
        audit("action: updateAttachment, userId: {}, documentId: {}", attachmentEntity.getUserId(), documentEntity.getId());
    }

    @Transactional
    public void deleteAttachments(final String userId, final Optional<String> documentId) {
        if (documentId.isPresent()) {
            final DocumentEntity documentEntity = documentRepository.findById(documentId.get()).orElseThrow(
                    () -> new ResourceNotFoundException("Document not found, ID: '%s'".formatted(documentId)));
            attachmentRepository.deleteAllByUserIdAndDocument(userId, documentEntity);
            audit("action: deleteAttachments, userId: {}, documentId: {}", userId, documentId.get());
            return;
        }
        attachmentRepository.deleteAllByUserId(userId);
        audit("action: deleteAttachments, userId: {}", userId, null);
    }

    private void audit(final String message, final String userId, final String documentId) {
        final String loggedUsername = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElse(null);
        final AuditDetail auditDetail = AuditDetail.builder()
                .type("attachment")
                .param("userId", userId)
                .param("documentId", documentId)
                .param("actorId", loggedUsername)
                .build();
        audit.info(message, auditDetail, userId);
    }

}
