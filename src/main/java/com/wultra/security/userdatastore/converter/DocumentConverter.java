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

import com.wultra.security.userdatastore.model.dto.DocumentDto;
import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import org.springframework.stereotype.Component;

/**
 * Converter for documents.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class DocumentConverter {

    /**
     * Convert {@link DocumentDto} to {@link DocumentEntity}.
     * @param document Document DTO.
     * @return Document entity.
     */
    public DocumentEntity toDocumentEntity(final DocumentDto document) {
        if (document == null) {
            return null;
        }

        final DocumentEntity entity = new DocumentEntity();
        entity.setId(document.id());
        entity.setUserId(document.userId());
        entity.setDocumentType(document.documentType());
        entity.setDataType(document.dataType());
        entity.setDocumentDataId(document.documentDataId());
        entity.setExternalId(document.externalId());
        entity.setDocumentData(document.documentData());
        entity.setTimestampCreated(document.timestampCreated());
        entity.setTimestampLastUpdated(document.timestampLastUpdated());
        return entity;
    }

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
                .documentData(entity.getDocumentData())
                .timestampCreated(entity.getTimestampCreated())
                .timestampLastUpdated(entity.getTimestampLastUpdated())
                .build();
    }

}

