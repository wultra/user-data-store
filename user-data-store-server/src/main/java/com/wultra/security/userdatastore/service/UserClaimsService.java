/*
 * User Data Store
 * Copyright (C) 2023 Wultra s.r.o.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import com.wultra.security.userdatastore.model.error.InvalidRequestException;
import com.wultra.security.userdatastore.model.error.ResourceNotFoundException;
import com.wultra.security.userdatastore.model.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for user claims.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserClaimsService {

    private static final String CLAIMS_DOCUMENT_TYPE = "profile";
    private static final String CLAIMS_DATA_TYPE = "claims";
    private static final String CLAIMS_DOCUMENT_DATA_ID = null;

    private final DocumentRepository documentRepository;
    private final Audit audit;
    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public Object fetchUserClaims(final String userId) {
        final String claims = documentRepository.findAllByUserIdAndDataType(userId, CLAIMS_DATA_TYPE)
                .stream()
                .map(encryptionService::decryptDocumentData)
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Claims for user ID: '%s' not found".formatted(userId)));
        audit("action: fetchUserClaims, userId: {}", userId, null);
        try {
            return objectMapper.readValue(claims, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new InvalidRequestException(e);
        }
    }

    @Transactional
    public void createOrUpdateUserClaims(final String userId, final Object claims) {
        final String claimsAsString;
        try {
            claimsAsString = objectMapper.writeValueAsString(claims);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestException(e);
        }
        documentRepository.findAllByUserIdAndDataType(userId, CLAIMS_DATA_TYPE).stream().findAny()
                .ifPresentOrElse(entity -> {
                    logger.debug("Updating claims of user ID: {}", userId);
                    encryptionService.encryptDocumentData(entity, claimsAsString);
                    entity.setTimestampLastUpdated(LocalDateTime.now());
                    audit("action: updateUserClaims, userId: {}, documentId: {}", userId, entity.getId());
                },
                () -> {
                    logger.debug("Creating new claims of user ID: {}", userId);
                    final DocumentEntity entity = new DocumentEntity();
                    entity.setId(UUID.randomUUID().toString());
                    entity.setUserId(userId);
                    entity.setDocumentType(CLAIMS_DOCUMENT_TYPE);
                    entity.setDataType(CLAIMS_DATA_TYPE);
                    entity.setDocumentDataId(CLAIMS_DOCUMENT_DATA_ID);
                    entity.setAttributes("{}");
                    entity.setTimestampCreated(LocalDateTime.now());
                    encryptionService.encryptDocumentData(entity, claimsAsString);

                    documentRepository.save(entity);
                    audit("action: createUserClaims, userId: {}, documentId: {}", userId, entity.getId());
                });
    }

    @Transactional
    public void deleteUserClaims(final String userId) {
        final List<DocumentEntity> toDelete = documentRepository.findAllByUserIdAndDataType(userId, CLAIMS_DATA_TYPE);
        documentRepository.deleteAll(toDelete);
        audit("action: deleteUserClaims, userId: {}", userId, null);
    }

    private void audit(final String message, final String userId, final String documentId) {
        final String loggedUsername = SecurityContextHolder.getContext().getAuthentication() != null ? SecurityContextHolder.getContext().getAuthentication().getName() : null;
        final AuditDetail auditDetail = AuditDetail.builder()
                .type("userClaims")
                .param("userId", userId)
                .param("documentId", documentId)
                .param("actorId", loggedUsername)
                .build();
        audit.info(message, auditDetail, userId);
    }
}
