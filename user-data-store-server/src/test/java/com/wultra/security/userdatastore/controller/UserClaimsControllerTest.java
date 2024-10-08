/*
 * User Data Store
 * Copyright (C) 2023 Wultra s.r.o.
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

import com.wultra.security.userdatastore.config.WebSecurityConfiguration;
import com.wultra.security.userdatastore.service.UserClaimsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test for {@link UserClaimsController}.
 *
 * @author Lubos Racansky lubos.racansky@wultra.com
 */
@WebMvcTest(UserClaimsController.class)
@Import(WebSecurityConfiguration.class)
class UserClaimsControllerTest {

    @MockBean
    private UserClaimsService service;

    @Autowired
    private MockMvc mvc;

    @WithMockUser(roles = "READ")
    @Test
    void testGet() throws Exception {
        when(service.fetchUserClaims("alice"))
                .thenReturn(Map.of(
                          "sub", "83692",
                          "name", "Alice Adams",
                          "email", "alice@example.com",
                          "birthdate", "1975-12-31",
                          "https://claims.example.com/department", "engineering"
                        ));

        mvc.perform(get("/private/user-claims?userId=alice")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.responseObject.name", is("Alice Adams")));
    }

    @WithMockUser(roles = "WRITE")
    @Test
    void testGet_wrongRoles() throws Exception {
        mvc.perform(get("/private/user-claims?userId=alice")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "WRITE")
    @Test
    void testDelete() throws Exception {
        mvc.perform(delete("/public/user-claims?userId=alice"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")));

        verify(service).deleteUserClaims("alice");
    }

    @WithMockUser(roles = "READ")
    @Test
    void testDelete_wrongRoles() throws Exception {
        mvc.perform(delete("/public/user-claims?userId=alice"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "WRITE")
    @Test
    void testPost() throws Exception {
        mvc.perform(post("/public/user-claims?userId=alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sub": "83692",
                                  "name": "Alice Adams",
                                  "email": "alice@example.com",
                                  "birthdate": "1975-12-31",
                                  "https://claims.example.com/department": "engineering"
                                }
                                """))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")));

        final var expectedClaims = Map.of(
                "sub", "83692",
                "name", "Alice Adams",
                "email", "alice@example.com",
                "birthdate", "1975-12-31",
                "https://claims.example.com/department", "engineering"
        );
        verify(service).createOrUpdateUserClaims("alice", expectedClaims);
    }

    @WithMockUser(roles = "READ")
    @Test
    void testPost_wrongRoles() throws Exception {
        mvc.perform(post("/public/user-claims?userId=alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
