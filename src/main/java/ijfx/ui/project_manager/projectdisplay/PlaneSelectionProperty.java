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
import javafx.beans.Observable;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.value.WeakChangeListener;

/**
 *
 * @author cyril
 */
public class PlaneSelectionProperty extends BooleanPropertyBase{

    Object bean;
    
    private final PlaneSelectionService service;
    
    private final Project project;
    
   private final PlaneDB plane;

   public static final String NAME =  "planeSelectionProperty";
   
    public PlaneSelectionProperty(PlaneSelectionService service, Project project, PlaneDB plane) {
        this.service = service;
        this.project = project;
        this.plane = plane;
        
        plane.selectedProperty().addListener(new WeakChangeListener<>(this::onPlaneSelectionChanged));
        
    }

   
   
    
    @Override
    public boolean get() {
        return plane.selectedProperty().get();//service.isPlaneSelected(project, plane);
    }
    
    @Override
    public void set(boolean value) {
        System.out.println("They are setting me !");
        service.setPlaneSelection(project, plane,value);
        
    }

    @Override
    public Object getBean() {
        return plane;
    }

    @Override
    public String getName() {
        return NAME;
    }
    
    public void onPlaneSelectionChanged(Observable obs, Boolean oldValue, Boolean newValue) {
        fireValueChangedEvent();
        
    }
}
