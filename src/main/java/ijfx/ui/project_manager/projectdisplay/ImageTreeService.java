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

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.project.imageDBService.PlaneDB;
import java.util.List;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;

/**
 *
 * @author cyril
 */
public class ImageTreeService {

    public static void placeImage(PlaneDB planeDB, ProjectTreeItem root, List<String> hierarchy) {

        // we progressively go down the tree to find the node corresponding to
        // the hierarchy
        ProjectTreeItem currentNode = root;
        
        for(String keyName : hierarchy) {
            
            // retrieve the metadata from the plane or create a metadata with missing inside
            // in order to compensate the lack of metadata
            MetaData metadata = planeDB.getMetaDataSet().getOrDefault(keyName, new GenericMetaData(keyName, 0));
            
            // the parent node become the found one
            currentNode = findNode(metadata, currentNode);
            
     
            
        }
        
        // the loop was repeated until finding the bottom of the hierarchy
        // so we put a last leaf containing the plane
        currentNode.getChildren().add(new ProjectTreeItem(planeDB));
    }

    public static void planePlaneList(List<PlaneDB> planeList, ProjectTreeItem root, List<String> hieararchy) {
        
        for( PlaneDB plane : planeList) {
            placeImage(plane, root, hieararchy);
        }
        
        
    }
    
    public static ProjectTreeItem findNode(MetaData metadata, ProjectTreeItem root) {

        // find the child with the value of the plane
        FilteredList<TreeItem<PlaneOrMetaData>> filtered = root.getChildren().filtered(child
                -> child.getValue().isMetaData()
                && child.getValue().getMetaData().equals(metadata));

        if (filtered.size() == 0) {
            ProjectTreeItem newItem = new ProjectTreeItem(metadata);
            root.getChildren().add(newItem);
            return newItem;
        }
        else return (ProjectTreeItem)filtered.get(0);

       

    }

}
