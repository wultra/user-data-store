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

import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.EmbeddedPhotoCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link Base64Validator}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
class Base64ValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = createValidator();
    }

    @Test
    void testValid() {
        final String validBase64 = "dGVzdA";

        final EmbeddedPhotoCreateRequest photoCreateRequest = new EmbeddedPhotoCreateRequest("person", validBase64, "");
        final var validRequest = new DocumentCreateRequest("alice", "profile", "claims",
                "83692", null, "{}", Collections.emptyMap(), List.of(photoCreateRequest), Collections.emptyList());

        final Set<ConstraintViolation<DocumentCreateRequest>> result = validator.validate(validRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    void testInvalid() {
        final String invalidBase64 = "****";

        final EmbeddedPhotoCreateRequest photoCreateRequest = new EmbeddedPhotoCreateRequest("person", invalidBase64, "");
        final var validRequest = new DocumentCreateRequest("alice", "profile", "claims",
                "83692", null, "{}", Collections.emptyMap(), List.of(photoCreateRequest), Collections.emptyList());

        final Set<ConstraintViolation<DocumentCreateRequest>> result = validator.validate(validRequest);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("'****' is not Base64 encoded", result.iterator().next().getMessage());
    }

    private static Validator createValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }
}
