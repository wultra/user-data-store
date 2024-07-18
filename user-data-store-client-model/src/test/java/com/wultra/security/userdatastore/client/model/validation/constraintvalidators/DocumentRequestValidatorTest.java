package com.wultra.security.userdatastore.client.model.validation.constraintvalidators;

import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.EmbeddedPhotoCreateRequest;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link DocumentRequestValidator}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
class DocumentRequestValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = createValidator();
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', textBlock = """
            jwt; {"sub":"1234567890","name":"John Doe","iat":1516239022}; profile
            image_base64; dGVzdA; profile
            image_base64; ***; photo
            binary_base64; dGVzdA; profile
            url; https://www.example.com; profile
            unknown; sample; profile
            """
    )
    void testValid(final String dataType, final String documentData, final String documentType) {
        final var validRequest = new DocumentCreateRequest("alice", documentType, dataType,
                documentData, null, documentData, Collections.emptyMap(), List.of(new EmbeddedPhotoCreateRequest("test_type", "dGVzdF9kYXRh", null)), Collections.emptyList());

        final Set<ConstraintViolation<DocumentCreateRequest>> result = validator.validate(validRequest);

        assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', textBlock = """
            jwt; ***; profile
            image_base64; ***; profile
            image_base64; ***; photo
            binary_base64; ***; profile
            url; invalid; profile"
            """
    )
    void testInvalid(final String dataType, final String documentData, final String documentType) {
        final var validRequest = new DocumentCreateRequest("alice", documentType, dataType,
                documentData, null, documentData, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());

        final Set<ConstraintViolation<DocumentCreateRequest>> result = validator.validate(validRequest);

        assertFalse(result.isEmpty());
        assertTrue(result.stream()
                .map(ConstraintViolation::getMessage)
                .anyMatch("Document request data is invalid for the given type"::equals));
    }

    private static Validator createValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }
}
