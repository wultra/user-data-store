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
package com.wultra.security.userdatastore.model.error;

/**
 * Exception to be thrown when there is a problem at encryption layer.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
public class EncryptionException extends RuntimeException {

    /**
     * No-arg constructor.
     */
    public EncryptionException() {
    }

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message message
     */
    public EncryptionException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message message
     * @param cause cause
     */
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause cause
     */
    public EncryptionException(Throwable cause) {
        super(cause);
    }
}
