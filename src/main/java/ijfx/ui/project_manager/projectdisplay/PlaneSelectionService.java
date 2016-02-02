/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.ui.project_manager.projectdisplay;

import ijfx.core.project.Project;
import ijfx.core.project.imageDBService.PlaneDB;
import java.util.List;
import javafx.scene.control.TreeItem;
import net.imagej.ImageJService;

/**
 *
 * @author cyril
 */
public interface PlaneSelectionService extends ImageJService{
    
    
    
    public void selectPlane(Project project, PlaneDB planeDB);
    
    public void selectPlanes(Project project,List<PlaneDB> planeDB);
    
    public void setPlaneSelection(Project project, PlaneDB planeDB, boolean selected);
    
    public void setPlaneSelection(Project project, List<PlaneDB> planeList, boolean selected);
    
    public void setPlaneSelection(Project project, TreeItem<PlaneOrMetaData> planeList, boolean selected);
    
    public List<PlaneDB> getSelectedPlane(Project project);
    
    
    
}
