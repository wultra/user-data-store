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
package com.wultra.security.userdatastore.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.util.ProxyUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity for user claims.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Entity
@Table(name = "uds_user_claims")
@Getter
@Setter
public class UserClaimsEntity {

    @Id
    private String userId;

    /**
     * Format of the data depends on the value of {@link #encryptionMode}.
     * If encrypted, the key is derived from the master server key and {@link #userId}.
     */
    @Column(nullable = false)
    private String claims;

    @Enumerated(EnumType.STRING)
    private EncryptionMode encryptionMode;

    @Column(nullable = false)
    private LocalDateTime timestampCreated = LocalDateTime.now();

    @Column
    private LocalDateTime timestampLastUpdated;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !this.getClass().equals(ProxyUtils.getUserClass(o))) return false;
        UserClaimsEntity that = (UserClaimsEntity) o;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
