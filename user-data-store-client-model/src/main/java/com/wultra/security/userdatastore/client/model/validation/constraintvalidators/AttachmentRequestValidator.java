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

import com.wultra.security.userdatastore.client.model.request.AttachmentRequest;
import com.wultra.security.userdatastore.client.model.validation.constraints.AttachmentRequestData;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Base64;

/**
 * Validator for {@link AttachmentRequestData}.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Slf4j
public class AttachmentRequestValidator implements ConstraintValidator<AttachmentRequestData, AttachmentRequest> {

    @Override
    public boolean isValid(AttachmentRequest value, ConstraintValidatorContext context) {
        return switch (value.attachmentType()) {
            case "image_base64", "binary_base64" -> {
                try {
                    Base64.getDecoder().decode(value.attachmentData());
                    yield true;
                } catch (IllegalArgumentException e) {
                    logger.debug("{} is not Base64 encoded", value.attachmentData(), e);
                    yield false;
                }
            }
            case "text" -> StringUtils.hasText(value.attachmentData());
            default -> {
                logger.debug("No specific rules for data format of attachment type: {}", value.attachmentType());
                yield true;
            }
        };
    }
}
