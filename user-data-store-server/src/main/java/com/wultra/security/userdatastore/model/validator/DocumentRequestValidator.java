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
package com.wultra.security.userdatastore.model.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentUpdateRequest;
import com.wultra.security.userdatastore.client.model.request.EmbeddedAttachmentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.EmbeddedPhotoCreateRequest;
import com.wultra.security.userdatastore.model.error.RequestValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

/**
 * Validator for document requests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Slf4j
public class DocumentRequestValidator {

    final ObjectMapper mapper = new ObjectMapper();
    final PhotoRequestValidator photoValidator = new PhotoRequestValidator();
    final AttachmentRequestValidator attachmentValidator = new AttachmentRequestValidator();

    public void validateRequest(final DocumentCreateRequest request) {
        validateDataTypes(request.documentType(), request.dataType(), request.documentData());
        if (request.photos() != null) {
            for (EmbeddedPhotoCreateRequest photoRequest: request.photos()) {
                photoValidator.validateRequest(photoRequest);
            }
        }
        if (request.attachments() != null) {
            for (EmbeddedAttachmentCreateRequest attachmentRequest: request.attachments()) {
                attachmentValidator.validateRequest(attachmentRequest);
            }
        }
        if (request.documentType().equals("photo") && CollectionUtils.isEmpty(request.photos())) {
            throw new RequestValidationException("Missing photo for document type 'photo'");
        }
    }

    public void validateRequest(final DocumentUpdateRequest request) {
        validateDataTypes(request.documentType(), request.dataType(), request.documentData());
    }

    private void validateDataTypes(final String documentType, final String dataType, final String documentData) {
        switch (dataType) {
            case "claims", "jwt", "vc" -> {
                try {
                    mapper.readTree(documentData);
                } catch (JsonProcessingException e) {
                    throw new RequestValidationException(e.getMessage(), e);
                }
            }
            case "image_base64" -> {
                if (documentType.equals("photo")) {
                    // avoid validation, photo is encoded separately in the photo request
                    return;
                }
                try {
                    Base64.getDecoder().decode(documentData);
                } catch (IllegalArgumentException e) {
                    throw new RequestValidationException(e.getMessage(), e);
                }
            }
            case "binary_base64" -> {
                try {
                    Base64.getDecoder().decode(documentData);
                } catch (IllegalArgumentException e) {
                    throw new RequestValidationException(e.getMessage(), e);
                }
            }
            case "url" -> {
                try {
                    new URL(documentData);
                } catch (MalformedURLException e) {
                    throw new RequestValidationException(e.getMessage(), e);
                }
            }
            default -> logger.debug("No validation rule for dataType: {}", dataType);
        }
    }
}
