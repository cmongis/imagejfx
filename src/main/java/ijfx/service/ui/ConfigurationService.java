/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.service.ui;

import ijfx.ui.main.ImageJFX;
import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class ConfigurationService {

    static ConfigurationService instance;

    HashMap<String, Object> defaultValues = new HashMap<String, Object>();

    private ConfigurationService() {

    }

    public static ConfigurationService getInstance() {
        if (instance == null) {
            instance = new ConfigurationService();
        }

        return instance;
    }

    public Preferences getPreferences(String domain) {

        return Preferences.userRoot().node(ImageJFX.IMAGEJFX_PREF_NODE + domain);
    }

}
