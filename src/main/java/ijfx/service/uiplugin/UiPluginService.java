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

import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.UiPluginSorter;
import java.util.Collection;
import javafx.concurrent.Task;
import net.imagej.ImageJService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.Node;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public interface UiPluginService extends ImageJService,UiPluginSorter<Node>{

    UiConfiguration getInfos(UiPlugin plugin);

    Collection<UiPlugin> getUiPluginList();
    
    Task<Collection<UiPlugin>> loadAll(boolean startNow);
    
    void reload(Class<? extends UiPlugin> widget);
 
    String getDefaultLocalization(UiPlugin uiPlugin);
    
    String getLocalization(String id);
    
    default String getLocalization(UiPlugin uiPlugin) {
        return getLocalization(getInfos(uiPlugin).id());
    }
    
    public Double getOrder(String id);
    
    public Double setOrder(String id, Double order);
    
    public Double getDefaultOrder(UiPlugin id);
    
    
     
    
}
