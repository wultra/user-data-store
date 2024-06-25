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
package com.wultra.security.userdatastore.restclient;

import com.wultra.core.rest.client.base.RestClientConfiguration;
import com.wultra.security.userdatastore.UserDataStoreRestClient;
import com.wultra.security.userdatastore.client.model.dto.PhotoDto;
import com.wultra.security.userdatastore.client.model.error.UserDataStoreClientException;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.EmbeddedPhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.request.PhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.request.PhotoUpdateRequest;
import com.wultra.security.userdatastore.client.model.response.DocumentCreateResponse;
import com.wultra.security.userdatastore.client.model.response.PhotoCreateResponse;
import com.wultra.security.userdatastore.client.model.response.PhotoResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.security.Security;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Photos REST API test.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PhotoRestClientTest {

    private static final String USER_DATA_STORE_REST_URL = "http://localhost:%d/user-data-store";

    @LocalServerPort
    private int serverPort;
    
    private UserDataStoreRestClient restClient;

    @BeforeAll
    void initTests() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        RestClientConfiguration config = new RestClientConfiguration();
        config.setHttpBasicAuthEnabled(true);
        config.setHttpBasicAuthUsername("admin");
        config.setHttpBasicAuthPassword("admin");
        config.setBaseUrl(USER_DATA_STORE_REST_URL.formatted(serverPort));
        restClient = new UserDataStoreRestClient(config);
    }

    @Test
    void testPost() throws Exception {
        DocumentCreateRequest request = new DocumentCreateRequest("alice", "test", "test", "1", null, "test_data", Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());
        DocumentCreateResponse response = restClient.createDocument(request);
        assertNotNull(response.id());
        assertNotNull(response.documentDataId());
        PhotoCreateRequest photoRequest = new PhotoCreateRequest("alice", response.id(), "test", "aW1hZ2VfZGF0YQ==", null);
        PhotoCreateResponse photoResponse = restClient.createPhoto(photoRequest);
        assertNotNull(photoResponse.id());
        assertNotNull(photoResponse.documentId());
    }

    @Test
    void testLifeCycle() throws Exception {
        DocumentCreateRequest request = new DocumentCreateRequest("alice", "test_type", "test_data_type", "1", null, "test_data", Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());
        DocumentCreateResponse response = restClient.createDocument(request);
        assertNotNull(response.id());
        assertNotNull(response.documentDataId());

        PhotoCreateRequest photoRequest = new PhotoCreateRequest("alice", response.id(), "test_type", "aW1hZ2VfZGF0YQ==", null);
        PhotoCreateResponse photoResponse = restClient.createPhoto(photoRequest);
        assertNotNull(photoResponse.id());
        assertNotNull(photoResponse.documentId());

        PhotoResponse fetchResponse = restClient.fetchPhotos("alice", photoResponse.documentId());
        assertEquals(1, fetchResponse.photos().size());
        PhotoDto photo = fetchResponse.photos().get(0);
        assertNotNull(photo.id());
        assertEquals(response.id(), photo.documentId());
        assertEquals("test_type", photo.photoType());
        assertEquals("aW1hZ2VfZGF0YQ==", photo.photoData());
        assertNull(photo.externalId());

        PhotoUpdateRequest requestUpdate = new PhotoUpdateRequest("test_type2", "aW1hZ2VfZGF0YTI=", null);
        restClient.updatePhoto(photo.id(), requestUpdate);

        PhotoResponse fetchResponse2 = restClient.fetchPhotos("alice", photoResponse.documentId());
        assertEquals(1, fetchResponse2.photos().size());
        PhotoDto photo2 = fetchResponse2.photos().get(0);
        assertNotNull(photo2.id());
        assertEquals(response.id(), photo2.documentId());
        assertEquals("test_type2", photo2.photoType());
        assertEquals("aW1hZ2VfZGF0YTI=", photo2.photoData());
        assertNull(photo2.externalId());

        restClient.deletePhotos("alice", photoResponse.documentId());

        PhotoResponse fetchResponse3 = restClient.fetchPhotos("alice", photoResponse.documentId());
        assertEquals(0, fetchResponse3.photos().size());
    }

    @Test
    void testValidation_NullUser() {
        PhotoCreateRequest photoRequest = new PhotoCreateRequest(null, "123", "test", "test_data", null);
        assertThrows(UserDataStoreClientException.class, () -> restClient.createPhoto(photoRequest));
    }

    @Test
    void testValidation_InvalidBase64() {
        EmbeddedPhotoCreateRequest photoRequest = new EmbeddedPhotoCreateRequest("person", "invalid_data", null);
        List<EmbeddedPhotoCreateRequest> photos = Collections.singletonList(photoRequest);
        DocumentCreateRequest request = new DocumentCreateRequest("alice", "photo", "image_base64", "1", null, "{}", Collections.emptyMap(), photos, Collections.emptyList());
        assertThrows(UserDataStoreClientException.class, () -> restClient.createDocument(request));
    }

}
