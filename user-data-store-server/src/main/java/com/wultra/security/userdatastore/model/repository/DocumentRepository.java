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
package com.wultra.security.userdatastore.model.repository;

import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link DocumentEntity}.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    /**
     * Find all documents for the given user, optionally filtered by document type.
     *
     * @param userId user identifier
     * @param documentType optional document type; when {@code null}, the filter is not applied
     * @return matching documents
     */
    @Query("SELECT d FROM DocumentEntity d WHERE d.userId = :userId AND (:documentType IS NULL OR d.documentType = :documentType)")
    List<DocumentEntity> findAllByUserId(@Param("userId") String userId, @Param("documentType") @Nullable String documentType);

    List<DocumentEntity> findAllByUserIdAndDataType(String userId, String dataType);

    void deleteAllByUserId(String userId);

    int deleteAllByUserIdAndId(String userId, String id);

}
