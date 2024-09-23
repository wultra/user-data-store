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
package com.wultra.security.userdatastore.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.security.userdatastore.client.model.dto.DocumentDto;
import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import com.wultra.security.userdatastore.service.EncryptionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Converter for documents.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
@Slf4j
@AllArgsConstructor
public class DocumentConverter {

    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final EncryptionService encryptionService;

    /**
     * Convert {@link DocumentEntity} to {@link DocumentDto}.
     * @param entity Document entity.
     * @return Document DTO.
     */
    public DocumentDto toDocument(final DocumentEntity entity) {
        if (entity == null) {
            return null;
        }

        return DocumentDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .documentType(entity.getDocumentType())
                .dataType(entity.getDataType())
                .documentDataId(entity.getDocumentDataId())
                .externalId(entity.getExternalId())
                .documentData(encryptionService.decryptDocumentData(entity))
                .attributes(convertAttributesToMap(entity.getAttributes()))
                .timestampCreated(entity.getTimestampCreated())
                .timestampLastUpdated(entity.getTimestampLastUpdated())
                .build();
    }

    public void convertAndSetAttributes(final Map<String, Object> attributes, final DocumentEntity documentEntity) {
        if (attributes == null) {
            documentEntity.setAttributes("{}");
        } else {
            try {
                documentEntity.setAttributes(OBJECT_MAPPER.writeValueAsString(attributes));
            } catch (JsonProcessingException e) {
                logger.warn("Invalid attributes, serialization error: ", e);
                documentEntity.setAttributes("{}");
            }
        }
    }

    private Map<String, Object> convertAttributesToMap(final String attributes) {
        if (attributes == null) {
            return Collections.emptyMap();
        } else {
            try {
                return OBJECT_MAPPER.readValue(attributes, new TypeReference<>(){});
            } catch (JsonProcessingException e) {
                logger.warn("Invalid attributes, deserialization error: ", e);
                return Collections.emptyMap();
            }
        }
    }

}

