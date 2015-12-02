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
package ijfx.service.uiplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javafx.scene.Node;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class UiPluginOrderingService extends HashMap<String, Double> {

    private UiPluginOrderingService() {
        super();
    }

    protected static UiPluginOrderingService instance;

    public static UiPluginOrderingService getInstance() {
        if (instance == null) {
            instance = new UiPluginOrderingService();
        }
        return instance;
    }

    public UiPluginOrderingService setOrder(String id, Double order) {
        put(id, order);
        return this;
    }

    public UiPluginOrderingService setOrder(Node node, Double order) {
        return setOrder(node.getId(), order);
    }

    public Double getOrder(String id) {
        if (containsKey(id) == false) {
            put(id, 1.0);
        }
        return get(id);
    }

   

   

}
