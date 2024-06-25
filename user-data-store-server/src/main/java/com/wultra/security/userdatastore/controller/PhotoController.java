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

import com.wultra.security.userdatastore.client.model.request.PhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.request.PhotoUpdateRequest;
import com.wultra.security.userdatastore.client.model.response.PhotoCreateResponse;
import com.wultra.security.userdatastore.client.model.response.PhotoResponse;
import com.wultra.security.userdatastore.model.validator.PhotoRequestValidator;
import com.wultra.security.userdatastore.service.PhotoService;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller providing API for CRUD for photos.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@Validated
@Slf4j
@AllArgsConstructor
class PhotoController {

    private final PhotoService photoService;
    private final PhotoRequestValidator validator = new PhotoRequestValidator();

    /**
     * Return photos for the given user and document.
     *
     * @param userId user identifier
     * @param documentId document identifier
     * @return photos
     */
    @Operation(
            summary = "Return photos",
            description = "Return photos for the given user and document."
    )
    @GetMapping("/photos")
    public ObjectResponse<PhotoResponse> fetchPhotos(@NotBlank @Size(max = 255) @RequestParam String userId, @NotBlank @Size(max = 255) @RequestParam String documentId) {
        logger.info("action: fetchPhotos, state: initiated, userId: {}, documentId: {}", userId, documentId);
        final PhotoResponse photos = photoService.fetchPhotos(userId, Optional.ofNullable(documentId));
        logger.info("action: fetchPhotos, state: succeeded, userId: {}, documentId: {}", userId, documentId);
        return new ObjectResponse<>(photos);
    }

    /**
     * Create a photo for the given user and document.
     *
     * @param request Photo create request
     * @return photo create response
     */
    @Operation(
            summary = "Create a photo",
            description = "Create a photo for the given user and document."
    )
    @PostMapping("/admin/photos")
    public ObjectResponse<PhotoCreateResponse> createPhoto(@Valid @RequestBody final ObjectRequest<PhotoCreateRequest> request) {
        logger.info("action: createPhoto, state: initiated, userId: {}, documentId: {}", request.getRequestObject().userId(), request.getRequestObject().documentId());
        validator.validateRequest(request.getRequestObject());
        final PhotoCreateResponse response = photoService.createPhoto(request.getRequestObject());
        logger.info("action: createPhoto, state: succeeded, userId: {}, documentId: {}", request.getRequestObject().userId(), request.getRequestObject().documentId());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a photo.
     *
     * @param request Update photo request
     * @return response
     */
    @Operation(
            summary = "Update a photo",
            description = "Update a photo."
    )
    @PutMapping("/admin/photos/{photoId}")
    public Response updatePhoto(@NotBlank @Size(max = 36) @PathVariable("photoId") String photoId, @Valid @RequestBody final ObjectRequest<PhotoUpdateRequest> request) {
        logger.info("action: createPhoto, state: initiated, photoId: {}", photoId);
        validator.validateRequest(request.getRequestObject());
        photoService.updatePhoto(photoId, request.getRequestObject());
        logger.info("action: createPhoto, state: succeeded, photoId: {}", photoId);
        return new Response();
    }

    /**
     * Delete photos for the given user and document.
     *
     * @param userId user identifier
     * @param documentId document identifier
     * @return response
     */
    @Operation(
            summary = "Delete photos",
            description = "Delete photos for the given user and document."
    )
    @DeleteMapping("/admin/photos")
    public Response deletePhotos(@NotBlank @Size(max = 255) @RequestParam String userId, @Size(max = 255) @RequestParam(required = false) String documentId) {
        logger.info("action: deletePhotos, state: initiated, userId: {}, documentId: {}", userId, documentId);
        photoService.deletePhotos(userId, Optional.ofNullable(documentId));
        logger.info("action: deletePhotos, state: succeeded, userId: {}, documentId: {}", userId, documentId);
        return new Response();
    }

}
