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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Web configuration class.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Value("${user-data-store.security.basic.realm}")
    private String realm;

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(httpBasic -> httpBasic.realmName(realm))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.DELETE, "/public/**")
                            .hasRole("WRITE")
                        .requestMatchers(HttpMethod.POST, "/public/**")
                            .hasRole("WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/admin/**")
                            .hasRole("WRITE")
                        .requestMatchers(HttpMethod.POST, "/admin/**")
                            .hasRole("WRITE")
                        .requestMatchers(HttpMethod.PUT, "/admin/**")
                            .hasRole("WRITE")
                        .requestMatchers(HttpMethod.GET, "/private/**")
                            .hasRole("READ")
                        .requestMatchers(HttpMethod.GET, "/**")
                            .hasRole("READ")
                        .anyRequest()
                            .permitAll()
                ).build();
    }

}
