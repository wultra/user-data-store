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

import com.wultra.security.userdatastore.UserDataStoreRestClient;
import com.wultra.security.userdatastore.UserDataStoreRestClientConfiguration;
import com.wultra.security.userdatastore.client.model.dto.DocumentDto;
import com.wultra.security.userdatastore.client.model.error.UserDataStoreClientException;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentUpdateRequest;
import com.wultra.security.userdatastore.client.model.response.DocumentCreateResponse;
import com.wultra.security.userdatastore.client.model.response.DocumentResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Documents REST API test.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentRestClientTest {

    private static final String USER_DATA_STORE_REST_URL = "http://localhost:%d/user-data-store-server";

    @LocalServerPort
    private int serverPort;
    
    private UserDataStoreRestClient restClient;

    @BeforeAll
    void initRestClient() throws Exception {
        UserDataStoreRestClientConfiguration config = new UserDataStoreRestClientConfiguration();
        config.setHttpBasicUsername("admin");
        config.setHttpBasicPassword("admin");
        restClient = new UserDataStoreRestClient(USER_DATA_STORE_REST_URL.formatted(serverPort), config);
    }

    @Test
    void testPost() throws Exception {
        DocumentCreateRequest request = new DocumentCreateRequest("alice", "test", "test_type", "1", null, "test_data", Collections.emptyMap());
        DocumentCreateResponse response = restClient.createDocument(request);
        assertNotNull(response.id());
        assertNotNull(response.documentDataId());
    }

    @Test
    void testLifeCycle() throws Exception {
        DocumentCreateRequest request = new DocumentCreateRequest("alice", "test_type", "test_data_type", "1", null, "test_data", Collections.emptyMap());
        DocumentCreateResponse response = restClient.createDocument(request);
        assertNotNull(response.id());
        assertNotNull(response.documentDataId());

        DocumentResponse documentResponse = restClient.fetchDocuments("alice", response.id());
        assertEquals(1, documentResponse.documents().size());
        DocumentDto document = documentResponse.documents().get(0);
        assertNotNull(document.id());
        assertEquals("alice", document.userId());
        assertEquals("test_type", document.documentType());
        assertEquals("test_data_type", document.dataType());
        assertEquals("1", document.documentDataId());
        assertNull(document.externalId());
        assertEquals("test_data", document.documentData());
        assertEquals(Collections.emptyMap(), document.attributes());

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("test", "test");
        DocumentUpdateRequest updateRequest = new DocumentUpdateRequest("bob", document.id(), "test_type2", "test_data_type2", "2", "3", "test_data2", attributes);
        restClient.updateDocument(updateRequest);

        DocumentResponse documentResponse2 = restClient.fetchDocuments("bob", response.id());
        assertEquals(1, documentResponse2.documents().size());
        DocumentDto document2 = documentResponse2.documents().get(0);
        assertNotNull(document2.id());
        assertEquals("bob", document2.userId());
        assertEquals("test_type2", document2.documentType());
        assertEquals("test_data_type2", document2.dataType());
        assertEquals("2", document2.documentDataId());
        assertEquals("3", document2.externalId());
        assertEquals("test_data2", document2.documentData());
        assertEquals(1, document2.attributes().size());

        restClient.deleteDocuments("bob", response.id());

        assertThrows(UserDataStoreClientException.class, () -> restClient.fetchDocuments("bob", response.id()));
    }
}
