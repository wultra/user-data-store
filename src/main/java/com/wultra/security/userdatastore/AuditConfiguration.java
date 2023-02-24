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

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.AuditFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration of auditing.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Configuration("userDataStoreAuditConfiguration")
@ComponentScan("com.wultra.core.audit.base")
@EnableScheduling
@Slf4j
public class AuditConfiguration {

    /**
     * Prepare audit interface.
     *
     * @return Audit interface.
     */
    @Bean
    public Audit audit(final AuditFactory auditFactory) {
        logger.info("Initializing Audit");
        return auditFactory.getAudit();
    }
}
