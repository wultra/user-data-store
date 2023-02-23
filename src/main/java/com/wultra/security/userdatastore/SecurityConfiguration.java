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
package com.wultra.security.userdatastore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

import javax.sql.DataSource;

/**
 * Security configuration class.
 *
 * @author Lubos Racansky lubos.racansky@wultra.com
 */
@Configuration
@Slf4j
public class SecurityConfiguration {

    private static final String USERS_BY_USERNAME_QUERY = "select username,password,enabled from ud_users where username = ?";

    private static final String AUTHORITIES_BY_USERNAME_QUERY = "select username,authority from ud_authorities where username = ?";

    @Bean
    public UserDetailsService userDetailsService(final DataSource dataSource) {
        logger.info("Initializing JdbcDaoImpl as UserDetailsService");
        final JdbcDaoImpl jdbcDao = new JdbcDaoImpl();
        jdbcDao.setDataSource(dataSource);
        jdbcDao.setUsersByUsernameQuery(USERS_BY_USERNAME_QUERY);
        jdbcDao.setAuthoritiesByUsernameQuery(AUTHORITIES_BY_USERNAME_QUERY);
        return jdbcDao;
    }
}
