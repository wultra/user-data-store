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
import com.wultra.security.userdatastore.config.WebSecurityConfiguration;
import com.wultra.security.userdatastore.model.dto.PhotoDto;
import com.wultra.security.userdatastore.model.request.PhotoCreateRequest;
import com.wultra.security.userdatastore.model.response.PhotoResponse;
import com.wultra.security.userdatastore.service.PhotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test for {@link PhotoController}.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@WebMvcTest(PhotoController.class)
@Import(WebSecurityConfiguration.class)
class PhotoControllerTest {

    final String encodedPhoto;

    {
        try {
            final BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            final byte[] imageData = baos.toByteArray();
            encodedPhoto = Base64.getEncoder().encodeToString(imageData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @MockBean
    private PhotoService service;

    @Autowired
    private MockMvc mvc;

    @WithMockUser(roles = "READ")
    @Test
    void testGet() throws Exception {
        PhotoResponse response = new PhotoResponse();
        PhotoDto photo = PhotoDto.builder()
                .userId("alice")
                .documentId("1")
                .photoType("person")
                .photoData(encodedPhoto)
                .build();
        response.add(photo);
        when(service.fetchPhotos("alice", "1"))
                .thenReturn(response);

        mvc.perform(get("/photos?userId=alice&documentId=1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.responseObject[0].userId", is("alice")))
                .andExpect(jsonPath("$.responseObject[0].documentId", is("1")))
                .andExpect(jsonPath("$.responseObject[0].photoType", is("person")))
                .andExpect(jsonPath("$.responseObject[0].photoData", is("iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAIAAAD/gAIDAAAANElEQVR4Xu3BAQ0AAADCoPdPbQ43oAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAfgx1lAABHywbagAAAABJRU5ErkJggg==")));
    }

    @WithMockUser(roles = "WRITE")
    @Test
    void testGet_wrongRoles() throws Exception {
        mvc.perform(get("/photos?userId=alice&documentId=1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "WRITE")
    @Test
    void testDelete() throws Exception {
        mvc.perform(delete("/admin/photos?userId=alice&documentId=1"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")));

        verify(service).deletePhotos("alice", "1");
    }

    @WithMockUser(roles = "READ")
    @Test
    void testDelete_wrongRoles() throws Exception {
        mvc.perform(delete("/admin/photos?userId=alice&documentId=1"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "WRITE")
    @Test
    void testPost() throws Exception {
        final var photoCreateRequest = new PhotoCreateRequest("person", encodedPhoto, null);

        final String requestBodyJson = new ObjectMapper().writeValueAsString(photoCreateRequest);
        mvc.perform(post("/admin/photos?userId=alice&documentId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("OK")));

        verify(service).createPhoto("alice", "1", photoCreateRequest);
    }

    @WithMockUser(roles = "READ")
    @Test
    void testPost_wrongRoles() throws Exception {
        mvc.perform(post("/admin/photos?userId=alice&documentId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

}
