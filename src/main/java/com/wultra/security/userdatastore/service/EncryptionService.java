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
package com.wultra.security.userdatastore.service;

import com.wultra.security.userdatastore.model.entity.*;
import com.wultra.security.userdatastore.model.error.EncryptionException;
import io.getlime.security.powerauth.crypto.lib.generator.KeyGenerator;
import io.getlime.security.powerauth.crypto.lib.model.exception.CryptoProviderException;
import io.getlime.security.powerauth.crypto.lib.model.exception.GenericCryptoException;
import io.getlime.security.powerauth.crypto.lib.util.AESEncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Service for encryption and decryption database data.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
@Slf4j
class EncryptionService {

    private final String masterDbEncryptionKeyBase64;

    private final KeyGenerator keyGenerator = new KeyGenerator();
    private final AESEncryptionUtils aesEncryptionUtils = new AESEncryptionUtils();

    @Autowired
    public EncryptionService(@Value("${user-data-store.db.master.encryption.key}") String masterDbEncryptionKeyBase64) {
        if (!StringUtils.hasText(masterDbEncryptionKeyBase64)) {
            logger.warn("masterDbEncryptionKey is not configured, claims will be stored in plain text");
        }
        this.masterDbEncryptionKeyBase64 = masterDbEncryptionKeyBase64;
    }

    /**
     * Decrypt claims of the given entity.
     *
     * @param entity user claims entity
     * @return decrypted claims
     */
    public String decryptClaims(final UserClaimsEntity entity) {
        final EncryptionMode encryptionMode = entity.getEncryptionMode();
        return switch (encryptionMode) {
            case NO_ENCRYPTION -> entity.getClaims();
            case AES_HMAC -> fromDBValue(entity.getUserId(), entity.getClaims());
        };
    }

    /**
     * Decrypt document data of the given entity.
     *
     * @param entity document entity
     * @return decrypted document data
     */
    public String decryptDocumentData(final DocumentEntity entity) {
        final EncryptionMode encryptionMode = entity.getEncryptionMode();
        return switch (encryptionMode) {
            case NO_ENCRYPTION -> entity.getDocumentData();
            case AES_HMAC -> fromDBValue(entity.getUserId(), entity.getDocumentData());
        };
    }

    /**
     * Decrypt photo data of the given entity.
     *
     * @param entity photo entity
     * @return decrypted photo data
     */
    public String decryptPhoto(final PhotoEntity entity) {
        final EncryptionMode encryptionMode = entity.getEncryptionMode();
        return switch (encryptionMode) {
            case NO_ENCRYPTION -> entity.getPhotoData();
            case AES_HMAC -> fromDBValue(entity.getDocument().getUserId(), entity.getPhotoData());
        };
    }

    /**
     * Decrypt attachment data of the given entity.
     *
     * @param entity attachment entity
     * @return decrypted attachment data
     */
    public String decryptAttachment(final AttachmentEntity entity) {
        final EncryptionMode encryptionMode = entity.getEncryptionMode();
        return switch (encryptionMode) {
            case NO_ENCRYPTION -> entity.getAttachmentData();
            case AES_HMAC -> fromDBValue(entity.getDocument().getUserId(), entity.getAttachmentData());
        };
    }

    /**
     * Encrypt the claims and set to the given entity.
     *
     * @param entity user claims entity to be modified
     * @param claims claims to encrypt
     */
    public void encryptClaims(final UserClaimsEntity entity, final String claims) {
        if (!StringUtils.hasText(masterDbEncryptionKeyBase64)) {
            entity.setEncryptionMode(EncryptionMode.NO_ENCRYPTION);
            entity.setClaims(claims);
        } else {
            entity.setEncryptionMode(EncryptionMode.AES_HMAC);
            entity.setClaims(toDBValue(entity.getUserId(), claims.getBytes(StandardCharsets.UTF_8)));
        }
    }

    /**
     * Encrypt the document data and set to the given entity.
     *
     * @param entity document entity to be modified
     * @param documentData document data to encrypt
     */
    public void encryptDocumentData(final DocumentEntity entity, final String documentData) {
        if (!StringUtils.hasText(masterDbEncryptionKeyBase64)) {
            entity.setEncryptionMode(EncryptionMode.NO_ENCRYPTION);
            entity.setDocumentData(documentData);
        } else {
            entity.setEncryptionMode(EncryptionMode.AES_HMAC);
            entity.setDocumentData(toDBValue(entity.getUserId(), documentData.getBytes(StandardCharsets.UTF_8)));
        }
    }

