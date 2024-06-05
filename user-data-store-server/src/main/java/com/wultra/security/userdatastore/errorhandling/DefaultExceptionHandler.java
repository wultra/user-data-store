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
package com.wultra.security.userdatastore.errorhandling;

import com.wultra.security.userdatastore.model.error.EncryptionException;
import com.wultra.security.userdatastore.model.error.InvalidRequestException;
import com.wultra.security.userdatastore.model.error.ResourceNotFoundException;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Exception handler.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 * @author Roman Strobl, roman.strobl@wulra.com
 */
@ControllerAdvice
@Slf4j
class DefaultExceptionHandler {

    /**
     * Exception handler for no resource found.
     *
     * @param e Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse handleNoResourceFoundException(final NoResourceFoundException e) {
        logger.warn("Error occurred when calling an API: {}", e.getMessage());
        logger.debug("Exception detail: ", e);
        return new ErrorResponse("ERROR_NOT_FOUND", "Resource not found.");
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
     * Exception handler for {@link ResourceNotFoundException}.
     *
     * @param e Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotFoundException(final ResourceNotFoundException e) {
        logger.warn("Error occurred when processing request object.", e);
        return new ErrorResponse("NOT_FOUND", e.getMessage());
    }

}
