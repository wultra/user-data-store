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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.core.rest.client.base.DefaultRestClient;
import com.wultra.core.rest.client.base.RestClient;
import com.wultra.core.rest.client.base.RestClientConfiguration;
import com.wultra.core.rest.client.base.RestClientException;
import com.wultra.security.userdatastore.client.UserDataStoreClient;
import com.wultra.security.userdatastore.client.model.error.UserDataStoreClientException;
import com.wultra.security.userdatastore.client.model.request.*;
import com.wultra.security.userdatastore.client.model.response.*;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

/**
 * Class implementing a User Data Store REST client.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 *
 */
@Slf4j
public class UserDataStoreRestClient implements UserDataStoreClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final MultiValueMap<String, String> EMPTY_MULTI_MAP = new LinkedMultiValueMap<>();

    /**
     * PowerAuth REST client constructor.
     *
     * @param baseUrl BASE URL of REST endpoints.
     */
    public UserDataStoreRestClient(String baseUrl) throws UserDataStoreClientException {
        final RestClientConfiguration config = new RestClientConfiguration();
        config.setBaseUrl(baseUrl);
        try {
            restClient = new DefaultRestClient(config);
        } catch (RestClientException ex) {
            throw new UserDataStoreClientException("REST client initialization failed, error: " + ex.getMessage(), ex);
        }
    }

    /**
     * PowerAuth REST client constructor.
     *
     * @param config REST client configuration.
     */
    public UserDataStoreRestClient(RestClientConfiguration config) throws UserDataStoreClientException {
        try {
            restClient = new DefaultRestClient(config);
        } catch (RestClientException ex) {
            throw new UserDataStoreClientException("REST client initialization failed, error: " + ex.getMessage(), ex);
        }
    }

    /**
     * Call the User Data Store REST API using HTTP GET.
     *
     * @param path Path of the endpoint.
     * @param queryParams HTTP query parameters.
     * @param httpHeaders HTTP headers.
     * @param responseType Response type.
     * @return Response.
     */
    private <T> T get(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> httpHeaders, Class<T> responseType) throws UserDataStoreClientException {
        try {
            final ObjectResponse<T> objectResponse = restClient.getObject(path, queryParams, httpHeaders, responseType);
            return objectResponse.getResponseObject();
        } catch (RestClientException ex) {
            handleException(ex);
        }
        return null;
    }

    /**
     * Call the User Data Store REST API using HTTP POST.
     *
     * @param path Path of the endpoint.
     * @param request Request object.
     * @param queryParams HTTP query parameters.
     * @param httpHeaders HTTP headers.
     * @param responseType Response type.
     * @return Response.
     */
    private <T> T post(String path, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> httpHeaders, Class<T> responseType) throws UserDataStoreClientException {
        final ObjectRequest<?> objectRequest = new ObjectRequest<>(request);
        try {
            final ObjectResponse<T> objectResponse = restClient.postObject(path, objectRequest, queryParams, httpHeaders, responseType);
            return objectResponse.getResponseObject();
        } catch (RestClientException ex) {
            handleException(ex);
        }
        return null;
    }

    /**
     * Call the User Data Store REST API using HTTP PUT.
     *
     * @param path Path of the endpoint.
     * @param request Request object.
     * @param queryParams HTTP query parameters.
     * @param httpHeaders HTTP headers.
     * @param responseType Response type.
     * @return Response.
     */
    private <T> T put(String path, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> httpHeaders, Class<T> responseType) throws UserDataStoreClientException {
        final ObjectRequest<?> objectRequest = new ObjectRequest<>(request);
        try {
            final ObjectResponse<T> objectResponse = restClient.putObject(path, objectRequest, queryParams, httpHeaders, responseType);
            return objectResponse.getResponseObject();
        } catch (RestClientException ex) {
            handleException(ex);
        }
        return null;
    }

    /**
     * Call the User Data Store REST API using HTTP DELETE.
     *
     * @param path Path of the endpoint.
     * @param queryParams HTTP query parameters.
     * @param httpHeaders HTTP headers.
     * @param responseType Response type.
     * @return Response.
     */
    private <T> T delete(String path, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> httpHeaders, Class<T> responseType) throws UserDataStoreClientException {
        try {
            final ObjectResponse<T> objectResponse = restClient.deleteObject(path, queryParams, httpHeaders, responseType);
            return objectResponse.getResponseObject();
        } catch (RestClientException ex) {
            handleException(ex);
        }
        return null;
    }

    private void handleException(RestClientException ex) throws UserDataStoreClientException {
        if (ex.getStatusCode() == null) {
            // Logging for network errors when port is closed
            logger.warn("User Data Store service is not accessible, error: {}", ex.getMessage());
            logger.debug(ex.getMessage(), ex);
        } else if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            // Logging for 404 errors
            logger.warn("User Data Store service is not available, error: {}", ex.getMessage());
            logger.debug(ex.getMessage(), ex);
        }
        // Error handling for other HTTP errors
        throw new UserDataStoreClientException(ex.getMessage(), ex);
    }

    @Override
    public DocumentResponse fetchDocuments(String userId, String documentId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        if (documentId != null) {
            queryParams.put("documentId", Collections.singletonList(documentId));
        }
        return get("/documents", queryParams, EMPTY_MULTI_MAP, DocumentResponse.class);
    }

    @Override
    public DocumentCreateResponse createDocument(DocumentCreateRequest request) throws UserDataStoreClientException {
        return post("/admin/documents", request, EMPTY_MULTI_MAP, EMPTY_MULTI_MAP, DocumentCreateResponse.class);
    }

    @Override
    public void updateDocument(String documentId, DocumentUpdateRequest request) throws UserDataStoreClientException {
        put("/admin/documents/" + documentId, request, EMPTY_MULTI_MAP, EMPTY_MULTI_MAP, Response.class);
    }

    @Override
    public void deleteDocuments(String userId, String documentId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        if (documentId != null) {
            queryParams.put("documentId", Collections.singletonList(documentId));
        }
        delete("/admin/documents", queryParams, EMPTY_MULTI_MAP, Response.class);
    }

    @Override
    public PhotoResponse fetchPhotos(String userId, String documentId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        if (documentId != null) {
            queryParams.put("documentId", Collections.singletonList(documentId));
        }
        return get("/photos", queryParams, EMPTY_MULTI_MAP, PhotoResponse.class);
    }

    @Override
    public PhotoCreateResponse createPhoto(PhotoCreateRequest request) throws UserDataStoreClientException {
        return post("/admin/photos", request, EMPTY_MULTI_MAP, EMPTY_MULTI_MAP, PhotoCreateResponse.class);
    }

    @Override
    public void updatePhoto(String photoId, PhotoUpdateRequest request) throws UserDataStoreClientException {
        put("/admin/photos/" + photoId, request, EMPTY_MULTI_MAP, EMPTY_MULTI_MAP, Response.class);
    }

    @Override
    public void deletePhotos(String userId, String documentId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        if (documentId != null) {
            queryParams.put("documentId", Collections.singletonList(documentId));
        }
        delete("/admin/photos", queryParams, EMPTY_MULTI_MAP, Response.class);
    }

    @Override
    public PhotosImportResponse importPhotos(PhotosImportRequest request) throws UserDataStoreClientException {
        return post("/admin/photos/import", request, EMPTY_MULTI_MAP, EMPTY_MULTI_MAP, PhotosImportResponse.class);
    }

    @Override
    public void importPhotosCsv(PhotosImportCsvRequest request) throws UserDataStoreClientException {
        post("/admin/photos/import/csv", request, EMPTY_MULTI_MAP, EMPTY_MULTI_MAP, Response.class);
    }

    @Override
    public AttachmentResponse fetchAttachments(String userId, String documentId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        if (documentId != null) {
            queryParams.put("documentId", Collections.singletonList(documentId));
        }
        return get("/attachments", queryParams, EMPTY_MULTI_MAP, AttachmentResponse.class);
    }

    @Override
    public AttachmentCreateResponse createAttachment(AttachmentCreateRequest request) throws UserDataStoreClientException {
        return post("/admin/attachments", request, EMPTY_MULTI_MAP, EMPTY_MULTI_MAP, AttachmentCreateResponse.class);
    }

    @Override
    public void updateAttachment(String attachmentId, AttachmentUpdateRequest request) throws UserDataStoreClientException {
        put("/admin/attachments/" + attachmentId, request, EMPTY_MULTI_MAP, EMPTY_MULTI_MAP, Response.class);
    }

    @Override
    public void deleteAttachments(String userId, String documentId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        if (documentId != null) {
            queryParams.put("documentId", Collections.singletonList(documentId));
        }
        delete("/admin/attachments", queryParams, EMPTY_MULTI_MAP, DocumentResponse.class);
    }

    @Override
    public Object fetchUserClaims(String userId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        return get("/private/user-claims", queryParams, EMPTY_MULTI_MAP, Object.class);
    }

    @Override
    public void storeUserClaims(String userId, Object claims) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        try {
            restClient.post("/public/user-claims", claims, queryParams, EMPTY_MULTI_MAP, new ParameterizedTypeReference<Response>(){});
        } catch (RestClientException ex) {
            handleException(ex);
        }
    }

    @Override
    public void deleteUserClaims(String userId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        delete("/public/user-claims", queryParams, EMPTY_MULTI_MAP, Response.class);
    }

    @Override
    public Object fetchClaims(String userId, String claim) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        if (claim != null) {
            queryParams.put("claim", Collections.singletonList(claim));
        }
        return get("/claims", queryParams, EMPTY_MULTI_MAP, Object.class);
    }

    @Override
    public void createClaims(String userId, Object value) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        try {
            restClient.post("/admin/claims", value, queryParams, EMPTY_MULTI_MAP, new ParameterizedTypeReference<Response>(){});
        } catch (RestClientException ex) {
            handleException(ex);
        }
    }

    @Override
    public void updateClaims(String userId, Object value) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        try {
            restClient.put("/admin/claims", value, queryParams, EMPTY_MULTI_MAP, new ParameterizedTypeReference<Response>(){});
        } catch (RestClientException ex) {
            handleException(ex);
        }
    }

    @Override
    public void deleteClaims(String userId, String claim) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        queryParams.put("claim", Collections.singletonList(claim));
        delete("/admin/claims", queryParams, EMPTY_MULTI_MAP, Response.class);
    }

}
