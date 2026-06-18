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
import com.wultra.security.userdatastore.client.model.request.AttachmentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.AttachmentUpdateRequest;
import com.wultra.security.userdatastore.client.model.response.AttachmentCreateResponse;
import com.wultra.security.userdatastore.client.model.response.AttachmentResponse;
import com.wultra.security.userdatastore.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.wultra.security.userdatastore.logging.StructuredLogging.action;
import static com.wultra.security.userdatastore.logging.StructuredLogging.kv;
import static com.wultra.security.userdatastore.logging.StructuredLogging.stateInitiated;
import static com.wultra.security.userdatastore.logging.StructuredLogging.stateSucceeded;

/**
 * REST controller providing API for CRUD for attachments.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@Validated
@Slf4j
@AllArgsConstructor
class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * Return attachments for the given user.
     *
     * @param userId user identifier
     * @param documentId document identifier
     * @return attachments
     */
    @Operation(
            summary = "Return attachments",
            description = "Return attachments for the given user and document."
    )
    @GetMapping("/attachments")
    public ObjectResponse<AttachmentResponse> fetchAttachments(@NotBlank @Size(max = 255) @RequestParam String userId, @NotBlank @Size(max = 255) @RequestParam String documentId) {
        logger.info("Fetch attachments initiated", action("fetchAttachments"), stateInitiated(), kv("userId", userId), kv("documentId", documentId));
        final AttachmentResponse attachments = attachmentService.fetchAttachments(userId, Optional.ofNullable(documentId));
        logger.info("Fetch attachments succeeded", action("fetchAttachments"), stateSucceeded(), kv("userId", userId), kv("documentId", documentId));
        return new ObjectResponse<>(attachments);
    }

    /**
     * Create an attachment for the given user and document.
     *
     * @param request Create attachment request
     * @return attachment create response
     */
    @Operation(
            summary = "Create an attachment",
            description = "Create an attachment for the given user and document."
    )
    @PostMapping("/admin/attachments")
    public ObjectResponse<AttachmentCreateResponse> createAttachment(@Valid @RequestBody final ObjectRequest<AttachmentCreateRequest> request) {
        logger.info("Create attachment initiated", action("createAttachment"), stateInitiated(), kv("userId", request.getRequestObject().userId()));
        final AttachmentCreateResponse response = attachmentService.createAttachment(request.getRequestObject());
        logger.info("Create attachment succeeded", action("createAttachment"), stateSucceeded(), kv("userId", request.getRequestObject().userId()));
        return new ObjectResponse<>(response);
    }

    /**
     * Update an attachment.
     *
     * @param request Update attachment request
     * @return response
     */
    @Operation(
            summary = "Update an attachment",
            description = "Update an attachment."
    )
    @PutMapping("/admin/attachments/{attachmentId}")
    public Response updateAttachment(@NotBlank @Size(max = 36) @PathVariable("attachmentId") String attachmentId, @Valid @RequestBody final ObjectRequest<AttachmentUpdateRequest> request) {
        logger.info("Update attachment initiated", action("updateAttachment"), stateInitiated(), kv("attachmentId", attachmentId));
        attachmentService.updateAttachment(attachmentId, request.getRequestObject());
        logger.info("Update attachment succeeded", action("updateAttachment"), stateSucceeded(), kv("attachmentId", attachmentId));
        return new Response();
    }

    /**
     * Delete attachments for the given user and document.
     *
     * @param userId user identifier
     * @param documentId document identifier
     * @return response
     */
    @Operation(
            summary = "Delete attachments",
            description = "Delete attachments for the given user and document."
    )
    @DeleteMapping("/admin/attachments")
    public Response deleteAttachments(@NotBlank @Size(max = 255) @RequestParam String userId, @Size(max = 255) @RequestParam(required = false) String documentId) {
        logger.info("Delete attachments initiated", action("deleteAttachments"), stateInitiated(), kv("userId", userId), kv("documentId", documentId));
        attachmentService.deleteAttachments(userId, Optional.ofNullable(documentId));
        logger.info("Delete attachments succeeded", action("deleteAttachments"), stateSucceeded(), kv("userId", userId), kv("documentId", documentId));
        return new Response();
    }

}
