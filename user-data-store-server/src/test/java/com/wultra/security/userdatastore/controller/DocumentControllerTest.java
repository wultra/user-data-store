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
package com.wultra.security.userdatastore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.security.userdatastore.client.model.dto.DocumentDto;
import com.wultra.security.userdatastore.client.model.request.DocumentCreateRequest;
import com.wultra.security.userdatastore.client.model.request.DocumentUpdateRequest;
import com.wultra.security.userdatastore.client.model.response.DocumentResponse;
import com.wultra.security.userdatastore.config.WebSecurityConfiguration;
import com.wultra.security.userdatastore.service.DocumentService;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test for {@link DocumentController}.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@WebMvcTest(DocumentController.class)
@Import(WebSecurityConfiguration.class)
class DocumentControllerTest {

    @MockBean
    private DocumentService service;

    @Autowired
    private MockMvc mvc;

    @WithMockUser(roles = "READ")
    @Test
    void testGet() throws Exception {
        DocumentDto document = DocumentDto.builder()
                .userId("alice")
                .documentType("profile")
                .dataType("claims")
                .documentDataId("83692")
                .documentData(new ObjectMapper().writeValueAsString(new LinkedHashMap<>(Map.of(
                        "sub", "83692",
                        "name", "Alice Adams",
                        "email", "alice@example.com",
                        "birthdate", "1975-12-31",
                        "https://claims.example.com/department", "engineering"
                ))))
                .build();
        DocumentResponse response = new DocumentResponse(Collections.singletonList(document));
        when(service.fetchDocuments("alice", Optional.empty()))
                .thenReturn(response);

        mvc.perform(get("/documents?userId=alice")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.responseObject.documents[0].userId", is("alice")))
                .andExpect(jsonPath("$.responseObject.documents[0].documentType", is("profile")))
                .andExpect(jsonPath("$.responseObject.documents[0].dataType", is("claims")))
                .andExpect(jsonPath("$.responseObject.documents[0].documentDataId", is("83692")))
                .andExpect(jsonPath("$.responseObject.documents[0].documentData", containsString("\"sub\":\"83692\"")))
                .andExpect(jsonPath("$.responseObject.documents[0].documentData", containsString("\"name\":\"Alice Adams\"")))
                .andExpect(jsonPath("$.responseObject.documents[0].documentData", containsString("\"email\":\"alice@example.com\"")))
                .andExpect(jsonPath("$.responseObject.documents[0].documentData", containsString("\"birthdate\":\"1975-12-31\"")))
                .andExpect(jsonPath("$.responseObject.documents[0].documentData", containsString("\"https://claims.example.com/department\":\"engineering\"")));
    }

   @WithMockUser(roles = "WRITE")
   @Test
   void testGet_wrongRoles() throws Exception {
        mvc.perform(get("/documents?userId=alice")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "WRITE")
    @Test
    void testDelete() throws Exception {
        mvc.perform(delete("/admin/documents?userId=alice"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")));

        verify(service).deleteDocuments("alice", Optional.empty());
    }

    @WithMockUser(roles = "READ")
    @Test
    void testDelete_wrongRoles() throws Exception {
        mvc.perform(delete("/admin/documents?userId=alice"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "WRITE")
    @Test
    void testPost() throws Exception {
        final var documentData = new ObjectMapper().writeValueAsString(Map.of(
                "sub", "83692",
                "name", "Alice Adams",
                "email", "alice@example.com",
                "birthdate", "1975-12-31",
                "https://claims.example.com/department", "engineering"
        ));
        final var documentCreateRequest = new DocumentCreateRequest("alice", "profile", "claims",
                "83692", null, documentData, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());
        final Map<String, Object> requestBody = Map.of(
                "userId", "alice",
                "documentType", "profile",
                "dataType", "claims",
                "documentDataId", "83692",
                "documentData", documentData,
                "attributes", Collections.emptyMap(),
                "photos", Collections.emptyList(),
                "attachments", Collections.emptyList()
        );

        final String requestBodyJson = new ObjectMapper().writeValueAsString(new ObjectRequest<>(documentCreateRequest));
        mvc.perform(post("/admin/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")));

        verify(service).createDocument(documentCreateRequest);
    }

    @WithMockUser(roles = "READ")
    @Test
    void testPost_wrongRoles() throws Exception {
        mvc.perform(post("/admin/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "WRITE")
    @Test
    void testPut() throws Exception {
        final var documentData = new ObjectMapper().writeValueAsString(Map.of(
                "sub", "83692",
                "name", "Alice Adams",
                "email", "alice@example.com",
                "birthdate", "1976-12-31",
                "https://claims.example.com/department", "engineering"
        ));
        final var documentUpdateRequest = new DocumentUpdateRequest("alice", "profile", "claims",
                "83692", null, documentData, Collections.emptyMap());
        final Map<String, Object> requestBody = Map.of(
                "userId", "alice",
                "id", "1",
                "documentType", "profile",
                "dataType", "claims",
                "documentDataId", "83692",
                "documentData", documentData,
                "attributes", Collections.emptyMap()
        );

        final String requestBodyJson = new ObjectMapper().writeValueAsString(new ObjectRequest<>(documentUpdateRequest));
        mvc.perform(put("/admin/documents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")));

        verify(service).updateDocument("1", documentUpdateRequest);
    }

    @WithMockUser(roles = "READ")
    @Test
    void testPut_wrongRoles() throws Exception {
        mvc.perform(put("/admin/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
