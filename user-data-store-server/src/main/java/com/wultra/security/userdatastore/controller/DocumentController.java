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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     *                   e.g. {@code ?attributes=firstName:Alice&attributes=lastName:Adams}
     * @return user documents
     */
    @Operation(
            summary = "Return documents",
            description = "Return documents for the given user, optionally filtered by document ID, document type, " +
                    "and required attribute key-value pairs."
    )
    @GetMapping("/documents")
    public ObjectResponse<DocumentResponse> fetchDocuments(
            @NotBlank @Size(max = 255) @RequestParam String userId,
            @Size(max = 255) @RequestParam(required = false) String documentId,
            @Size(max = 255) @RequestParam(required = false) String documentType,
            @Parameter(
                    description = "Required attribute key-value pairs encoded as `key:value`. " +
                            "Repeat the parameter for multiple pairs, e.g. " +
                            "`?attributes=firstName:Alice&attributes=lastName:Adams`. " +
                            "Only documents whose `attributes` map contains all the given pairs are returned.",
                    example = "firstName:Alice"
            )
            @RequestParam(required = false) List<String> attributes) {
        logger.info("action: fetchDocuments, state: initiated, userId: {}, documentId: {}, documentType: {}, attributes: {}", userId, documentId, documentType, attributes);
        final Map<String, String> attributeFilter = parseAttributes(attributes);
        final DocumentResponse documents = documentService.fetchDocuments(userId, documentId, documentType, attributeFilter);
        logger.info("action: fetchDocuments, state: succeeded");
        return new ObjectResponse<>(documents);
    }

    /**
     * Parse attribute filter pairs in the form {@code key:value} into a map.
     *
     * @param attributes list of {@code key:value} pairs; may be {@code null} or empty
     * @return parsed map of attribute filters, or empty when no entries are provided
     */
    private static Map<String, String> parseAttributes(final List<String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }
        final Map<String, String> result = new LinkedHashMap<>();
        for (final String entry : attributes) {
            if (entry == null || entry.isEmpty()) {
                continue;
            }
            final int separator = entry.indexOf(':');
            if (separator <= 0) {
                throw new IllegalArgumentException("Invalid attribute filter '%s', expected format 'key:value'".formatted(entry));
            }
            result.put(entry.substring(0, separator), entry.substring(separator + 1));
        }
        return result;
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
        logger.info("action: createDocument, state: initiated, userId: {}", request.getRequestObject().userId());
        final DocumentCreateResponse response = documentService.createDocument(request.getRequestObject());
        logger.info("action: createDocument, state: succeeded, userId: {}", request.getRequestObject().userId());
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
        logger.info("action: updateDocument, state: initiated, userId: {}, documentId: {}", request.getRequestObject().userId(), documentId);
        documentService.updateDocument(documentId, request.getRequestObject());
        logger.info("action: updateDocument, state: succeeded, userId: {}, documentId: {}", request.getRequestObject().userId(), documentId);
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
        logger.info("action: deleteDocuments, state: initiated, userId: {}, documentId: {}", userId, documentId);
        documentService.deleteDocuments(userId, Optional.ofNullable(documentId));
        logger.info("action: deleteDocuments, state: succeeded, userId: {}, documentId: {}", userId, documentId);
        return new Response();
    }

}
