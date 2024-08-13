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
package com.wultra.security.userdatastore.client.model.request;

import com.wultra.security.userdatastore.client.model.validation.constraints.Base64;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Request class for import of photos.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Builder
@Jacksonized
public record EmbeddedPhotoImportRequest(

        @NotBlank @Size(max = 255)
        String userId,
        @NotBlank @Size(max = 32)
        String photoDataType,
        @NotBlank @Size(max = 32)
        String photoType,
        @NotBlank
        String photoData

) { }
