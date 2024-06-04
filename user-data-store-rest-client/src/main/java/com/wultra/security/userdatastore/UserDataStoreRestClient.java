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
package com.wultra.security.userdatastore;

import com.wultra.security.userdatastore.client.UserDataStoreClient;
import com.wultra.security.userdatastore.client.model.request.AttachmentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentUpdateRequest;
import com.wultra.security.userdatastore.client.model.request.PhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.response.*;

/**
 * Class implementing a User Data Store REST client.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 *
 */
public class UserDataStoreRestClient implements UserDataStoreClient {


    @Override
    public DocumentResponse fetchDocuments(String userId, String documentId) {
        return null;
    }

    @Override
    public DocumentCreateResponse createDocument(DocumentCreateRequest request) {
        return null;
    }

    @Override
    public void updateDocument(DocumentUpdateRequest request) {

    }

    @Override
    public void deleteDocuments(String userId, String documentId) {

    }

    @Override
    public PhotoResponse fetchPhotos(String userId, String documentId) {
        return null;
    }

    @Override
    public PhotoCreateResponse createPhoto(PhotoCreateRequest request) {
        return null;
    }

    @Override
    public void deletePhotos(String userId, String documentId) {

    }

    @Override
    public AttachmentResponse fetchAttachments(String userId, String documentId) {
        return null;
    }

    @Override
    public AttachmentCreateResponse createAttachment(AttachmentCreateRequest request) {
        return null;
    }

    @Override
    public void deleteAttachments(String userId, String documentId) {

    }
}
