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
package com.wultra.security.userdatastore.userclaims;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.Security;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link EncryptionService}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
class EncryptionServiceTest {

    @BeforeAll
    static void registerSecurityProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void testNoEncryption_encrypt_keyNotConfiguredAndModeNotSpecified() {
        final EncryptionService tested = new EncryptionService(null);
        final UserClaimsEntity entity = new UserClaimsEntity();
        final String claims = "{\"name\": \"Alice Adams\"}";

        tested.encryptClaims(entity, claims);

        assertEquals("{\"name\": \"Alice Adams\"}", entity.getClaims());
        assertEquals(EncryptionMode.NO_ENCRYPTION, entity.getEncryptionMode());
    }

    @Test
    void testNoEncryption_encrypt_keyConfigured() {
        final EncryptionService tested = new EncryptionService("MTIzNDU2Nzg5MDEyMzQ1Ng==");
        final UserClaimsEntity entity = new UserClaimsEntity();
        entity.setUserId("alice");
        entity.setEncryptionMode(EncryptionMode.NO_ENCRYPTION);
        final String claims = "{\"name\": \"Alice Adams\"}";

        tested.encryptClaims(entity, claims);

        assertNotEquals("{\"name\": \"Alice Adams\"}", entity.getClaims());
        Base64.getDecoder().decode(entity.getClaims()); // would throw IllegalArgumentException if it is not in valid Base64
        assertEquals(EncryptionMode.AES_HMAC, entity.getEncryptionMode());
    }

    @Test
    void testNoEncryption_decrypt_keyConfigured() {
        final EncryptionService tested = new EncryptionService("MTIzNDU2Nzg5MDEyMzQ1Ng==");
        final UserClaimsEntity entity = new UserClaimsEntity();
        entity.setEncryptionMode(EncryptionMode.NO_ENCRYPTION);
        entity.setClaims("{\"name\": \"Alice Adams\"}");

        final String result = tested.decryptClaims(entity);

        assertEquals("{\"name\": \"Alice Adams\"}", result);
    }

    @Test
    void testNoEncryption_decrypt_keyNotConfigured() {
        final EncryptionService tested = new EncryptionService(null);
        final UserClaimsEntity entity = new UserClaimsEntity();
        entity.setEncryptionMode(EncryptionMode.NO_ENCRYPTION);
        entity.setClaims("{\"name\": \"Alice Adams\"}");

        final String result = tested.decryptClaims(entity);

        assertEquals("{\"name\": \"Alice Adams\"}", result);
    }

    @Test
    void testEncryption_AES_HMAC() {
        final EncryptionService tested = new EncryptionService("MTIzNDU2Nzg5MDEyMzQ1Ng==");
        final UserClaimsEntity entity = new UserClaimsEntity();
        entity.setUserId("alice.adams");

        tested.encryptClaims(entity, "{\"name\": \"Alice Adams\"}");

        assertNotEquals("{\"name\": \"Alice Adams\"}", entity.getClaims());
        Base64.getDecoder().decode(entity.getClaims()); // would throw IllegalArgumentException if it is not in valid Base64
        assertEquals(EncryptionMode.AES_HMAC, entity.getEncryptionMode());

        final String result = tested.decryptClaims(entity);
        assertEquals("{\"name\": \"Alice Adams\"}", result);
    }
}
