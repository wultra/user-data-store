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

import com.wultra.security.userdatastore.client.model.dto.DocumentDto;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentUpdateRequest;
import com.wultra.security.userdatastore.client.model.response.DocumentCreateResponse;
import com.wultra.security.userdatastore.client.model.response.DocumentResponse;
import com.wultra.security.userdatastore.service.DocumentService;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller providing API for CRUD for user documents.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@Validated
@Slf4j
class DocumentController {

    private final DocumentService documentService;

    DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Return documents for the given user.
     *
     * @param userId user identifier
     * @param documentId optional document identifier
     * @return user documents
     */
    @Operation(
            summary = "Return documents",
            description = "Return documents for the given user."
    )
    @GetMapping("/documents")
    public ObjectResponse<DocumentResponse> fetchDocuments(@NotBlank @Size(max = 255) @RequestParam String userId, @Size(max = 255) @RequestParam(required = false) String documentId) {
        logger.info("Fetching documents; user ID: {}, document ID: {}", userId, documentId);
        final List<DocumentDto> documents = documentService.fetchDocuments(userId, Optional.ofNullable(documentId));
        final DocumentResponse response = new DocumentResponse();
        response.addAll(documents);
        return new ObjectResponse<>(response);
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
    public ObjectResponse<DocumentCreateResponse> createDocument(@RequestBody final DocumentCreateRequest request) {
        logger.info("Creating document for user ID: {}", request.userId());
        final DocumentCreateResponse response = documentService.createDocument(request);
        return new ObjectResponse<>(response);
    }

    /**
     * Update a document for the given user.
     *
     * @param request Document update request
     * @return user documents
     */
    @Operation(
            summary = "Update a document",
            description = "Update a document for the given user."
    )
    @PutMapping("/admin/documents")
    public Response updateDocument(@RequestBody final DocumentUpdateRequest request) {
        logger.info("Updating document for user ID: {}", request.userId());
        documentService.updateDocument(request);
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
        logger.info("Deleting documents; user ID: {}, document ID: {}", userId, documentId);
        documentService.deleteDocuments(userId, Optional.ofNullable(documentId));
        return new Response();
    }

}
