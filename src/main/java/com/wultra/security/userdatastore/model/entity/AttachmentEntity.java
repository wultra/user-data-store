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
package com.wultra.security.userdatastore.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.util.ProxyUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity for attachments.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "uds_attachment")
@Getter
@Setter
public class AttachmentEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6667938502679127302L;

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @ManyToOne
    @JoinColumn(name = "document_id", referencedColumnName = "id", nullable = false)
    private DocumentEntity document;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "attachment_type", nullable = false)
    private String attachmentType;

    @Column(name = "attachment_data", nullable = false)
    private String attachmentData;

    @Enumerated(EnumType.STRING)
    @Column(name = "encryption_mode", nullable = false)
    private EncryptionMode encryptionMode;

    @Column(name = "timestamp_created", nullable = false)
    private LocalDateTime timestampCreated = LocalDateTime.now();

    @Column(name = "timestamp_last_updated")
    private LocalDateTime timestampLastUpdated;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !this.getClass().equals(ProxyUtils.getUserClass(o))) return false;
        AttachmentEntity that = (AttachmentEntity) o;
        return document.equals(that.document) && attachmentType.equals(that.attachmentType) && timestampCreated.equals(that.timestampCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document, attachmentType, timestampCreated);
    }
    
}
