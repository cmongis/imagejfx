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
package ijfx.ui.project_manager.project;

import ijfx.core.metadata.MetaData;
import ijfx.core.project.Project;
import ijfx.core.project.imageDBService.PlaneDB;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import net.imagej.ImageJService;

/**
 *
 * @author Cyril Quinton
 */
public interface ProjectViewModelService extends ImageJService{

    public ProjectViewModel getViewModel(Project project);
    
    public static boolean containsPlane(TreeItem item) {
        return PlaneDB.class.isAssignableFrom(item.getValue().getClass());
    }
    
    public static boolean containsMetaData(TreeItem item) {
        return MetaData.class.isAssignableFrom(item.getValue().getClass());
    }
    
    public static boolean containsObjectOfType(TreeItem item, Class<?> type) {
        return type.isAssignableFrom(item.getValue().getClass());
    }
    
    

}
