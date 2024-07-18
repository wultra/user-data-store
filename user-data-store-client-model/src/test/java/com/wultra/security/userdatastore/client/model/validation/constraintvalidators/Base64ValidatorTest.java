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
