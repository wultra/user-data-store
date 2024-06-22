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
package com.wultra.security.userdatastore.model.validator;

import com.wultra.security.userdatastore.client.model.request.EmbeddedPhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.request.PhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.request.PhotoUpdateRequest;
import com.wultra.security.userdatastore.model.error.RequestValidationException;

import java.util.Base64;

/**
 * Validator for photo requests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class PhotoRequestValidator {

    public void validateRequest(final PhotoCreateRequest request) {
        validateEncoding(request.photoData());
    }

    public void validateRequest(final PhotoUpdateRequest request) {
        validateEncoding(request.photoData());
    }

    public void validateRequest(final EmbeddedPhotoCreateRequest request) {
        validateEncoding(request.photoData());
    }

    private void validateEncoding(final String photoData) {
        try {
            Base64.getDecoder().decode(photoData);
        } catch (IllegalArgumentException e) {
            throw new RequestValidationException(e.getMessage(), e);
        }
    }
}
