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
package ijfx.plugins.bunwarpJ;

import ijfx.ui.module.BeanInputWrapper;
import ijfx.ui.module.ConvertedChoiceBeanInputWrapper;
import ijfx.ui.module.InputSkinPluginService;
import ijfx.ui.module.MappedConverter;
import ijfx.ui.module.input.InputControl;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class BUnwarpJConfigurator extends GridPane {

    Property<bunwarpj.Param> parameterProperty = new SimpleObjectProperty();

    MappedConverter<Integer> modeMap = new MappedConverter("Fast",0,"Accurate",1,"Mono",2);
    
    int row = 0;

    @Parameter
    InputSkinPluginService inputSkinFactory;

    public BUnwarpJConfigurator() {
        parameterProperty.addListener(this::onParameterObjectChanged);
        setHgap(5);
        setVgap(10);

    }
    
    public void setObjectParameters(bunwarpj.Param params) {
        parameterProperty.setValue(params);
    }

    private void onParameterObjectChanged(Observable obs, bunwarpj.Param oldValue, bunwarpj.Param newValue) {
        row = 0;
        getChildren().clear();
        
        Platform.runLater(this::fillPane);
        
    }
    
    private void fillPane() {
        addModeRow("Mode","mode");
        addRow("Minimum scale deformation","min_scale_deformation",int.class);
        addRow("Maximum scale deformation","min_scale_deformation",int.class);
        addRow("Image subsampling factor","img_subsamp_fact",int.class);
        addRow("Division weight","divWeight",double.class);
        addRow("Curl Weighe","curlWeight",double.class);
        addRow("Landmark Weight","landmarkWeight",double.class);
        addRow("Image weight","imageWeight",double.class);
        addRow("Consistency Weight","consistencyWeight",double.class);
        addRow("Stop threshold","stopThreshold",double.class);
    }
    

    public void addRow(String label, String name, Class<?> clazz) {

        InputControl control = new InputControl(inputSkinFactory, new BeanInputWrapper(getParameterObject(), clazz, name));

        add(new Label(label), 0, row);
        add(control, 1, row);
        row++;
    }
    
    public  void addModeRow(String label, String name) {
        
        InputControl control = new InputControl(inputSkinFactory, new ConvertedChoiceBeanInputWrapper(getParameterObject(),name, modeMap));
        add(new Label(label), 0, row);
        add(control, 1, row);
        row++;
    }

    public bunwarpj.Param getParameterObject() {
        return parameterProperty.getValue();
    }

}
