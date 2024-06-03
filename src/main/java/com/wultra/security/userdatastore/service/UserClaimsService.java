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
import com.wultra.security.userdatastore.model.entity.UserClaimsEntity;
import com.wultra.security.userdatastore.model.error.InvalidRequestException;
import com.wultra.security.userdatastore.model.error.ResourceNotFoundException;
import com.wultra.security.userdatastore.model.repository.UserClaimsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    private final UserClaimsRepository userClaimsRepository;

    private final Audit audit;

    private final EncryptionService encryptionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public Object fetchUserClaims(final String userId) {
        final String claims = userClaimsRepository.findById(userId)
                .map(encryptionService::decryptClaims)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Claims for user ID: '%s' not found".formatted(userId)));
        audit("Retrieved claims of user ID: {}", userId);
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
        userClaimsRepository.findById(userId).ifPresentOrElse(entity -> {
                    logger.debug("Updating claims of user ID: {}", userId);
                    encryptionService.encryptClaims(entity, claimsAsString);
                    entity.setTimestampLastUpdated(LocalDateTime.now());
                    audit("Updated claims of user ID: {}", userId);
                },
                () -> {
                    logger.debug("Creating new claims of user ID: {}", userId);
                    final UserClaimsEntity entity = new UserClaimsEntity();
                    entity.setUserId(userId);
                    encryptionService.encryptClaims(entity, claimsAsString);
                    userClaimsRepository.saveAndFlush(entity);
                    audit("Created claims for user ID: {}", userId);
                });
    }

    @Transactional
    public void deleteUserClaims(final String userId) {
        final UserClaimsEntity user = userClaimsRepository.getReferenceById(userId);
        userClaimsRepository.delete(user);
        audit("Deleted claims of user ID: {}", userId);
    }

    private void audit(final String message, final String userId) {
        final String loggedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        final AuditDetail auditDetail = AuditDetail.builder()
                .type("userClaims")
                .param("userId", userId)
                .param("actorId", loggedUsername)
                .build();
        audit.info(message, auditDetail, userId);
    }
}
