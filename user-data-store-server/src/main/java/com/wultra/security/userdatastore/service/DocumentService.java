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
import com.wultra.security.userdatastore.client.model.dto.DocumentDto;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentUpdateRequest;
import com.wultra.security.userdatastore.client.model.response.*;
import com.wultra.security.userdatastore.converter.DocumentConverter;
import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import com.wultra.security.userdatastore.model.entity.DocumentHistoryEntity;
import com.wultra.security.userdatastore.model.error.ResourceNotFoundException;
import com.wultra.security.userdatastore.model.repository.DocumentHistoryRepository;
import com.wultra.security.userdatastore.model.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for user documents.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
@Slf4j
@AllArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentHistoryRepository documentHistoryRepository;
    private final Audit audit;
    private final EncryptionService encryptionService;
    private final PhotoService photoService;
    private final AttachmentService attachmentService;
    private final DocumentConverter documentConverter;

    @Transactional(readOnly = true)
    public DocumentResponse fetchDocuments(final String userId, final Optional<String> documentId) {
        if (documentId.isPresent()) {
            final DocumentEntity documentEntity = documentRepository.findById(documentId.get()).orElseThrow(
                    () -> new ResourceNotFoundException("Document not found, ID: '%s'".formatted(documentId.get())));
            final DocumentDto document = documentConverter.toDocument(documentEntity);
            audit("action: fetchDocuments, userId: {}, documentId: {}", userId, documentId.get());
            return new DocumentResponse(Collections.singletonList(document));
        }
        final List<DocumentEntity> documentEntities = documentRepository.findAllByUserId(userId);
        final List<DocumentDto> documents = documentEntities.stream().map(documentConverter::toDocument).toList();
        audit("action: fetchDocuments, userId: {}", userId, null);
        return new DocumentResponse(documents);
    }

    @Transactional
    public DocumentCreateResponse createDocument(final DocumentCreateRequest request) {
        final String userId = request.userId();
        logger.debug("Creating document for user ID: {}", userId);
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setId(UUID.randomUUID().toString());
        documentEntity.setUserId(userId);
        documentEntity.setDocumentType(request.documentType());
        documentEntity.setDataType(request.dataType());
        documentEntity.setDocumentDataId(request.documentDataId());
        documentEntity.setExternalId(request.externalId());
        encryptionService.encryptDocumentData(documentEntity, request.documentData());
        documentConverter.convertAndSetAttributes(request.attributes(), documentEntity);

        final LocalDateTime timestamp = LocalDateTime.now();
        documentEntity.setTimestampCreated(timestamp);

        documentEntity = documentRepository.save(documentEntity);
        updateDocumentHistory(documentEntity);
        audit("action: createDocument, userId: {}, documentId: {}", userId, documentEntity.getId());

        final DocumentEntity documentEntityFinal = documentEntity;

        final List<EmbeddedPhotoCreateResponse> photosResponse = new ArrayList<>();
        if (!CollectionUtils.isEmpty(request.photos())) {
            photosResponse.addAll(request.photos().stream()
                    .map(photoRequest -> photoService.createPhoto(photoRequest, documentEntityFinal))
                    .map(response -> new EmbeddedPhotoCreateResponse(response.id()))
                    .toList());
        }

        final List<EmbeddedAttachmentCreateResponse> attachmentsResponse = new ArrayList<>();
        if (!CollectionUtils.isEmpty(request.attachments())) {
            attachmentsResponse.addAll(request.attachments().stream()
                    .map(attachmentRequest -> attachmentService.createAttachment(attachmentRequest, documentEntityFinal))
                    .map(response -> new EmbeddedAttachmentCreateResponse(response.id()))
                    .toList());
        }
        return new DocumentCreateResponse(documentEntity.getId(), documentEntity.getDocumentDataId(), photosResponse, attachmentsResponse);
    }

    @Transactional
    public void updateDocument(final String documentId, final DocumentUpdateRequest request) {
        final String userId = request.userId();
        logger.debug("Updating document for user ID: {}", userId);
        final DocumentEntity documentEntity = documentRepository.findById(documentId).orElseThrow(
                () -> new ResourceNotFoundException("Document not found, ID: '%s'".formatted(documentId)));
        documentEntity.setUserId(userId);
        documentEntity.setDocumentType(request.documentType());
        documentEntity.setDataType(request.dataType());
        documentEntity.setDocumentDataId(request.documentDataId());
        documentEntity.setExternalId(request.externalId());
        encryptionService.encryptDocumentData(documentEntity, request.documentData());
        documentConverter.convertAndSetAttributes(request.attributes(), documentEntity);

        final LocalDateTime timestamp = LocalDateTime.now();
        documentEntity.setTimestampLastUpdated(timestamp);

        documentRepository.save(documentEntity);
        updateDocumentHistory(documentEntity);
        audit("action: updateDocument, userId: {}, documentId: {}", userId, documentId);
    }

    @Transactional
    public void deleteDocuments(final String userId, final Optional<String> documentId) {
        photoService.deletePhotos(userId, documentId);
        attachmentService.deleteAttachments(userId, documentId);
        if (documentId.isPresent()) {
            int count = documentRepository.deleteAllByUserIdAndId(userId, documentId.get());
            if (count == 1) {
                audit("action: deleteDocuments, userId: {}, documentId: {}", userId, documentId.get());
            }
            return;
        }
        documentRepository.deleteAllByUserId(userId);
        audit("action: deleteDocuments, userId: {}", userId, null);
    }

    private void audit(final String message, final String userId, final String documentId) {
        final String loggedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        final AuditDetail auditDetail = AuditDetail.builder()
                .type("document")
                .param("userId", userId)
                .param("documentId", documentId)
                .param("actorId", loggedUsername)
                .build();
        audit.info(message, auditDetail, userId);
    }

    private void updateDocumentHistory(final DocumentEntity documentEntity) {
        final DocumentHistoryEntity historyEntity = new DocumentHistoryEntity();
        historyEntity.setId(UUID.randomUUID().toString());
        historyEntity.setDocumentId(documentEntity.getId());
        historyEntity.setUserId(documentEntity.getUserId());
        historyEntity.setDocumentType(documentEntity.getDocumentType());
        historyEntity.setDataType(documentEntity.getDataType());
        historyEntity.setDocumentDataId(documentEntity.getDocumentDataId());
        historyEntity.setExternalId(documentEntity.getExternalId());
        historyEntity.setDocumentData(documentEntity.getDocumentData());
        historyEntity.setEncryptionMode(documentEntity.getEncryptionMode());
        historyEntity.setAttributes(documentEntity.getAttributes());
        historyEntity.setTimestampCreated(LocalDateTime.now());
        documentHistoryRepository.save(historyEntity);
    }
}
