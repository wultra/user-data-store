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
import com.wultra.security.userdatastore.client.model.error.UserDataStoreClientException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.security.Security;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Claim REST API test.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClaimsRestClientTest {

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
    @SuppressWarnings("unchecked")
    void testPost() throws Exception {
        Map<String, String> claims = new LinkedHashMap<>();
        claims.put("claim1", "value1");
        claims.put("claim2", "value2");
        restClient.createClaims("alice", claims);

        Map<String, String> claimsRetrieved = (Map<String, String>) restClient.fetchClaims("alice", null);
        assertEquals(2, claimsRetrieved.size());
        assertEquals("value1", claimsRetrieved.get("claim1"));
        assertEquals("value2", claimsRetrieved.get("claim2"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testLifeCycleAllClaims() throws Exception {
        Map<String, String> claims = new LinkedHashMap<>();
        claims.put("claim1", "value1");
        claims.put("claim2", "value2");
        restClient.createClaims("bob", claims);

        Map<String, String> claimsRetrieved = (Map<String, String>) restClient.fetchClaims("bob", null);
        assertEquals(2, claimsRetrieved.size());
        assertEquals("value1", claimsRetrieved.get("claim1"));
        assertEquals("value2", claimsRetrieved.get("claim2"));

        Map<String, String> claims2 = new LinkedHashMap<>();
        claims2.put("claim1", "value2");
        claims2.put("claim2", "value1");
        restClient.updateClaims("bob", claims2);

        Map<String, String> claimsRetrieved2 = (Map<String, String>) restClient.fetchClaims("bob", null);
        assertEquals(2, claimsRetrieved2.size());
        assertEquals("value2", claimsRetrieved2.get("claim1"));
        assertEquals("value1", claimsRetrieved2.get("claim2"));

        restClient.deleteClaims("bob", null);
        assertThrows(UserDataStoreClientException.class, () -> restClient.fetchClaims("bob", null));
    }
}
