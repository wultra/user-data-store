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

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity for import result.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "uds_import_result")
@Getter
@Setter
public class ImportResultEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 743666156963790739L;

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "import_path")
    private String importPath;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "document_id", nullable = false)
    private String documentId;

    @Column(name = "photo_id")
    private String photoId;

    @Column(name = "attachment_id")
    private String attachmentId;

    @Column(name = "imported", nullable = false)
    private boolean imported;

    @Column(name = "error")
    private String error;

    @Column(name = "timestamp_created", nullable = false)
    private LocalDateTime timestampCreated = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportResultEntity that = (ImportResultEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
