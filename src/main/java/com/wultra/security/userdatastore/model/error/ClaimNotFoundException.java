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
 * Exception to be thrown when the requested claim is not found.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
public class ClaimNotFoundException extends RuntimeException {

    /**
     * No-arg constructor.
     */
    public ClaimNotFoundException() {
    }

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message message
     */
    public ClaimNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause cause
     */
    public ClaimNotFoundException(Throwable cause) {
        super(cause);
    }
}