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
 * Entity for user documents.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "uds_document")
@Getter
@Setter
public class DocumentEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -383789783213592918L;

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "data_type", nullable = false)
    private String dataType;

    @Column(name = "document_data_id")
    private String documentDataId;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "document_data", nullable = false, columnDefinition = "CLOB")
    private String documentData;

    @Column(name = "attributes", nullable = false)
    private String attributes;

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
        DocumentEntity that = (DocumentEntity) o;
        return userId.equals(that.userId) && documentType.equals(that.documentType) && dataType.equals(that.dataType) && documentDataId.equals(that.documentDataId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, documentType, dataType, documentDataId);
    }

}
