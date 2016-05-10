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
package ijfx.ui.module.skin;

import ijfx.plugins.ProjectionMethod;
import ijfx.ui.module.InputSkinPlugin;
import ijfx.ui.module.input.Input;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = InputSkinPlugin.class)
public class ProjectionMethodInputSkin   extends AbstractInputSkinPlugin<ProjectionMethod>{
    
    ObjectProperty<ProjectionMethod> projectionMethodProperty = new SimpleObjectProperty();
    
    
    
    ComboBox<ProjectionMethod> projectionMethodComboBox = new ComboBox<>();
    
    List<ProjectionMethod> projectionMethodList;

    @Parameter
    PluginService pluginService;
    
    
    @Override
    public Property valueProperty() {
        return projectionMethodProperty;
    }

    @Override
    public Node getNode() {
        return projectionMethodComboBox;
    }

    @Override
    public void dispose() {
        
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        System.out.println("can i handle ?"+clazz);
        return clazz == ProjectionMethod.class;
    }
    
    public List<ProjectionMethod> getProjectionMethodList() {
        if(projectionMethodList == null) {
            projectionMethodList = pluginService.createInstancesOfType(ProjectionMethod.class);

        }
        
        return projectionMethodList;
    }

    @Override
    public void init(Input<ProjectionMethod> input) {
        System.out.println("Initiliazing");
        projectionMethodComboBox.getItems().addAll(getProjectionMethodList());
        projectionMethodComboBox.getSelectionModel().selectedItemProperty().addListener((obs,oldValue,newValue)->projectionMethodProperty.setValue(newValue));
    }
    
}
