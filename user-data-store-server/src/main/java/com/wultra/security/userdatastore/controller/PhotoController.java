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

import com.wultra.security.userdatastore.client.model.dto.PhotoDto;
import com.wultra.security.userdatastore.client.model.request.PhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.response.PhotoCreateResponse;
import com.wultra.security.userdatastore.client.model.response.PhotoResponse;
import com.wultra.security.userdatastore.service.PhotoService;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller providing API for CRUD for photos.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@Validated
@Slf4j
class PhotoController {

    private final PhotoService photoService;

    PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }


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
    public ObjectResponse<PhotoResponse> userPhotos(@NotBlank @Size(max = 255) @RequestParam String userId, @NotBlank @Size(max = 255) @RequestParam String documentId) {
        logger.info("Fetching photos for document ID: {}", documentId);
        final List<PhotoDto> photos = photoService.fetchPhotos(userId, documentId);
        final PhotoResponse response = new PhotoResponse();
        response.addAll(photos);
        return new ObjectResponse<>(response);
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
    public ObjectResponse<PhotoCreateResponse> createPhoto(@RequestBody final PhotoCreateRequest request) {
        logger.info("Creating photo for user ID: {}", request.userId());
        final PhotoCreateResponse response = photoService.createPhoto(request);
        return new ObjectResponse<>(response);
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
    public Response deletePhotos(@NotBlank @Size(max = 255) @RequestParam String userId, @NotBlank @Size(max = 255) @RequestParam(required = false) String documentId) {
        logger.info("Deleting photos for document ID: {}", documentId);
        photoService.deletePhotos(userId, documentId);
        return new Response();
    }

}
