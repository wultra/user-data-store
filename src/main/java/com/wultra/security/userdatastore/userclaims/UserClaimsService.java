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
package com.wultra.security.userdatastore.userclaims;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;

/**
 * Service for user claims.
 *
 * @author Lubos Racansky lubos.racansky@wultra.com
 */
@Service
@Transactional
@Slf4j
class UserClaimsService {

    private final UserClaimsRepository userClaimsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    UserClaimsService(final UserClaimsRepository userClaimsRepository) {
        this.userClaimsRepository = userClaimsRepository;
    }

    @Transactional(readOnly = true)
    public Object fetchUserClaims(final String userId) {
        final String claims = userClaimsRepository.findById(userId).orElseThrow(() ->
                        new EntityNotFoundException("Claims for user ID: '%s' not found".formatted(userId)))
                .getClaims();
        try {
            return objectMapper.readValue(claims, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            // TODO (racansky, 2023-02-21) choose a dedicated exception, add exception handler
            throw new RuntimeException(e);
        }
    }

    public void createOrUpdateUserClaims(final String userId, final Object claims) {
        final String claimsAsString;
        try {
            claimsAsString = objectMapper.writeValueAsString(claims);
        } catch (JsonProcessingException e) {
            // TODO (racansky, 2023-02-21) choose a dedicated exception, add exception handler
            throw new RuntimeException(e);
        }
        userClaimsRepository.findById(userId).ifPresentOrElse(userClaims -> {
                    logger.debug("Updating claims of user ID: {}", userId);
                    userClaims.setClaims(claimsAsString);
                    userClaims.setTimestampUpdated(LocalDateTime.now());
                },
                () -> {
                    logger.debug("Creating new claims of user ID: {}", userId);
                    final UserClaimsEntity userClaims = new UserClaimsEntity();
                    userClaims.setUserId(userId);
                    userClaims.setClaims(claimsAsString);
                    userClaimsRepository.saveAndFlush(userClaims);
                });
    }

    public void deleteUserClaims(final String userId) {
        final UserClaimsEntity user = userClaimsRepository.getReferenceById(userId);
        userClaimsRepository.delete(user);
    }
}
