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
package ijfx.core.project;

import ijfx.core.project.imageDBService.PlaneDB;
import java.io.File;
import java.util.List;
import javafx.scene.control.TreeItem;
import net.imagej.ImageJService;

/**
 *
 * @author Cyril Quinton
 */
public interface ProjectModifierService extends ImageJService {

    void selectPlane(Project project, List<PlaneDB> planes, boolean select);

    void selectPlane(Project project, PlaneDB plane, boolean select);

    void selectPlane(Project project, List<PlaneDB> planesToUnselect, List<PlaneDB> planesToSelect);

    void updatePlaneSource(Project project, PlaneDB plane, File file, long planeIndex);
    
    void removePlaneFromProject(Project project, PlaneDB plane);

    void removePlaneFromProject(Project project, List<PlaneDB> planes);

    void removeSubPlanes(Project project, TreeItem rootItem);
    void selectSubImages(Project project, TreeItem rootItem, boolean select);

}
