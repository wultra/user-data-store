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
package com.wultra.security.userdatastore.controller;

import com.wultra.security.userdatastore.service.UserClaimsService;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller providing API for CRUD of user claims.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@Validated
@Slf4j
class UserClaimsController {

    private final UserClaimsService userClaimsService;

    @Autowired
    UserClaimsController(UserClaimsService userClaimsService) {
        this.userClaimsService = userClaimsService;
    }

    /**
     * Return claims for the given user.
     *
     * @param userId user identifier
     * @return user claims
     */
    @Operation(
            summary = "Return claims",
            description = "Return claims for the given user."
    )
    @GetMapping("/private/user-claims")
    public ObjectResponse<Object> fetchClaims(@NotBlank @Size(max = 255) @RequestParam String userId) {
        logger.info("action: fetchClaims, state: initiated, userId: {}", userId);
        final Object userClaims = userClaimsService.fetchUserClaims(userId);
        logger.info("action: fetchClaims, state: succeeded, userId: {}", userId);
        return new ObjectResponse<>(userClaims);
    }

    /**
     * Create claims for the given user or update the exiting ones.
     *
     * @param userId user identifier
     * @param claims claims to be stored
     * @return response status
     */
    @Operation(
            summary = "Create or update claims",
            description = "Create claims for the given user or update the exiting ones."
    )
    @PostMapping("/public/user-claims")
    public Response storeClaims(@NotBlank @Size(max = 255) @RequestParam String userId, @RequestBody final Object claims) {
        logger.info("action: storeClaims, state: initiated, userId: {}", userId);
        userClaimsService.createOrUpdateUserClaims(userId, claims);
        logger.info("action: storeClaims, state: succeeded, userId: {}", userId);
        return new Response();
    }

    /**
     * Delete claims of the given user.
     *
     * @param userId user identifier
     * @return response status
     */
    @Operation(
            summary = "Delete claims",
            description = "Delete claims of the given user."
    )
    @DeleteMapping("/public/user-claims")
    public Response deleteClaims(@NotBlank @Size(max = 255) @RequestParam String userId) {
        logger.info("action: deleteClaims, state: initiated, userId: {}", userId);
        userClaimsService.deleteUserClaims(userId);
        logger.info("action: deleteClaims, state: succeeded, userId: {}", userId);
        return new Response();
    }
}
