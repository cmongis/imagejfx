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
package ijfx.ui.project_manager.projectview;

import ijfx.core.project.Project;
import ijfx.core.project.imageDBService.PlaneDB;
import java.util.List;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import mercury.core.MercuryTimer;

/**
 *
 * @author cyril
 */
public class DefaultPlaneSet implements PlaneSet<PlaneDB>{
    
    private Property<String> nameProperty = new SimpleObjectProperty<>();
    
    private Property<ProjectTreeItem> currentItemProperty =  new SimpleObjectProperty<>();
    
    private final Property<ProjectTreeItem> rootProperty = new SimpleObjectProperty<>();
    
    private ObservableList<PlaneDB> planeList;
    
    private final Project project;

    public DefaultPlaneSet(Project project) {
        this.project = project;
        setName("All images");
        planeList = project.getImages();
        planeList.addListener(this::onListChanged);
        updateTree();
        
    }

    private void onListChanged(ListChangeListener.Change<? extends PlaneDB> change) {
        
        // placing all the add planes into the tree
        
        while(change.next())
        
        change.getAddedSubList().forEach(plane->ImageTreeService.placeImage(plane, getRoot(), project.getHierarchy()));
        
    }
    
    private void updateTree() {
        
        MercuryTimer t = new MercuryTimer("updateTree");
        
        setRoot(new ProjectTreeItem());
        t.start();
        ImageTreeService.planePlaneList(planeList, rootProperty.getValue(), project.getHierarchy());
        t.elapsed("Tree placement");
    }
    
    
    

    @Override
    public ObservableList<PlaneDB> getPlaneList() {
        return planeList;
    }

    @Override
    public ProjectTreeItem getRoot() {
        return rootProperty.getValue();
    }

    @Override
    public void setRoot(ProjectTreeItem item) {
        rootProperty.setValue(item);
    }

    @Override
    public Property<ProjectTreeItem> rootProperty() {
        return rootProperty;
    }

    @Override
    public Property<ProjectTreeItem> currentItemProperty() {
        return currentItemProperty;
    }

    @Override
    public void setCurrentItem(ProjectTreeItem item) {
        currentItemProperty.setValue(item);
    }

    @Override
    public ProjectTreeItem getCurrentItem() {
        return currentItemProperty.getValue();
    }

    @Override
    public String getName() {
        return nameProperty.getValue();
    }

    @Override
    public void setName(String name) {
        nameProperty.setValue(name);
    }
    
    public void addImage(PlaneDB planeDB) {
        addImage(planeDB,project.getHierarchy());
    }
    
    private void addImage(PlaneDB planeDB,List<String> hierarchy) {
        ImageTreeService.placeImage(planeDB, rootProperty.getValue(), hierarchy);
    }

    @Override
    public void dispose() {
    }
    
    
    
    
}
