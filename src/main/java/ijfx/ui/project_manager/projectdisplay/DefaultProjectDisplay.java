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
import java.util.ArrayList;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 *
 * @author cyril
 */
public class DefaultProjectDisplay extends ArrayList<PlaneSet> implements ProjectDisplay {

    final private Project project;

    final private ObservableList<PlaneSet> planeSetList = FXCollections.observableArrayList();
    
     private TreeItem<PlaneOrMetaData> currentItem;
    
     private final Property<PlaneSet> currentPlaneSetProperty = new SimpleObjectProperty<>();
     
    public DefaultProjectDisplay(Project project) {
        this.project = project;
        planeSetList.add(new DefaultPlaneSet(project));
        currentPlaneSetProperty.setValue(planeSetList.get(0));
    }
    
    
    
    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public ObservableList<PlaneSet> getPlaneSetList() {
        return planeSetList;
    }

    @Override
    public Property<PlaneSet> currentPlaneSetProperty() {
        return currentPlaneSetProperty;
    }

    @Override
    public PlaneSet getCurrentPlaneSet() {
        return currentPlaneSetProperty.getValue();
    }

    @Override
    public void setCurrentPlaneSet(PlaneSet planeSet) {
        currentPlaneSetProperty.setValue(planeSet);
    }
    
    
    
    public void onHierarchyChange(ListChangeListener.Change<? extends String> change) {
        
    }

    
    
    @Override
    public PlaneSet getPlaneSet(String id) {
        return planeSetList
                .stream()
                .filter(planeSet->planeSet.getName().equals(id))
                .findFirst()
                .orElse(null);
    }

    
    
    
}
