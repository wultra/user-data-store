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

/**
 * Exception to be thrown when the user's request is invalid.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
class InvalidRequestException extends RuntimeException {

    /**
     * No-arg constructor.
     */
    public InvalidRequestException() {
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause cause
     */
    public InvalidRequestException(Throwable cause) {
        super(cause);
    }
}
