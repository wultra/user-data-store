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

import com.wultra.security.userdatastore.model.entity.AttachmentEntity;
import com.wultra.security.userdatastore.model.entity.DocumentEntity;
import com.wultra.security.userdatastore.model.entity.PhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link AttachmentEntity}.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, String> {

    List<AttachmentEntity> findAllByUserId(String userId);

    List<AttachmentEntity> findAllByUserIdAndDocument(String userId, DocumentEntity documentEntity);

    int deleteAllByUserId(String userId);

    int deleteAllByUserIdAndDocument(String userId, DocumentEntity documentEntity);

}
