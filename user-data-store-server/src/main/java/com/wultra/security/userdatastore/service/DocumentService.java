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
import com.wultra.security.userdatastore.converter.DocumentConverter;
import com.wultra.security.userdatastore.model.dto.DocumentDto;
import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import com.wultra.security.userdatastore.model.error.ResourceNotFoundException;
import com.wultra.security.userdatastore.model.repository.DocumentRepository;
import com.wultra.security.userdatastore.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.model.request.DocumentUpdateRequest;
import com.wultra.security.userdatastore.model.response.DocumentCreateResponse;
import com.wultra.security.userdatastore.model.response.DocumentResponse;
import io.getlime.core.rest.model.base.response.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final Audit audit;
    private final EncryptionService encryptionService;
    private final DocumentConverter documentConverter;

    @Transactional(readOnly = true)
    public DocumentResponse fetchDocuments(final String userId, final String documentId) {
        if (documentId != null) {
            final Optional<DocumentEntity> documentEntityOptional = documentRepository.findById(documentId);
            if (documentEntityOptional.isEmpty()) {
                throw new ResourceNotFoundException("Document not found, ID: '%s'".formatted(documentId));
            }
            final DocumentDto document = documentConverter.toDocument(documentEntityOptional.get());
            final DocumentResponse response = new DocumentResponse();
            response.add(document);
            return response;
        }
        final List<DocumentEntity> documentEntities = documentRepository.findAllByUserId(userId);
        documentEntities.forEach(encryptionService::decryptDocumentData);
        final List<DocumentDto> documents = documentEntities.stream().map(documentConverter::toDocument).toList();
        audit("Retrieved documents of user ID: {}", userId);
        final DocumentResponse response = new DocumentResponse();
        response.addAll(documents);
        return response;
    }

    @Transactional
    public DocumentCreateResponse createDocument(final DocumentCreateRequest request) {
        final String userId = request.userId();
        logger.debug("Creating document for user ID: {}", userId);
        final DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setId(UUID.randomUUID().toString());
        documentEntity.setUserId(userId);
        documentEntity.setDocumentType(request.documentType());
        documentEntity.setDataType(request.dataType());
        documentEntity.setDocumentDataId(request.documentDataId());
        encryptionService.encryptDocumentData(documentEntity, request.documentData());
        documentConverter.convertAntSetAttributes(request.attributes(), documentEntity);

        final LocalDateTime timestamp = LocalDateTime.now();
        documentEntity.setTimestampCreated(timestamp);

        documentRepository.save(documentEntity);
        audit("Created document for user ID: {}", userId);

        final DocumentCreateResponse response = new DocumentCreateResponse();
        response.setId(documentEntity.getId());
        response.setDocumentId(documentEntity.getDocumentDataId());
        return response;
    }

    @Transactional
    public Response updateDocument(final DocumentUpdateRequest request) {
        final String userId = request.userId();
        logger.debug("Updating document for user ID: {}", userId);
        final Optional<DocumentEntity> documentEntityOptional = documentRepository.findById(request.id());
        if (documentEntityOptional.isEmpty()) {
            throw new ResourceNotFoundException("Document not found, ID: '%s'".formatted(request.id()));
        }
        final DocumentEntity documentEntity = documentEntityOptional.get();
        documentEntity.setId(UUID.randomUUID().toString());
        documentEntity.setUserId(userId);
        documentEntity.setDocumentType(request.documentType());
        documentEntity.setDataType(request.dataType());
        documentEntity.setDocumentDataId(request.documentDataId());
        encryptionService.encryptDocumentData(documentEntity, request.documentData());
        documentConverter.convertAntSetAttributes(request.attributes(), documentEntity);

        final LocalDateTime timestamp = LocalDateTime.now();
        documentEntity.setTimestampLastUpdated(timestamp);

        documentRepository.save(documentEntity);
        audit("Updated document for user ID: {}", userId);

        return new Response();
    }

    @Transactional
    public void deleteDocuments(final String userId, final String documentId) {
        if (documentId != null) {
            int count = documentRepository.deleteAllByUserIdAndId(userId, documentId);
            if (count == 1) {
                audit("Deleted document with ID: {}", documentId);
            }
            return;
        }
        documentRepository.deleteAllByUserId(userId);
        audit("Deleted documents for user ID: {}", userId);
    }

    private void audit(final String message, final String userId) {
        final String loggedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        final AuditDetail auditDetail = AuditDetail.builder()
                .type("document")
                .param("userId", userId)
                .param("actorId", loggedUsername)
                .build();
        audit.info(message, auditDetail, userId);
    }
}
