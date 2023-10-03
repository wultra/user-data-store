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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home controller to display nice welcome page.
 *
 * @author Lubos Racansky lubos.racansky@wultra.com
 */
@Controller
class HomeController {

    private BuildProperties buildProperties;

    public HomeController(@Autowired(required = false) final BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home(Model model) {
        if (buildProperties != null) {
            model.addAttribute("version", buildProperties.getVersion());
            model.addAttribute("built", buildProperties.getTime());
        } else {
            model.addAttribute("version", "UNKNOWN");
            model.addAttribute("built", "UNKNOWN");
        }
        return "index";
    }

}
