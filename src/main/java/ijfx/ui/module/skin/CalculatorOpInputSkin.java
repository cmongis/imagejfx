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

import ijfx.ui.module.InputSkinPlugin;
import ijfx.ui.module.input.Input;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import net.imagej.operator.CalculatorOp;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

/**
 *
 * @author Tuan anh TRINH
 */

@Plugin(type = InputSkinPlugin.class)
public class CalculatorOpInputSkin   extends AbstractInputSkinPlugin<CalculatorOp>{
    
    ObjectProperty<CalculatorOp> calculatorOpProperty = new SimpleObjectProperty();
    
    
    
    ComboBox<CalculatorOp> calculatorOpComboBox = new ComboBox<>();
    
    List<CalculatorOp> calculatorOpList;

    @Parameter
    PluginService pluginService;
    
    
    @Override
    public Property valueProperty() {
        return calculatorOpProperty;
    }

    @Override
    public Node getNode() {
        return calculatorOpComboBox;
    }

    @Override
    public void dispose() {
        
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
      
        return clazz == CalculatorOp.class;
    }
    
    public List<CalculatorOp> getCalculatorOpList() {
        if(calculatorOpList == null) {
            calculatorOpList = pluginService.createInstancesOfType(CalculatorOp.class);

        }
        
        return calculatorOpList;
    }

    @Override
    public void init(Input<CalculatorOp> input) {
        System.out.println("Initiliazing");
        calculatorOpComboBox.getItems().addAll(getCalculatorOpList());
        calculatorOpComboBox.getSelectionModel().selectedItemProperty().addListener((obs,oldValue,newValue)->calculatorOpProperty.setValue(newValue));
    }
    
}