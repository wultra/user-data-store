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
package com.wultra.security.userdatastore.client.model.validation.constraintvalidators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentRequest;
import com.wultra.security.userdatastore.client.model.validation.constraints.DocumentRequestData;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
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
public class DocumentRequestValidator implements ConstraintValidator<DocumentRequestData, DocumentRequest> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean isValid(DocumentRequest value, ConstraintValidatorContext context) {
        return switch (value.dataType()) {
            case "claims", "jwt", "vc" -> {
                try {
                    mapper.readTree(value.documentData());
                    yield true;
                } catch (JsonProcessingException e) {
                    logger.debug("'{}' is not valid JSON", value.documentData(), e);
                    yield false;
                }
            }
            case "image_base64" -> {
                if ("photo".equals(value.documentType())) {
                    // avoid validation documentData, photo is encoded separately in the photo request
                    if (value instanceof DocumentCreateRequest createRequest) {
                        yield !CollectionUtils.isEmpty(createRequest.photos());
                    } else {
                        yield true;
                    }
                }
                try {
                    Base64.getDecoder().decode(value.documentData());
                    yield true;
                } catch (IllegalArgumentException e) {
                    logger.debug("{} is not Base64 encoded", value.documentData(), e);
                    yield false;
                }
            }
            case "binary_base64" -> {
                try {
                    Base64.getDecoder().decode(value.documentData());
                    yield true;
                } catch (IllegalArgumentException e) {
                    logger.debug("{} is not Base64 encoded", value.documentData(), e);
                    yield false;
                }
            }
            case "url" -> {
                try {
                    new URL(value.documentData());
                    yield true;
                } catch (MalformedURLException e) {
                    logger.debug("{} is not valid URL", value.documentData(), e);
                    yield false;
                }
            }
            default -> {
                logger.debug("No validation rule for dataType: {}", value.dataType());
                yield true;
            }
        };
    }
}
