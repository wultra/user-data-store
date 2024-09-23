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
package com.wultra.security.userdatastore.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Security configuration class.
 *
 * @author Lubos Racansky lubos.racansky@wultra.com
 */
@Configuration
@Slf4j
public class SecurityConfiguration {

    private static final String USERS_BY_USERNAME_QUERY = "select username,password,enabled from uds_users where username = ?";

    private static final String AUTHORITIES_BY_USERNAME_QUERY = "select username,authority from uds_authorities where username = ?";

    private static final String SHA_256 = "SHA-256";

    @Bean
    public UserDetailsService userDetailsService(final DataSource dataSource) {
        logger.info("bean: init, type: UserDetailsService, implementation: JdbcDaoImpl");
        final JdbcDaoImpl jdbcDao = new JdbcDaoImpl();
        jdbcDao.setDataSource(dataSource);
        jdbcDao.setUsersByUsernameQuery(USERS_BY_USERNAME_QUERY);
        jdbcDao.setAuthoritiesByUsernameQuery(AUTHORITIES_BY_USERNAME_QUERY);
        return jdbcDao;
    }

    /**
     * Configure SHA-256 or bcrypt password encoder. Note that since the passwords are technical, using old SHA-256
     * algorithm does not cause security issues. Bcrypt is used as default in case no prefix is specified.
     * See the following URL for constant details:
     * <a href="https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/crypto/factory/PasswordEncoderFactories.html#createDelegatingPasswordEncoder()">PasswordEncoderFactories.createDelegatingPasswordEncoder()</a>
     *
     * @return Delegating password encoder.
     */
    @Bean
    @SuppressWarnings({"deprecation", "java:S5344"})
    public PasswordEncoder passwordEncoder() {
        logger.info("bean: init, type: PasswordEncoder, implementation: DelegatingPasswordEncoder, default: {}", SHA_256);
        final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        final MessageDigestPasswordEncoder sha256 = new MessageDigestPasswordEncoder(SHA_256);
        final Map<String, PasswordEncoder> encoders = Map.of(
                "bcrypt", bcrypt,
                SHA_256, sha256
        );
        final DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder(SHA_256, encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(sha256); // try using sha256 as default, for technical accounts
        return passwordEncoder;
    }
}
