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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import com.wultra.security.userdatastore.model.error.InvalidRequestException;
import com.wultra.security.userdatastore.model.error.ResourceAlreadyExistsException;
import com.wultra.security.userdatastore.model.error.ResourceNotFoundException;
import com.wultra.security.userdatastore.model.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for manipulating claims.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
@Slf4j
@AllArgsConstructor
public class ClaimsService {

    private static final String CLAIMS_DOCUMENT_TYPE = "profile";
    private static final String CLAIMS_DATA_TYPE = "claims";
    private static final String CLAIMS_DOCUMENT_DATA_ID = null;

    private final DocumentRepository documentRepository;
    private final Audit audit;
    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public Object fetchClaims(final String userId, final Optional<String> claim) {
        final String claims = readClaims(userId);
        if (claim.isEmpty()) {
            audit("action: fetchClaims, userId: {}", userId);
            try {
                return objectMapper.<Map<String, Object>>readValue(claims, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                throw new InvalidRequestException(e);
            }
        }
        try {
            audit("action: fetchClaims, userId: {}, claim: {}", userId, claim.get());
            final Map<String, Object> claimMap = objectMapper.readValue(claims, new TypeReference<>() {});
            return claimMap.get(claim.get());
        } catch (JsonProcessingException e) {
            throw new InvalidRequestException(e);
        }
    }

    @Transactional
    public void createClaims(final String userId, final Object claims) {
        final String claimsAsString;
        try {
            claimsAsString = objectMapper.writeValueAsString(claims);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestException(e);
        }
        documentRepository.findAllByUserIdAndDataType(userId, CLAIMS_DATA_TYPE).stream().findAny()
                .ifPresentOrElse(entity -> {
                            throw new ResourceAlreadyExistsException("Claims for user '%s' already exist".formatted(userId));
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
                            audit("action: createClaims, userId: {}", userId);
                        });
    }

    @Transactional
    public void updateClaims(final String userId, final Object claims) {
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
                            audit("action: updateClaims, userId: {}", userId);
                        },
                        () -> {
                            throw new ResourceNotFoundException("Claims for user '%s' do not exist".formatted(userId));
                        });
    }

    @Transactional
    public void deleteClaims(final String userId, final String claim) {
        if (!StringUtils.hasText(claim)) {
            final List<DocumentEntity> toDelete = documentRepository.findAllByUserIdAndDataType(userId, CLAIMS_DATA_TYPE);
            documentRepository.deleteAll(toDelete);
            audit("action: deleteClaims, userId: {}", userId);
            return;
        }
        documentRepository.findAllByUserIdAndDataType(userId, CLAIMS_DATA_TYPE).stream().findAny()
                .ifPresentOrElse(entity -> {
                    logger.debug("Updating claims of user ID: {}, deleted claim: {}", userId, claim);
                    final String claims = encryptionService.decryptDocumentData(entity);
                    final Map<String, Object> claimMap;
                    try {
                        claimMap = objectMapper.readValue(claims, new TypeReference<>() {});
                        claimMap.remove(claim);
                        encryptionService.encryptDocumentData(entity, objectMapper.writeValueAsString(claimMap));
                    } catch (JsonProcessingException e) {
                        throw new InvalidRequestException(e);
                    }
                    entity.setTimestampLastUpdated(LocalDateTime.now());

                    documentRepository.save(entity);
                    audit("action: deleteClaims, userId: {}, claim: {}", userId, claim);
                },
                () -> logger.debug("Delete request ignored, no claims found for user ID: {}", userId));
    }

    private String readClaims(final String userId) {
        return documentRepository.findAllByUserIdAndDataType(userId, CLAIMS_DATA_TYPE)
                .stream()
                .map(encryptionService::decryptDocumentData)
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Claims for user ID: '%s' not found".formatted(userId)));
    }

    private void audit(final String message, final String userId) {
        final String loggedUsername = SecurityContextHolder.getContext().getAuthentication() != null ? SecurityContextHolder.getContext().getAuthentication().getName() : null;
        final AuditDetail auditDetail = AuditDetail.builder()
                .type("claims")
                .param("userId", userId)
                .param("actorId", loggedUsername)
                .build();
        audit.info(message, auditDetail, userId);
    }

    private void audit(final String message, final String userId, final String claim) {
        final String loggedUsername = SecurityContextHolder.getContext().getAuthentication() != null ? SecurityContextHolder.getContext().getAuthentication().getName() : null;
        final AuditDetail auditDetail = AuditDetail.builder()
                .type("claims")
                .param("userId", userId)
                .param("claim", claim)
                .param("actorId", loggedUsername)
                .build();
        audit.info(message, auditDetail, userId);
    }

}
