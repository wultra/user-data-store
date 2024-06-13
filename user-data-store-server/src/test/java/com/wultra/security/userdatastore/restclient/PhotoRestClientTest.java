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
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.PhotoCreateRequest;
import com.wultra.security.userdatastore.client.model.response.DocumentCreateResponse;
import com.wultra.security.userdatastore.client.model.response.PhotoCreateResponse;
import com.wultra.security.userdatastore.client.model.response.PhotoResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

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

    private static final String USER_DATA_STORE_REST_URL = "http://localhost:%d/user-data-store-server";

    @LocalServerPort
    private int serverPort;
    
    private UserDataStoreRestClient restClient;

    @BeforeAll
    void initRestClient() throws Exception {
        RestClientConfiguration config = new RestClientConfiguration();
        config.setHttpBasicAuthEnabled(true);
        config.setHttpBasicAuthUsername("admin");
        config.setHttpBasicAuthPassword("admin");
        config.setBaseUrl(USER_DATA_STORE_REST_URL.formatted(serverPort));
        restClient = new UserDataStoreRestClient(config);
    }

    @Test
    void testPost() throws Exception {
        DocumentCreateRequest request = new DocumentCreateRequest("alice", "test", "test", "1", null, "test_data", Collections.emptyMap());
        DocumentCreateResponse response = restClient.createDocument(request);
        assertNotNull(response.id());
        assertNotNull(response.documentDataId());
        PhotoCreateRequest photoRequest = new PhotoCreateRequest("alice", response.id(), "test", "test_data", null);
        PhotoCreateResponse photoResponse = restClient.createPhoto(photoRequest);
        assertNotNull(photoResponse.id());
        assertNotNull(photoResponse.documentId());
    }

    @Test
    void testLifeCycle() throws Exception {
        DocumentCreateRequest request = new DocumentCreateRequest("alice", "test_type", "test_data_type", "1", null, "test_data", Collections.emptyMap());
        DocumentCreateResponse response = restClient.createDocument(request);
        assertNotNull(response.id());
        assertNotNull(response.documentDataId());

        PhotoCreateRequest PhotoRequest = new PhotoCreateRequest("alice", response.id(), "test_type", "test_data", null);
        PhotoCreateResponse PhotoResponse = restClient.createPhoto(PhotoRequest);
        assertNotNull(PhotoResponse.id());
        assertNotNull(PhotoResponse.documentId());

        PhotoResponse fetchResponse = restClient.fetchPhotos("alice", PhotoResponse.documentId());
        assertEquals(1, fetchResponse.photos().size());
        PhotoDto Photo = fetchResponse.photos().get(0);
        assertNotNull(Photo.id());
        assertEquals(response.id(), Photo.documentId());
        assertEquals("test_type", Photo.photoType());
        assertEquals("test_data", Photo.photoData());
        assertNull(Photo.externalId());

        restClient.deletePhotos("alice", PhotoResponse.documentId());

        PhotoResponse fetchResponse2 = restClient.fetchPhotos("alice", PhotoResponse.documentId());
        assertEquals(0, fetchResponse2.photos().size());
    }
}
