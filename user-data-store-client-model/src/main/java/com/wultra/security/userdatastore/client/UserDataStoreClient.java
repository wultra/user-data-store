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
package com.wultra.security.userdatastore.client;

import com.wultra.security.userdatastore.client.model.request.AttachmentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentUpdateRequest;
import com.wultra.security.userdatastore.client.model.request.PhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.response.*;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User Data Store client interface.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public interface UserDataStoreClient {

        DocumentResponse fetchDocuments(String userId, String documentId);

        DocumentCreateResponse createDocument(DocumentCreateRequest request);

        void updateDocument(DocumentUpdateRequest request);

        void deleteDocuments(String userId, String documentId);

        PhotoResponse fetchPhotos(String userId, String documentId);

        PhotoCreateResponse createPhoto(PhotoCreateRequest request);

        void deletePhotos(String userId, String documentId);

        AttachmentResponse fetchAttachments(String userId, String documentId);

        AttachmentCreateResponse createAttachment(AttachmentCreateRequest request);

        void deleteAttachments(String userId, String documentId);

}

