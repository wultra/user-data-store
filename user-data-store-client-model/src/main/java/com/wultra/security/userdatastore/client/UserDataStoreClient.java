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
import com.wultra.security.userdatastore.client.model.request.*;
import com.wultra.security.userdatastore.client.model.response.*;

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
    DocumentResponse fetchDocuments(String userId, String documentId) throws UserDataStoreClientException;

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
     * @param documentId Document identifier.
     * @param request Document update request.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void updateDocument(String documentId, DocumentUpdateRequest request) throws UserDataStoreClientException;

    /**
     * Delete documents.
     *
     * @param userId     User identifier.
     * @param documentId Optional document identifier.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void deleteDocuments(String userId, String documentId) throws UserDataStoreClientException;

    /**
     * Fetch photos.
     *
     * @param userId     User identifier.
     * @param documentId Document identifier.
     * @return Photo response.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    PhotoResponse fetchPhotos(String userId, String documentId) throws UserDataStoreClientException;

    /**
     * Create a photo.
     *
     * @param request Photo create request.
     * @return Photo create response.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    PhotoCreateResponse createPhoto(PhotoCreateRequest request) throws UserDataStoreClientException;

    /**
     * Update a photo.
     *
     * @param photoId Photo identifier.
     * @param request Photo update request.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void updatePhoto(String photoId, PhotoUpdateRequest request) throws UserDataStoreClientException;

    /**
     * Delete photos.
     *
     * @param userId     User identifier.
     * @param documentId Optional document identifier.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void deletePhotos(String userId, String documentId) throws UserDataStoreClientException;

    /**
     * Import photos.
     *
     * @param request Photo import request.
     * @return Photo import response.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    PhotosImportResponse importPhotos(PhotosImportRequest request) throws UserDataStoreClientException;

    /**
     * Import photos from CSV files.
     *
     * @param request Photo import from CSV files request.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void importPhotosCsv(PhotosImportCsvRequest request) throws UserDataStoreClientException;

    /**
     * Fetch attachments.
     *
     * @param userId     User identifier.
     * @param documentId Optional document identifier.
     * @return Attachment response.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    AttachmentResponse fetchAttachments(String userId, String documentId) throws UserDataStoreClientException;

    /**
     * Create an attachment.
     *
     * @param request Attachment create request.
     * @return Attachment create response.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    AttachmentCreateResponse createAttachment(AttachmentCreateRequest request) throws UserDataStoreClientException;

    /**
     * Update an attachment.
     *
     * @param attachmentId Attachment identifier.
     * @param request Attachment update request.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void updateAttachment(String attachmentId, AttachmentUpdateRequest request) throws UserDataStoreClientException;

    /**
     * Delete attachments.
     *
     * @param userId     User identifier.
     * @param documentId Optional document identifier.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void deleteAttachments(String userId, String documentId) throws UserDataStoreClientException;

    /**
     * Fetch user claims.
     * @param userId User identifier.
     * @return User claims.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    @Deprecated
    Object fetchUserClaims(String userId) throws UserDataStoreClientException;

    /**
     * Store user claims.
     * @param userId User identifier.
     * @param claims User claims.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    @Deprecated
    void storeUserClaims(String userId, Object claims) throws UserDataStoreClientException;

    /**
     * Delete user claims.
     * @param userId User identifier.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    @Deprecated
    void deleteUserClaims(String userId) throws UserDataStoreClientException;

    /**
     * Fetch claim(s).
     * @param userId User identifier.
     * @param claim Optional claim to filter by claim name.
     * @return User claims.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    Object fetchClaims(String userId, String claim) throws UserDataStoreClientException;

    /**
     * Create claims.
     * @param userId User identifier.
     * @param value Claims.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void createClaims(String userId, Object value) throws UserDataStoreClientException;

    /**
     * Create claims.
     * @param userId User identifier.
     * @param value Claims.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void updateClaims(String userId, Object value) throws UserDataStoreClientException;

    /**
     * Delete claim(s).
     * @param userId User identifier.
     * @param claim Optional claim to delete.
     * @throws UserDataStoreClientException Thrown in case REST API call fails.
     */
    void deleteClaims(String userId, String claim) throws UserDataStoreClientException;
}

