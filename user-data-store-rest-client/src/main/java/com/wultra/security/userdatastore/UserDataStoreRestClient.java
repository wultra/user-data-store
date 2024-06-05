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
import com.wultra.core.rest.client.base.RestClientException;
import com.wultra.security.userdatastore.client.UserDataStoreClient;
import com.wultra.security.userdatastore.client.model.error.UserDataStoreClientException;
import com.wultra.security.userdatastore.client.model.request.AttachmentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentUpdateRequest;
import com.wultra.security.userdatastore.client.model.request.PhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.response.*;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Optional;

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
        this(baseUrl, new UserDataStoreRestClientConfiguration());
    }

    /**
     * PowerAuth REST client constructor.
     *
     * @param baseUrl Base URL of REST endpoints.
     */
    public UserDataStoreRestClient(String baseUrl, UserDataStoreRestClientConfiguration config) throws UserDataStoreClientException {
        final DefaultRestClient.Builder builder = DefaultRestClient.builder().baseUrl(baseUrl)
                .acceptInvalidCertificate(config.getAcceptInvalidSslCertificate())
                .connectionTimeout(config.getConnectTimeout())
                .maxInMemorySize(config.getMaxMemorySize());
        if (config.isProxyEnabled()) {
            final DefaultRestClient.ProxyBuilder proxyBuilder = builder.proxy().host(config.getProxyHost()).port(config.getProxyPort());
            if (config.getProxyUsername() != null) {
                proxyBuilder.username(config.getProxyUsername()).password(config.getProxyPassword());
            }
            proxyBuilder.build();
        }
        if (config.getPowerAuthClientToken() != null) {
            builder.httpBasicAuth().username(config.getPowerAuthClientToken()).password(config.getPowerAuthClientSecret()).build();
        }
        if (config.getDefaultHttpHeaders() != null) {
            builder.defaultHttpHeaders(config.getDefaultHttpHeaders());
        }
        if (config.getFilter() != null) {
            builder.filter(config.getFilter());
        }
        try {
            restClient = builder.build();
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
    public DocumentResponse fetchDocuments(String userId, Optional<String> documentId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        documentId.ifPresent(s -> queryParams.put("documentId", Collections.singletonList(s)));
        return get("/documents", queryParams, EMPTY_MULTI_MAP, DocumentResponse.class);
    }

    @Override
    public DocumentCreateResponse createDocument(DocumentCreateRequest request) throws UserDataStoreClientException {
        return post("/documents", request, EMPTY_MULTI_MAP, EMPTY_MULTI_MAP, DocumentCreateResponse.class);
    }

    @Override
    public void updateDocument(DocumentUpdateRequest request) throws UserDataStoreClientException {
        put("/documents", request, EMPTY_MULTI_MAP, EMPTY_MULTI_MAP, Response.class);
    }

    @Override
    public void deleteDocuments(String userId, Optional<String> documentId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        documentId.ifPresent(s -> queryParams.put("documentId", Collections.singletonList(s)));
        delete("/documents", queryParams, EMPTY_MULTI_MAP, DocumentResponse.class);
    }

    @Override
    public PhotoResponse fetchPhotos(String userId, Optional<String> documentId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        documentId.ifPresent(s -> queryParams.put("documentId", Collections.singletonList(s)));
        return get("/photos", queryParams, EMPTY_MULTI_MAP, PhotoResponse.class);
    }

    @Override
    public PhotoCreateResponse createPhoto(PhotoCreateRequest request) throws UserDataStoreClientException {
        return post("/photos", request, EMPTY_MULTI_MAP, EMPTY_MULTI_MAP, PhotoCreateResponse.class);
    }

    @Override
    public void deletePhotos(String userId, Optional<String> documentId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        documentId.ifPresent(s -> queryParams.put("documentId", Collections.singletonList(s)));
        delete("/photos", queryParams, EMPTY_MULTI_MAP, DocumentResponse.class);
    }

    @Override
    public AttachmentResponse fetchAttachments(String userId, Optional<String> documentId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        documentId.ifPresent(s -> queryParams.put("documentId", Collections.singletonList(s)));
        return get("/attachments", queryParams, EMPTY_MULTI_MAP, AttachmentResponse.class);

    }

    @Override
    public AttachmentCreateResponse createAttachment(AttachmentCreateRequest request) throws UserDataStoreClientException {
        return post("/attachments", request, EMPTY_MULTI_MAP, EMPTY_MULTI_MAP, AttachmentCreateResponse.class);
    }

    @Override
    public void deleteAttachments(String userId, Optional<String> documentId) throws UserDataStoreClientException {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("userId", Collections.singletonList(userId));
        documentId.ifPresent(s -> queryParams.put("documentId", Collections.singletonList(s)));
        delete("/attachments", queryParams, EMPTY_MULTI_MAP, DocumentResponse.class);
    }
}
