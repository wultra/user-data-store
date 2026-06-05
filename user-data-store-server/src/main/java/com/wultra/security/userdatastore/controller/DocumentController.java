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
package com.wultra.security.userdatastore.controller;

import com.wultra.core.rest.model.base.request.ObjectRequest;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import com.wultra.core.rest.model.base.response.Response;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentUpdateRequest;
import com.wultra.security.userdatastore.client.model.response.DocumentCreateResponse;
import com.wultra.security.userdatastore.client.model.response.DocumentResponse;
import com.wultra.security.userdatastore.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.wultra.security.userdatastore.logging.StructuredLogging.action;
import static com.wultra.security.userdatastore.logging.StructuredLogging.kv;
import static com.wultra.security.userdatastore.logging.StructuredLogging.stateInitiated;
import static com.wultra.security.userdatastore.logging.StructuredLogging.stateSucceeded;

/**
 * REST controller providing API for CRUD for user documents.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@Validated
@Slf4j
@AllArgsConstructor
class DocumentController {

    private final DocumentService documentService;

    /**
     * Return documents for the given user.
     *
     * @param userId user identifier
     * @param documentId optional document identifier; when provided, {@code documentType} and {@code attributes} are ignored
     * @param documentType optional document type; when provided, only documents of this type are returned
     * @param attributes optional list of required attribute key-value pairs in the form {@code key:value};
     *                   only documents whose {@code attributes} map contains all the given pairs are returned.
     *                   Spring MVC does not support direct binding of a named query parameter into a
     *                   {@code Map<String, String>}, so the pairs are encoded as a repeatable list parameter,
     *                   e.g. {@code ?attributes=status:active&attributes=category:contract}
     * @return user documents
     */
    @Operation(
            summary = "Return documents",
            description = """
                    Return documents for the given user, optionally filtered by document ID, document type,
                    and required attribute key-value pairs."""
    )
    @Parameter(
            name = "attributes",
            description = """
                    Required attribute key-value pairs encoded as `key:value`.
                    Repeat the parameter for multiple pairs, e.g.
                    `?attributes=status:active&attributes=category:contract`.
                    Only documents whose `attributes` map contains all the given pairs are returned.""",
            example = "status:active"
    )
    @GetMapping("/documents")
    public ObjectResponse<DocumentResponse> fetchDocuments(
            @NotBlank @Size(max = 255) @RequestParam String userId,
            @Size(max = 255) @RequestParam(required = false) String documentId,
            @Size(max = 255) @RequestParam(required = false) String documentType,
            @RequestParam(required = false) List<String> attributes) {

        logger.info("", action("fetchDocuments"), stateInitiated(), kv("userId", userId), kv("documentId", documentId), kv("documentType", documentType), kv("attributes", attributes));
        final var request = DocumentService.DocumentsRequest.builder()
                .userId(userId)
                .documentId(documentId)
                .documentType(documentType)
                .attributes(parseAttributes(attributes))
                .build();
        final DocumentResponse documents = documentService.fetchDocuments(request);
        logger.info("", action("fetchDocuments"), stateSucceeded(), kv("count", documents.documents().size()));
        return new ObjectResponse<>(documents);
    }

    /**
     * Parse attribute filter pairs in the form {@code key:value} into a map.
     *
     * @param attributes list of {@code key:value} pairs; may be {@code null} or empty
     * @return parsed map of attribute filters, or empty when no entries are provided
     */
    private static Map<String, String> parseAttributes(final List<String> attributes) {
        if (attributes == null) {
            return Map.of();
        }
        return attributes.stream()
                .filter(StringUtils::hasText)
                .map(DocumentController::parseAttribute)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));
    }

    private static Map.Entry<String, String> parseAttribute(final String entry) {
        final int separator = entry.indexOf(':');
        if (separator <= 0) {
            throw new IllegalArgumentException("Invalid attribute filter '%s', expected format 'key:value'".formatted(entry));
        }
        return Map.entry(entry.substring(0, separator), entry.substring(separator + 1));
    }

    /**
     * Create a document for the given user.
     *
     * @param request Document create request
     * @return user documents
     */
    @Operation(
            summary = "Create a document",
            description = "Create a documents for the given user."
    )
    @PostMapping("/admin/documents")
    public ObjectResponse<DocumentCreateResponse> createDocument(@Valid @RequestBody final ObjectRequest<DocumentCreateRequest> request) {
        logger.info("", action("createDocument"), stateInitiated(), kv("userId", request.getRequestObject().userId()));
        final DocumentCreateResponse response = documentService.createDocument(request.getRequestObject());
        logger.info("", action("createDocument"), stateSucceeded(), kv("userId", request.getRequestObject().userId()));
        return new ObjectResponse<>(response);
    }

    /**
     * Update a document for the given user.
     *
     * @param documentId Document identifier
     * @param request Document update request
     * @return user documents
     */
    @Operation(
            summary = "Update a document",
            description = "Update a document for the given user."
    )
    @PutMapping("/admin/documents/{documentId}")
    public Response updateDocument(@NotBlank @Size(max = 36) @PathVariable("documentId") String documentId, @Valid @RequestBody final ObjectRequest<DocumentUpdateRequest> request) {
        logger.info("", action("updateDocument"), stateInitiated(), kv("userId", request.getRequestObject().userId()), kv("documentId", documentId));
        documentService.updateDocument(documentId, request.getRequestObject());
        logger.info("", action("updateDocument"), stateSucceeded(), kv("userId", request.getRequestObject().userId()), kv("documentId", documentId));
        return new Response();
    }

    /**
     * Delete documents for the given user.
     *
     * @param userId user identifier
     * @param documentId document identifier
     * @return response
     */
    @Operation(
            summary = "Delete documents",
            description = "Delete documents for the given user."
    )
    @DeleteMapping("/admin/documents")
    public Response deleteDocuments(@NotBlank @Size(max = 255) @RequestParam String userId, @Size(max = 255) @RequestParam(required = false) String documentId) {
        logger.info("", action("deleteDocuments"), stateInitiated(), kv("userId", userId), kv("documentId", documentId));
        documentService.deleteDocuments(userId, Optional.ofNullable(documentId));
        logger.info("", action("deleteDocuments"), stateSucceeded(), kv("userId", userId), kv("documentId", documentId));
        return new Response();
    }

}
