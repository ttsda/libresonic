/*
 This file is part of Libresonic.

 Libresonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Libresonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Libresonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2016 (C) Libresonic Authors
 Based upon Subsonic, Copyright 2009 (C) Sindre Mehus
 */
package org.libresonic.player.theme;

import org.libresonic.player.service.*;
import org.libresonic.player.domain.*;
import org.springframework.web.servlet.*;

import javax.servlet.http.*;
import java.util.*;

/**
 * Theme resolver implementation which returns the theme selected in the settings.
 *
 * @author Sindre Mehus
 */
public class LibresonicThemeResolver implements ThemeResolver {

    private SecurityService securityService;
    private SettingsService settingsService;
    private Set<String> themeIds;

    /**
    * Resolve the current theme name via the given request.
    *
    * @param request Request to be used for resolution
    * @return The current theme name
    */
    public String resolveThemeName(HttpServletRequest request) {
        String themeId = (String) request.getAttribute("libresonic.theme");
        if (themeId != null) {
            return themeId;
        }

        // Optimization: Cache theme in the request.
        themeId = doResolveThemeName(request);
        request.setAttribute("libresonic.theme", themeId);

        return themeId;
    }

    private String doResolveThemeName(HttpServletRequest request) {
        String themeId = null;

        // Look for user-specific theme.
        String username = securityService.getCurrentUsername(request);
        if (username != null) {
            UserSettings userSettings = settingsService.getUserSettings(username);
            if (userSettings != null) {
                themeId = userSettings.getThemeId();
            }
        }

        if (themeId != null && themeExists(themeId)) {
            return themeId;
        }

        // Return system theme.
        themeId = settingsService.getThemeId();
        return themeExists(themeId) ? themeId : "default";
    }

    /**
     * Returns whether the theme with the given ID exists.
     * @param themeId The theme ID.
     * @return Whether the theme with the given ID exists.
     */
    private synchronized boolean themeExists(String themeId) {
        // Lazily create set of theme IDs.
        if (themeIds == null) {
            themeIds = new HashSet<String>();
            Theme[] themes = settingsService.getAvailableThemes();
            for (Theme theme : themes) {
                themeIds.add(theme.getId());
            }
        }

        return themeIds.contains(themeId);
    }

    /**
     * Set the current theme name to the given one. This method is not supported.
     *
     * @param request   Request to be used for theme name modification
     * @param response  Response to be used for theme name modification
     * @param themeName The new theme name
     * @throws UnsupportedOperationException If the ThemeResolver implementation
     *                                       does not support dynamic changing of the theme
     */
    public void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName) {
        throw new UnsupportedOperationException("Cannot change theme - use a different theme resolution strategy");
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
}
