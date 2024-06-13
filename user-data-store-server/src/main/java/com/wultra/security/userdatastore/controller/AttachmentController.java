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

import com.wultra.security.userdatastore.client.model.request.AttachmentCreateRequest;
import com.wultra.security.userdatastore.client.model.response.AttachmentCreateResponse;
import com.wultra.security.userdatastore.client.model.response.AttachmentResponse;
import com.wultra.security.userdatastore.service.AttachmentService;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller providing API for CRUD for attachments.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@Validated
@Slf4j
class AttachmentController {

    private final AttachmentService attachmentService;

    @Autowired
    AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

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
        logger.info("Fetching photos for document ID: {}", documentId);
        final AttachmentResponse attachments = attachmentService.fetchAttachments(userId, Optional.ofNullable(documentId));
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
    public ObjectResponse<AttachmentCreateResponse> createAttachment(@RequestBody final ObjectRequest<AttachmentCreateRequest> request) {
        logger.info("Creating attachment for user ID: {}", request.getRequestObject().userId());
        final AttachmentCreateResponse response = attachmentService.createAttachment(request.getRequestObject());
        return new ObjectResponse<>(response);
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
        logger.info("Deleting attachments for document ID: {}", documentId);
        attachmentService.deleteAttachments(userId, Optional.ofNullable(documentId));
        return new Response();
    }

}
