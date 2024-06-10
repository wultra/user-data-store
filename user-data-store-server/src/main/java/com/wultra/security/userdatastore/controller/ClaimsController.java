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
package com.wultra.security.userdatastore.controller;

import com.wultra.security.userdatastore.service.ClaimsService;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller providing API for manipulating claims individually.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@Validated
@Slf4j
class ClaimsController {

    private final ClaimsService claimsService;

    @Autowired
    ClaimsController(ClaimsService claimsService) {
        this.claimsService = claimsService;
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
    @GetMapping("/claims")
    public ObjectResponse<Object> fetchClaim(@NotBlank @Size(max = 255) @RequestParam String userId, @Size(max = 255) @RequestParam(required = false) String claim) {
        logger.info("Fetching claim of user ID: {}, claim: {}", userId, claim);
        final Object claims = claimsService.fetchClaims(userId, Optional.ofNullable(claim));
        return new ObjectResponse<>(claims);
    }

    /**
     * Create claim for the given user or update an exiting one.
     *
     * @param userId user identifier
     * @param claim claim to be stored
     * @param value claim value
     * @return response status
     */
    @Operation(
            summary = "Create or update a claim",
            description = "Create claim for the given user or update an exiting one."
    )
    @PostMapping("/admin/claim")
    public Response storeClaim(@NotBlank @Size(max = 255) @RequestParam String userId, @NotBlank @Size(max = 255) String claim, @RequestBody final String value) {
        logger.info("Creating or updating claim of user ID: {}, claim: {} ", userId, claim);
        claimsService.createOrUpdateClaim(userId, claim, value);
        return new Response();
    }

    /**
     * Create claims for the given user or update the exiting ones.
     *
     * @param userId user identifier
     * @param claims claims
     * @return response status
     */
    @Operation(
            summary = "Create or update claims",
            description = "Create claims for the given user or update the exiting ones."
    )
    @PostMapping("/admin/claims")
    public Response storeClaims(@NotBlank @Size(max = 255) @RequestParam String userId, @RequestBody final Object claims) {
        logger.info("Creating or updating claim of user ID: {}", claims);
        claimsService.createOrUpdateClaims(userId, claims);
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
    @DeleteMapping("/admin/claims")
    public Response deleteClaim(@NotBlank @Size(max = 255) @RequestParam String userId, @Size(max = 255) @RequestParam(required = false) String claim) {
        logger.info("Updating claims of user ID: {}, deleted claim: {}", userId, claim);
        claimsService.deleteClaims(userId, claim);
        return new Response();
    }
}
