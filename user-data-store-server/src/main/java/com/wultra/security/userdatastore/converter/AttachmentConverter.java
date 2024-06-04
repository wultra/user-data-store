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

import com.wultra.security.userdatastore.model.dto.AttachmentDto;
import com.wultra.security.userdatastore.model.dto.DocumentDto;
import com.wultra.security.userdatastore.model.dto.PhotoDto;
import com.wultra.security.userdatastore.model.entity.AttachmentEntity;
import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import com.wultra.security.userdatastore.model.entity.PhotoEntity;
import com.wultra.security.userdatastore.model.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Converter for attachments.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class AttachmentConverter {

    private final DocumentRepository documentRepository;

    /**
     * Converter constructor.
     * @param documentRepository Document repository.
     */
    @Autowired
    public AttachmentConverter(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * Convert {@link AttachmentDto} to {@link AttachmentEntity}.
     * @param attachment Attachment DTO.
     * @return Attachment entity.
     */
    public AttachmentEntity toAttachmentEntity(final AttachmentDto attachment) {
        if (attachment == null) {
            return null;
        }

        final Optional<DocumentEntity> documentEntityOptional = documentRepository.findById(attachment.documentId());
        if (documentEntityOptional.isEmpty()) {
            return null;
        }

        final AttachmentEntity entity = new AttachmentEntity();
        entity.setId(attachment.id());
        entity.setDocument(documentEntityOptional.get());
        entity.setAttachmentData(attachment.attachmentData());
        entity.setAttachmentType(attachment.attachmentType());
        entity.setExternalId(attachment.externalId());
        entity.setTimestampCreated(attachment.timestampCreated());
        entity.setTimestampLastUpdated(attachment.timestampLastUpdated());
        return entity;
    }

    /**
     * Convert {@link AttachmentEntity} to {@link AttachmentDto}.
     * @param entity Attachment entity.
     * @return Attachment DTO.
     */
    public AttachmentDto toAttachment(final AttachmentEntity entity) {
        if (entity == null) {
            return null;
        }

        return AttachmentDto.builder()
                .id(entity.getId())
                .documentId(entity.getDocument().getId())
                .attachmentData(entity.getAttachmentData())
                .attachmentType(entity.getAttachmentType())
                .externalId(entity.getExternalId())
                .timestampCreated(entity.getTimestampCreated())
                .timestampLastUpdated(entity.getTimestampLastUpdated())
                .build();
    }

}

