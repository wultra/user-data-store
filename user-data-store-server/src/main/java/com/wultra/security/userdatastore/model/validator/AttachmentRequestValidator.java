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

import com.wultra.security.userdatastore.client.model.request.*;
import com.wultra.security.userdatastore.model.error.RequestValidationException;
import org.springframework.util.StringUtils;

import java.util.Base64;

/**
 * Validator for attachment requests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AttachmentRequestValidator {

    public void validateRequest(final AttachmentCreateRequest request) {
        validateDataType(request.attachmentType(), request.attachmentData());
    }

    public void validateRequest(final AttachmentUpdateRequest request) {
        validateDataType(request.attachmentType(), request.attachmentData());
    }

    public void validateRequest(final EmbeddedAttachmentCreateRequest request) {
        validateDataType(request.attachmentType(), request.attachmentData());
    }

    private void validateDataType(final String attachmentType, final String attachmentData) {
        switch (attachmentType) {
            case "image_base64":
            case "binary_base64":
                try {
                    Base64.getDecoder().decode(attachmentData);
                } catch (IllegalArgumentException e) {
                    throw new RequestValidationException(e.getMessage(), e);
                }
                break;
            case "text":
                if (!StringUtils.hasText(attachmentData)) {
                    throw new RequestValidationException("Missing attachment data for type 'text'.");
                }
                break;
        }
    }
}
