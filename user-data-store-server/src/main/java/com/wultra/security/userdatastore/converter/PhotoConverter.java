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
package com.wultra.security.userdatastore.converter;

import com.wultra.security.userdatastore.client.model.dto.PhotoDto;
import com.wultra.security.userdatastore.client.model.dto.PhotoImportDto;
import com.wultra.security.userdatastore.client.model.dto.PhotoImportResultDto;
import com.wultra.security.userdatastore.client.model.request.EmbeddedPhotoImportRequest;
import com.wultra.security.userdatastore.client.model.response.EmbeddedPhotoImportResponse;
import com.wultra.security.userdatastore.model.entity.PhotoEntity;
import com.wultra.security.userdatastore.service.EncryptionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Converter for photos.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
@AllArgsConstructor
public class PhotoConverter {

    private final EncryptionService encryptionService;

    /**
     * Convert {@link PhotoEntity} to {@link PhotoDto}.
     * @param entity Photo entity.
     * @return Photo DTO.
     */
    public PhotoDto toPhoto(final PhotoEntity entity) {
        if (entity == null) {
            return null;
        }

        return PhotoDto.builder()
                .id(entity.getId())
                .documentId(entity.getDocument().getId())
                .photoData(encryptionService.decryptPhoto(entity))
                .photoType(entity.getPhotoType())
                .externalId(entity.getExternalId())
                .timestampCreated(entity.getTimestampCreated())
                .timestampLastUpdated(entity.getTimestampLastUpdated())
                .build();
    }

    public PhotoImportDto toPhotoImport(final EmbeddedPhotoImportRequest photo) {
        return PhotoImportDto.builder()
                .userId(photo.userId())
                .photoDataType(photo.photoDataType())
                .photoType(photo.photoType())
                .photoData(photo.photoData())
                .build();
    }

    public EmbeddedPhotoImportResponse toPhotoImportResponse(final PhotoImportResultDto result) {
        return EmbeddedPhotoImportResponse.builder()
                .userId(result.userId())
                .photoType(result.photoType())
                .documentId(result.documentId())
                .photoId(result.photoId())
                .imported(result.imported())
                .error(result.error())
                .build();
    }

}
