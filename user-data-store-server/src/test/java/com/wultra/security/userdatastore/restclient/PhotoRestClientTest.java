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
import com.wultra.security.userdatastore.client.model.request.*;
import com.wultra.security.userdatastore.client.model.response.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;
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
    private static final String PHOTO_BASE_64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR4nGP4//8/AAX+Av4N70a4AAAAAElFTkSuQmCC";
    private static final String PHOTO2_BASE_64 = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAMAAABEpIrGAAAAkFBMVEUAAAAQM0QWNUYWNkYXNkYALjoWNUYYOEUXN0YaPEUPMUAUM0QVNUYWNkYWNUYWNUUWNUYVNEYWNkYWNUYWM0eF6i0XNkchR0OB5SwzZj9wyTEvXkA3az5apTZ+4C5DgDt31C9frjU5bz5uxTI/eDxzzjAmT0IsWUEeQkVltzR62S6D6CxIhzpKijpJiDpOkDl4b43lAAAAFXRSTlMAFc304QeZ/vj+ECB3xKlGilPXvS2Ka/h0AAABfklEQVR42oVT2XaCMBAdJRAi7pYJa2QHxbb//3ctSSAUPfa+THLmzj4DBvZpvyauS9b7kw3PWDkWsrD6fFQhQ9dZLfVbC5M88CWCPERr+8fLZodJ5M8QJbjbGL1H2M1fIGfEm+wJN+bGCSc6EXtNS/8FSrq2VX6YDv++XLpJ8SgDWMnwqznGo6alcTbIxB2CHKn8VFikk2mMV2lEnV+CJd9+jJlxXmMr5dW14YCqwgbFpO8FNvJxwwM4TPWPo5QalEsRMAcusXpi58/QUEWPL0AK1ThM5oQCUyXPoPINkdd922VBw4XgTV9zDGWWFrgjIQs4vwvOg6xr+6gbCTqE+DYhlMGX0CF2OknK5gQ2JrkDh/W6TOEbYDeVecKbJtyNXiCfGmW7V93J2hDus1bDfhxWbIZVYDXITA7Lo6E0Ktgg9eB4KWuR44aj7ppBVPazhQH7/M/KgWe9X1qAg8XypT6nxIMJH+T94QCsLvj29IYwZxyO9/F8vCbO9tX5/wDGjEZ7vrgFZwAAAABJRU5ErkJggg==";

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
        PhotoCreateRequest photoRequest = new PhotoCreateRequest(null, "123", "test", PHOTO_BASE_64, null);
        assertThrows(UserDataStoreClientException.class, () -> restClient.createPhoto(photoRequest));
    }

    @Test
    void testValidation_InvalidBase64() {
        EmbeddedPhotoCreateRequest photoRequest = new EmbeddedPhotoCreateRequest("person", "invalid_data", null);
        List<EmbeddedPhotoCreateRequest> photos = Collections.singletonList(photoRequest);
        DocumentCreateRequest request = new DocumentCreateRequest("alice", "photo", "image_base64", "1", null, "{}", Collections.emptyMap(), photos, Collections.emptyList());
        assertThrows(UserDataStoreClientException.class, () -> restClient.createDocument(request));
    }

    @Test
    void testPhotoImportBase64Inline() throws Exception {
        EmbeddedPhotoImportRequest photoImportRequest = EmbeddedPhotoImportRequest.builder()
                .userId("alice")
                .photoDataType("base64_inline")
                .photoType("person")
                .photoData(PHOTO_BASE_64)
                .build();
        PhotosImportRequest importRequest = PhotosImportRequest.builder()
                .photos(Collections.singletonList(photoImportRequest))
                .build();
        PhotosImportResponse response = restClient.importPhotos(importRequest);
        verifyImportResponse(response, PHOTO_BASE_64);
    }

    @Test
    void testPhotoImportFileBase64() throws Exception {
        Path tempFile = Files.createTempFile("photo", ".png");
        Files.writeString(tempFile, PHOTO_BASE_64, StandardOpenOption.WRITE);
        EmbeddedPhotoImportRequest photoImportRequest = EmbeddedPhotoImportRequest.builder()
                .userId("alice")
                .photoDataType("base64")
                .photoType("person")
                .photoData(tempFile.toAbsolutePath().toString())
                .build();
        PhotosImportRequest importRequest = PhotosImportRequest.builder()
                .photos(Collections.singletonList(photoImportRequest))
                .build();
        PhotosImportResponse response = restClient.importPhotos(importRequest);
        verifyImportResponse(response, PHOTO_BASE_64);
    }

    @Test
    void testPhotoImportFileRaw() throws Exception {
        Path tempFile = Files.createTempFile("photo", ".png");
        byte[] imageData = Base64.getDecoder().decode(PHOTO_BASE_64);
        Files.write(tempFile, imageData, StandardOpenOption.WRITE);
        EmbeddedPhotoImportRequest photoImportRequest = EmbeddedPhotoImportRequest.builder()
                .userId("alice")
                .photoDataType("raw")
                .photoType("person")
                .photoData(tempFile.toAbsolutePath().toString())
                .build();
        PhotosImportRequest importRequest = PhotosImportRequest.builder()
                .photos(Collections.singletonList(photoImportRequest))
                .build();
        PhotosImportResponse response = restClient.importPhotos(importRequest);
        verifyImportResponse(response, PHOTO_BASE_64);
    }

    @Test
    void testPhotoImportUrl() throws Exception {
        EmbeddedPhotoImportRequest photoImportRequest = EmbeddedPhotoImportRequest.builder()
                .userId("alice")
                .photoDataType("raw")
                .photoType("person")
                .photoData("http://localhost:8080/user-data-store/swagger-ui/favicon-32x32.png")
                .build();
        PhotosImportRequest importRequest = PhotosImportRequest.builder()
                .photos(Collections.singletonList(photoImportRequest))
                .build();
        PhotosImportResponse response = restClient.importPhotos(importRequest);
        verifyImportResponse(response, PHOTO2_BASE_64);
    }

    @Test
    void testPhotoImportCsvBase64Inline() throws Exception {
        Path tempFile = Files.createTempFile("photos", ".csv");
        Files.writeString(tempFile, "user_test_123,base64_inline,person," + PHOTO_BASE_64 +
                "\n" + "user_test_456,base64_inline,person," + PHOTO_BASE_64);
        PhotosImportCsvRequest importRequest = PhotosImportCsvRequest.builder()
                .importPaths(Collections.singletonList(tempFile.toAbsolutePath().toString()))
                .build();
        restClient.importPhotosCsv(importRequest);
        verifyImportCsv(Arrays.asList("user_test_b64i_123", "user_test_b64i_456"), PHOTO_BASE_64);
    }

    @Test
    void testPhotoImportCsvBase64() throws Exception {
        Path tempFile = Files.createTempFile("photos", ".csv");
        Path photo1 = Files.createTempFile("photos", ".txt");
        Files.writeString(photo1, PHOTO_BASE_64);
        Path photo2 = Files.createTempFile("photos", ".txt");
        Files.writeString(photo2, PHOTO_BASE_64);
        Files.writeString(tempFile, "user_test_123,base64,person," + photo1.toAbsolutePath() +
                "\n" + "user_test_456,base64,person," + photo2.toAbsolutePath());
        PhotosImportCsvRequest importRequest = PhotosImportCsvRequest.builder()
                .importPaths(Collections.singletonList(tempFile.toAbsolutePath().toString()))
                .build();
        restClient.importPhotosCsv(importRequest);
        verifyImportCsv(Arrays.asList("user_test_b64_123", "user_test_b64_456"), PHOTO_BASE_64);
    }

    @Test
    void testPhotoImportCsvRaw() throws Exception {
        Path tempFile = Files.createTempFile("photos", ".csv");
        Path photo1 = Files.createTempFile("photos", ".png");
        Files.write(photo1, Base64.getDecoder().decode(PHOTO_BASE_64));
        Path photo2 = Files.createTempFile("photos", ".png");
        Files.write(photo2, Base64.getDecoder().decode(PHOTO_BASE_64));
        Files.writeString(tempFile, "user_test_123,raw,person," + photo1.toAbsolutePath() +
                "\n" + "user_test_456,raw,person," + photo2.toAbsolutePath());
        PhotosImportCsvRequest importRequest = PhotosImportCsvRequest.builder()
                .importPaths(Collections.singletonList(tempFile.toAbsolutePath().toString()))
                .build();
        restClient.importPhotosCsv(importRequest);
        verifyImportCsv(Arrays.asList("user_test_raw_123", "user_test_raw_456"), PHOTO_BASE_64);
    }

    @Test
    void testPhotoImportCsvUrl() throws Exception {
        Path tempFile = Files.createTempFile("photos", ".csv");
        Files.writeString(tempFile, "user_test_123,raw,person,http://localhost:8080/user-data-store/swagger-ui/favicon-32x32.png");
        PhotosImportCsvRequest importRequest = PhotosImportCsvRequest.builder()
                .importPaths(Collections.singletonList(tempFile.toAbsolutePath().toString()))
                .build();
        restClient.importPhotosCsv(importRequest);
        verifyImportCsv(List.of("user_test_url"), PHOTO2_BASE_64);
    }

    private void verifyImportResponse(PhotosImportResponse response, String expectedPhotoBase64) throws UserDataStoreClientException {
        assertEquals(1, response.photos().size());
        EmbeddedPhotoImportResponse result = response.photos().get(0);
        assertEquals("alice", result.userId());
        assertEquals("person", result.photoType());
        assertNotNull(result.documentId());
        assertNotNull(result.photoId());
        assertTrue(result.imported());
        assertNull(result.error());
        PhotoResponse photoResponse = restClient.fetchPhotos("alice", result.documentId());
        assertEquals(1, photoResponse.photos().size());
        PhotoDto photo = photoResponse.photos().get(0);
        assertEquals(result.photoId(), photo.id());
        assertEquals(result.documentId(), photo.documentId());
        assertEquals(result.photoType(), photo.photoType());
        assertEquals(expectedPhotoBase64, photo.photoData());
    }

    private void verifyImportCsv(List<String> userIds, String expectedPhotoBase64) {
        userIds.forEach(userId -> {
            for (int i = 0; i < 100; i++) {
                try {
                    verifyImportCsv(userId, expectedPhotoBase64);
                    if (i == 99) {
                        throw new Exception("Import from CSV failed");
                    }
                    break;
                } catch (Exception ex) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }

    private void verifyImportCsv(String userId, String expectedPhotoBase64) throws UserDataStoreClientException {
        DocumentResponse documentResponse = restClient.fetchDocuments(userId, null);
        PhotoResponse photoResponse = restClient.fetchPhotos(userId, documentResponse.documents().get(0).id());
        assertEquals(1, photoResponse.photos().size());
        PhotoDto photo = photoResponse.photos().get(0);
        assertEquals(expectedPhotoBase64, photo.photoData());
    }

}
