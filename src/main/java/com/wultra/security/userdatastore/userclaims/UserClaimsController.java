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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller providing API for CRUD of user claims.
 *
 * @author Lubos Racansky lubos.racansky@wultra.com
 */
@RestController
class UserClaimsController {

    private static final Logger logger = LoggerFactory.getLogger(UserClaimsController.class);

    private final UserClaimsService userClaimsService;

    UserClaimsController(UserClaimsService userClaimsService) {
        this.userClaimsService = userClaimsService;
    }

    /**
     * Return claims for the given user.
     *
     * @param userId user identifier
     * @return user claims
     */
    @GetMapping("/private/user/{userId}/claims")
    public Object userClaims(@PathVariable final String userId) {
        logger.info("Fetching claims of user ID: {}", userId);
        return userClaimsService.fetchUserClaims(userId);
    }

    /**
     * Create claims for the given user or update the exiting ones.
     *
     * @param userId user identifier
     * @param claims claims to be stored
     */
    @PostMapping("/public/user/{userId}/claims")
    @ResponseStatus(HttpStatus.CREATED)
    public void createOrUpdate(@PathVariable final String userId, @RequestBody final Object claims) {
        logger.info("Creating or updating claims of user ID: {}", userId);
        userClaimsService.createOrUpdateUserClaims(userId, claims);
    }

    /**
     * Delete claims of the given user.
     *
     * @param userId user identifier
     */
    @DeleteMapping("/public/user/{userId}/claims")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final String userId) {
        logger.info("Deleting claims of user ID: {}", userId);
        userClaimsService.deleteUserClaims(userId);
    }
}
