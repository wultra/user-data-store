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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class.
 *
 * @author Lubos Racansky lubos.racansky@wultra.com
 */
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http.csrf()
                .disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.DELETE, "/public/**")
                .hasRole("DELETE")
                .antMatchers(HttpMethod.POST, "/public/**")
                .hasRole("WRITE")
                .antMatchers(HttpMethod.GET, "/private/**")
                .hasRole("READ")
                .antMatchers("/swagger-ui*/**")
                .anonymous()
                .and()
                .build();
    }
}
