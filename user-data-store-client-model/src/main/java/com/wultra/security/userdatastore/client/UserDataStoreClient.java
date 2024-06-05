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

import com.wultra.security.userdatastore.client.model.error.UserDataStoreClientException;
import com.wultra.security.userdatastore.client.model.request.AttachmentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentUpdateRequest;
import com.wultra.security.userdatastore.client.model.request.PhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.response.*;

import java.util.Optional;

/**
 * User Data Store client interface.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public interface UserDataStoreClient {

    /**
     * Fetch documents.
     *
     * @param userId     User identifier.
     * @param documentId Optional document identifier.
     * @return Documents.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    DocumentResponse fetchDocuments(String userId, Optional<String> documentId) throws UserDataStoreClientException;

    /**
     * Create a document.
     *
     * @param request Document create request.
     * @return Document create response.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    DocumentCreateResponse createDocument(DocumentCreateRequest request) throws UserDataStoreClientException;

    /**
     * Update a document.
     *
     * @param request Document update request.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void updateDocument(DocumentUpdateRequest request) throws UserDataStoreClientException;

    /**
     * Delete documents.
     *
     * @param userId     User identifier.
     * @param documentId Optional document identifier.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void deleteDocuments(String userId, Optional<String> documentId) throws UserDataStoreClientException;

    /**
     * Fetch photos.
     *
     * @param userId     User identifier.
     * @param documentId Document identifier.
     * @return Photo response.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    PhotoResponse fetchPhotos(String userId, Optional<String> documentId) throws UserDataStoreClientException;

    /**
     * Create a photo.
     *
     * @param request Photo create request.
     * @return Photo create response.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    PhotoCreateResponse createPhoto(PhotoCreateRequest request) throws UserDataStoreClientException;

    /**
     * Delete photos.
     *
     * @param userId     User identifier.
     * @param documentId Optional document identifier.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void deletePhotos(String userId, Optional<String> documentId) throws UserDataStoreClientException;

    /**
     * Fetch attachments.
     *
     * @param userId     User identifier.
     * @param documentId Optional document identifier.
     * @return Attachment response.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    AttachmentResponse fetchAttachments(String userId, Optional<String> documentId) throws UserDataStoreClientException;

    /**
     * Create an attachment.
     *
     * @param request Attachment create request.
     * @return Attachment create response.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    AttachmentCreateResponse createAttachment(AttachmentCreateRequest request) throws UserDataStoreClientException;

    /**
     * Delete attachments.
     *
     * @param userId     User identifier.
     * @param documentId Optional document identifier.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void deleteAttachments(String userId, Optional<String> documentId) throws UserDataStoreClientException;

}

