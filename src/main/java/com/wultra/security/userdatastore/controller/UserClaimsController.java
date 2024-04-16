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

import com.wultra.security.userdatastore.model.error.ClaimNotFoundException;
import com.wultra.security.userdatastore.model.error.EncryptionException;
import com.wultra.security.userdatastore.model.error.InvalidRequestException;
import com.wultra.security.userdatastore.service.UserClaimsService;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller providing API for CRUD of user claims.
 *
 * @author Lubos Racansky lubos.racansky@wultra.com
 */
@RestController
@Validated
@Slf4j
class UserClaimsController {

    private final UserClaimsService userClaimsService;

    UserClaimsController(UserClaimsService userClaimsService) {
        this.userClaimsService = userClaimsService;
    }

    /**
     * Exception handler for {@link InvalidRequestException} or {@link ConstraintViolationException}.
     *
     * @param e Exception.
     * @return Response with error details.
     */
    @ExceptionHandler({InvalidRequestException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRequestException(final RuntimeException e) {
        logger.warn("Error occurred when processing request object.", e);
        return new ErrorResponse("INVALID_REQUEST", e.getMessage());
    }

    /**
     * Exception handler for {@link ClaimNotFoundException}.
     *
     * @param e Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(ClaimNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotFoundException(final ClaimNotFoundException e) {
        logger.warn("Error occurred when processing request object.", e);
        return new ErrorResponse("NOT_FOUND", e.getMessage());
    }

    /**
     * Exception handler for {@link EncryptionException}.
     *
     * @param e Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(EncryptionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEncryptionException(final EncryptionException e) {
        logger.warn("Error occurred when processing request object.", e);
        return new ErrorResponse("ENCRYPTION_ERROR", e.getMessage());
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
    public ObjectResponse<Object> userClaims(@NotBlank @Size(max = 255) @RequestParam String userId) {
        logger.info("Fetching claims of user ID: {}", userId);
        final Object userClaims = userClaimsService.fetchUserClaims(userId);
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
    public Response createOrUpdate(@NotBlank @Size(max = 255) @RequestParam String userId, @RequestBody final Object claims) {
        logger.info("Creating or updating claims of user ID: {}", userId);
        userClaimsService.createOrUpdateUserClaims(userId, claims);
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
    public Response delete(@NotBlank @Size(max = 255) @RequestParam String userId) {
        logger.info("Deleting claims of user ID: {}", userId);
        userClaimsService.deleteUserClaims(userId);
        return new Response();
    }
}
