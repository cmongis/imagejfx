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
import ijfx.ui.project_manager.project.TreeItemUtils;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;
import javafx.util.Pair;

/**
 *
 * @author cyril
 */
public class ImageTreeService {

    public static void placePlane(PlaneDB planeDB, ProjectTreeItem root, List<String> hierarchy) {

        // we progressively go down the tree to find the node corresponding to
        // the hierarchy
        ProjectTreeItem currentNode = root;
        //System.out.println("################# Placing plane "+planeDB.toString());
        for (String keyName : hierarchy) {
            //System.out.println("hierarchy "+hierarchy);
            // retrieve the metadata from the plane or create a metadata with missing inside
            // in order to compensate the lack of metadata
            MetaData metadata = planeDB.getMetaDataSet().getOrDefault(keyName, new GenericMetaData(keyName, 0));

            // the parent node become the found one
            currentNode = findNode(metadata, currentNode);
            
            System.out.println(currentNode);

        }

        // the loop was repeated until finding the bottom of the hierarchy
        // so we put a last leaf containing the plane
        currentNode.getChildren().add(new ProjectTreeItem(planeDB));
        //System.out.println("############### NODE PLACED #############");
    }

    public static void placePlanes(List<PlaneDB> planeList, ProjectTreeItem root, List<String> hieararchy) {


        
        for (PlaneDB plane : planeList) {
            placePlane(plane, root, hieararchy);
        }

    }

    public static void removePlane(PlaneDB plane, ProjectTreeItem root) {
        
        
        List<Pair<TreeItem,TreeItem>> toRemove = new ArrayList<>();
        
        TreeItemUtils.goThroughLeaves(root, leaf->{
            if(leaf.getValue() == null) return;
            if(leaf.getValue().getPlaneDB() == plane || (leaf.getValue().isPlane() == false && leaf.getChildren().isEmpty())) {
                toRemove.add(new Pair<>(leaf.getParent(),leaf));
            }
            
            /*
            if(parent.getChildren().size() == 0) {
                leaf = parent;
                parent = parent.getParent();
                parent.getChildren().remove(leaf);
            }*/

        });
        
        for(Pair<TreeItem,TreeItem> pair : toRemove) {
            pair.getKey().getChildren().remove(pair.getValue());
        }
       
    }

    public static void removePlanes(List<? extends PlaneDB> planeList, ProjectTreeItem root) {
        for (PlaneDB plane : planeList) {
            removePlane(plane, root);
        }
    }

    public static void deleteEmptyNodes(ProjectTreeItem root) {
        if (true) {
            return;
        }
        TreeItemUtils.goThrough(root, treeItem -> {
            TreeItem parentNode = treeItem.getParent();
            if (treeItem.getChildren().size() == 0) {
                parentNode.getChildren().remove(treeItem);
            }
        });
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
        } else {
           
            return (ProjectTreeItem) filtered.get(0);
        }
    }

}


