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
package com.wultra.security.userdatastore.client.model.validation.constraints;

import com.wultra.security.userdatastore.client.model.validation.constraintvalidators.DocumentRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotated document class must have valid combination of type and data.
 *
 * @author Lubos Racansky lubos.racansky@wultra.com
 */
@Target(TYPE_USE)
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = DocumentRequestValidator.class)
public @interface DocumentRequestData {

    String message() default "Document request data is invalid for the given type";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
