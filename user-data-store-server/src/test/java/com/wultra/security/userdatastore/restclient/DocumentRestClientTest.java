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
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.response.DocumentCreateResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Document REST API test.
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
        DocumentCreateRequest request = new DocumentCreateRequest("alice", "test", "test", "1", null, "test_data", Collections.emptyMap());
        DocumentCreateResponse response = restClient.createDocument(request);
        assertNotNull(response.id());
        assertNotNull(response.documentId());
    }
}
