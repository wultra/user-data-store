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
import com.wultra.security.userdatastore.client.model.dto.AttachmentDto;
import com.wultra.security.userdatastore.client.model.request.AttachmentCreateRequest;
import com.wultra.security.userdatastore.client.model.response.AttachmentResponse;
import com.wultra.security.userdatastore.config.WebSecurityConfiguration;
import com.wultra.security.userdatastore.service.AttachmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.SecureRandom;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test for {@link AttachmentController}.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@WebMvcTest(AttachmentController.class)
@Import(WebSecurityConfiguration.class)
class AttachmentControllerTest {

    final String attachmentData;

    {
        final SecureRandom random = new SecureRandom();
        final StringBuilder stringBuilder = new StringBuilder(100);
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // Loop to create a random string of the specified length
        for (int i = 0; i < 100; i++) {
            int index = random.nextInt(characters.length());
            stringBuilder.append(characters.charAt(index));
        }
        attachmentData = stringBuilder.toString();
    }

    @MockBean
    private AttachmentService service;

    @Autowired
    private MockMvc mvc;

    @WithMockUser(roles = "READ")
    @Test
    void testGet() throws Exception {
        AttachmentResponse response = new AttachmentResponse();
        AttachmentDto attachment = AttachmentDto.builder()
                .userId("alice")
                .documentId("1")
                .attachmentType("text")
                .attachmentData(attachmentData)
                .build();
        response.add(attachment);
        when(service.fetchAttachments("alice", Optional.of("1")))
                .thenReturn(response);

        mvc.perform(get("/attachments?userId=alice&documentId=1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.responseObject[0].userId", is("alice")))
                .andExpect(jsonPath("$.responseObject[0].documentId", is("1")))
                .andExpect(jsonPath("$.responseObject[0].attachmentType", is("text")))
                .andExpect(jsonPath("$.responseObject[0].attachmentData", is(attachmentData)));
    }

    @WithMockUser(roles = "WRITE")
    @Test
    void testGet_wrongRoles() throws Exception {
        mvc.perform(get("/attachments?userId=alice&documentId=1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "WRITE")
    @Test
    void testDelete() throws Exception {
        mvc.perform(delete("/admin/attachments?userId=alice&documentId=1"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")));

        verify(service).deleteAttachments("alice", Optional.of("1"));
    }

    @WithMockUser(roles = "READ")
    @Test
    void testDelete_wrongRoles() throws Exception {
        mvc.perform(delete("/admin/attachments?userId=alice&documentId=1"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "WRITE")
    @Test
    void testPost() throws Exception {
        final var attachmentCreateRequest = new AttachmentCreateRequest("alice", "1","text", attachmentData, null);

        final String requestBodyJson = new ObjectMapper().writeValueAsString(attachmentCreateRequest);
        mvc.perform(post("/admin/attachments?userId=alice&documentId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")));

        verify(service).createAttachment(attachmentCreateRequest);
    }

    @WithMockUser(roles = "READ")
    @Test
    void testPost_wrongRoles() throws Exception {
        mvc.perform(post("/admin/attachments?userId=alice&documentId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

}
