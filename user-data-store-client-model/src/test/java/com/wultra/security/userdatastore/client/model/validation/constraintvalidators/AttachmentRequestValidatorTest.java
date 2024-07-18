package com.wultra.security.userdatastore.client.model.validation.constraintvalidators;

import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.EmbeddedAttachmentCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link AttachmentRequestValidator}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
class AttachmentRequestValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = createValidator();
    }

    @ParameterizedTest
    @CsvSource({
            "text, sample",
            "unknown,sample",
            "image_base64, dGVzdA",
            "binary_base64, dGVzdA",
    })
    void testValid(final String attachmentType, final String attachmentData) {
        final EmbeddedAttachmentCreateRequest attachmentRequest = new EmbeddedAttachmentCreateRequest(attachmentType, attachmentData, "3c39ffd2-b639-4bb7-a25a-3a6eefdba8ef");
        final var validRequest = new DocumentCreateRequest("alice", "profile", "claims",
                "83692", null, "{}", Collections.emptyMap(), Collections.emptyList(), List.of(attachmentRequest));

        final Set<ConstraintViolation<DocumentCreateRequest>> result = validator.validate(validRequest);

        assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "text,",
            "image_base64, *****",
            "binary_base64, image_base64, *****",
    })
    void testInvalid(final String attachmentType, final String attachmentData) {
        final EmbeddedAttachmentCreateRequest attachmentRequest = new EmbeddedAttachmentCreateRequest(attachmentType, attachmentData, "3c39ffd2-b639-4bb7-a25a-3a6eefdba8ef");
        final var validRequest = new DocumentCreateRequest("alice", "profile", "claims",
                "83692", null, "{}", Collections.emptyMap(), Collections.emptyList(), List.of(attachmentRequest));

        final Set<ConstraintViolation<DocumentCreateRequest>> result = validator.validate(validRequest);

        assertFalse(result.isEmpty());
        assertTrue(result.stream()
                .map(ConstraintViolation::getMessage)
                .anyMatch("Attachment request data is invalid for the given type"::equals));
    }

    private static Validator createValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }
}