    /**
     * Encrypt the photo data and set to the given entity.
     *
     * @param entity photo entity to be modified
     * @param photoData photo data to encrypt
     */
    public void encryptPhoto(final PhotoEntity entity, final String photoData) {
        if (!StringUtils.hasText(masterDbEncryptionKeyBase64)) {
            entity.setEncryptionMode(EncryptionMode.NO_ENCRYPTION);
            entity.setPhotoData(photoData);
        } else {
            entity.setEncryptionMode(EncryptionMode.AES_HMAC);
            entity.setPhotoData(toDBValue(entity.getDocument().getUserId(), photoData.getBytes(StandardCharsets.UTF_8)));
        }
    }

    /**
     * Encrypt the attachment data and set to the given entity.
     *
     * @param entity attachment entity to be modified
     * @param attachmentData attachment data to encrypt
     */
    public void encryptAttachment(final AttachmentEntity entity, final String attachmentData) {
        if (!StringUtils.hasText(masterDbEncryptionKeyBase64)) {
            entity.setEncryptionMode(EncryptionMode.NO_ENCRYPTION);
            entity.setAttachmentData(attachmentData);
        } else {
            entity.setEncryptionMode(EncryptionMode.AES_HMAC);
            entity.setAttachmentData(toDBValue(entity.getDocument().getUserId(), attachmentData.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private String toDBValue(final String userId, final byte[] dataBytes) {
        final SecretKey secretKey = fetchDerivedKey(userId);

        try {
            final byte[] iv = keyGenerator.generateRandomBytes(16);
            final byte[] encrypted = aesEncryptionUtils.encrypt(dataBytes, iv, secretKey);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(iv);
            baos.write(encrypted);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (GenericCryptoException | CryptoProviderException | InvalidKeyException | IOException e) {
            logger.error("Unable to encrypt claims for user ID: {}", userId, e);
            throw new EncryptionException("Unable to encrypt claims for user ID: " + userId, e);
        }
    }

    private String fromDBValue(final String userId, final String data) {
        final SecretKey secretKey = fetchDerivedKey(userId);
        final byte[] dataBytes = Base64.getDecoder().decode(data);

        if (dataBytes.length < 16) {
            throw new EncryptionException("Invalid encrypted private key format - the byte array is too short");
        }

        // IV is present in first 16 bytes
        final byte[] iv = Arrays.copyOfRange(dataBytes, 0, 16);

        // Encrypted claims is present after IV
        final byte[] encryptedClaims = Arrays.copyOfRange(dataBytes, 16, dataBytes.length);

        try {
            final byte[] decryptedClaims = aesEncryptionUtils.decrypt(encryptedClaims, iv, secretKey);
            return new String(decryptedClaims, StandardCharsets.UTF_8);
        } catch (InvalidKeyException | GenericCryptoException | CryptoProviderException e) {
            logger.error("Unable to decrypt claims for user ID: {}", userId, e);
            throw new EncryptionException("Unable to decrypt claims for user ID: " + userId, e);
        }
    }


    private SecretKey fetchDerivedKey(final String userId) {
        if (!StringUtils.hasText(masterDbEncryptionKeyBase64)) {
            throw new EncryptionException("masterDbEncryptionKey is not configured");
        }

        final SecretKey masterDbEncryptionKey = convertBytesToSharedSecretKey(Base64.getDecoder().decode(masterDbEncryptionKeyBase64));
        return deriveSecretKey(masterDbEncryptionKey, userId);
    }

    private static SecretKey convertBytesToSharedSecretKey(final byte[] bytesSecretKey) {
        return new SecretKeySpec(bytesSecretKey, "AES");
    }

    /**
     * Derive secret key from master DB encryption key and user ID.
     *
     * @param masterDbEncryptionKey Master DB encryption key.
     * @param userId User ID, used as index for KDF_INTERNAL
     * @return Derived secret key.
     * @see <a href="https://github.com/wultra/powerauth-server/blob/develop/docs/Encrypting-Records-in-Database.md">Encrypting Records in Database</a>
     */
    private SecretKey deriveSecretKey(final SecretKey masterDbEncryptionKey, final String userId) {
        Assert.hasText(userId, "userId must not be blank");
        final byte[] index = userId.getBytes(StandardCharsets.UTF_8);
        try {
            return keyGenerator.deriveSecretKeyHmac(masterDbEncryptionKey, index);
        } catch (GenericCryptoException | CryptoProviderException e) {
            throw new EncryptionException("Unable to derive key for user ID: " + userId, e);
        }
    }
}
