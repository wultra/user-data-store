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
package com.wultra.security.userdatastore.client.model.error;

import java.io.Serial;

/**
 * User Data Store client exception.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserDataStoreClientException extends Exception {

    @Serial
    private static final long serialVersionUID = 7677237399256399571L;

    /**
     * No-arg constructor.
     */
    public UserDataStoreClientException() {
    }

    /**
     * Constructor with message.
     * @param message Error message.
     */
    public UserDataStoreClientException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause.
     * @param message Error message.
     * @param cause Exception which caused the error.
     */
    public UserDataStoreClientException(String message, Throwable cause) {
        super(message, cause);
    }

}