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
package com.wultra.security.userdatastore.userclaims;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for user claims.
 *
 * @author Lubos Racansky lubos.racansky@wultra.com
 */
@Service
class UserClaimsService {

    // TODO (racansky, 2023-02-17, #4) implement persistence
    private static final Map<String, Object> data = new HashMap<>();

    public Object fetchUserClaims(final String userId) {
        return data.get(userId);
    }

    public void createOrUpdateUserClaims(final String userId, final Object claims) {
        data.put(userId, claims);
    }

    public void deleteUserClaims(final String userId) {
        data.remove(userId);
    }
}
