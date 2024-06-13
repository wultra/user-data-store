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
import com.wultra.security.userdatastore.client.model.dto.AttachmentDto;
import com.wultra.security.userdatastore.client.model.request.AttachmentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.response.AttachmentCreateResponse;
import com.wultra.security.userdatastore.client.model.response.AttachmentResponse;
import com.wultra.security.userdatastore.client.model.response.DocumentCreateResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Attachments REST API test.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentRestClientTest {

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
        AttachmentCreateRequest attachmentRequest = new AttachmentCreateRequest("alice", response.id(), "test", "test_data", null);
        AttachmentCreateResponse attachmentResponse = restClient.createAttachment(attachmentRequest);
        assertNotNull(attachmentResponse.id());
        assertNotNull(attachmentResponse.documentId());
    }

    @Test
    void testLifeCycle() throws Exception {
        DocumentCreateRequest request = new DocumentCreateRequest("alice", "test_type", "test_data_type", "1", null, "test_data", Collections.emptyMap());
        DocumentCreateResponse response = restClient.createDocument(request);
        assertNotNull(response.id());
        assertNotNull(response.documentDataId());

        AttachmentCreateRequest attachmentRequest = new AttachmentCreateRequest("alice", response.id(), "test_type", "test_data", null);
        AttachmentCreateResponse attachmentResponse = restClient.createAttachment(attachmentRequest);
        assertNotNull(attachmentResponse.id());
        assertNotNull(attachmentResponse.documentId());

        AttachmentResponse fetchResponse = restClient.fetchAttachments("alice", attachmentResponse.documentId());
        assertEquals(1, fetchResponse.attachments().size());
        AttachmentDto attachment = fetchResponse.attachments().get(0);
        assertNotNull(attachment.id());
        assertEquals(response.id(), attachment.documentId());
        assertEquals("test_type", attachment.attachmentType());
        assertEquals("test_data", attachment.attachmentData());
        assertNull(attachment.externalId());

        restClient.deleteAttachments("alice", attachmentResponse.documentId());

        AttachmentResponse fetchResponse2 = restClient.fetchAttachments("alice", attachmentResponse.documentId());
        assertEquals(0, fetchResponse2.attachments().size());
    }
}
