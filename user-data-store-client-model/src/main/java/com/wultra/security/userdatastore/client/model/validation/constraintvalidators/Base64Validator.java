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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator for {@link com.wultra.security.userdatastore.client.model.validation.constraints.Base64}.
 *
 * @author Lubos Racansky lubos.racansky@wultra.com
 */
@Slf4j
public class Base64Validator implements ConstraintValidator<com.wultra.security.userdatastore.client.model.validation.constraints.Base64, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            java.util.Base64.getDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException e) {
            logger.debug("{} is not Base64 encoded", value, e);
            return false;
        }
    }
}
