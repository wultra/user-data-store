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

import io.getlime.security.powerauth.crypto.lib.generator.KeyGenerator;
import io.getlime.security.powerauth.crypto.lib.model.exception.CryptoProviderException;
import io.getlime.security.powerauth.crypto.lib.model.exception.GenericCryptoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Service for encryption and decryption database data.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Service
@Slf4j
class EncryptionService {

    @Value("${user-data-store.db.master.encryption.key}")
    private String masterDbEncryptionKeyBase64;

    private final KeyGenerator keyGenerator = new KeyGenerator();

    public String decryptClaims(final UserClaimsEntity entity) {
        // TODO
        return entity.getClaims();
    }

    public void encryptClaims(final UserClaimsEntity entity, final String claims) {
        // TODO
        entity.setClaims(claims);
    }

//    public Object x() {
//        if (!StringUtils.hasText(masterDbEncryptionKeyBase64)) {
//            logger.debug("masterDbEncryptionKey is not configured");
//            return null;
//        }
//        final SecretKey secretKey = convertBytesToSharedSecretKey(Base64.getDecoder().decode(masterDbEncryptionKeyBase64));
//
//        // IV is present in first 16 bytes
//        byte[] iv = Arrays.copyOfRange(serverPrivateKeyBytes, 0, 16);
//
//        // Encrypted serverPrivateKey is present after IV
//        byte[] encryptedServerPrivateKey = Arrays.copyOfRange(serverPrivateKeyBytes, 16, serverPrivateKeyBytes.length);
//
//        // Decrypt serverPrivateKey
//        byte[] decryptedServerPrivateKey = aesEncryptionUtils.decrypt(encryptedServerPrivateKey, iv, secretKey);
//
//        return Base64.getEncoder().encode(decryptedServerPrivateKey);
//    }

    private static SecretKey convertBytesToSharedSecretKey(final byte[] bytesSecretKey) {
        return new SecretKeySpec(bytesSecretKey, "AES");
    }

    /**
     * Derive secret key from master DB encryption key, user ID and activation ID.<br/>
     * <br/>
     * See: https://github.com/wultra/powerauth-server/blob/develop/docs/Encrypting-Records-in-Database.md
     *
     * @param masterDbEncryptionKey Master DB encryption key.
     * @param userId User ID.
     * @return Derived secret key.
     * @throws GenericCryptoException In case key derivation fails.
     */
    private SecretKey deriveSecretKey(SecretKey masterDbEncryptionKey, String userId) throws GenericCryptoException, CryptoProviderException {
        // Use user ID bytes as index for KDF_INTERNAL
        byte[] index = (userId).getBytes(StandardCharsets.UTF_8);

        // Derive secretKey from master DB encryption key using KDF_INTERNAL with constructed index
        return keyGenerator.deriveSecretKeyHmac(masterDbEncryptionKey, index);
    }

}
