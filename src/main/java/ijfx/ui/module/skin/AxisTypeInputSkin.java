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
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = InputSkinPlugin.class)
public class AxisTypeInputSkin extends AbstractInputSkinPlugin<AxisType> {

    ObjectProperty<AxisType> axisTypeProperty = new SimpleObjectProperty();

    ComboBox<AxisType> axisTypeComboBox = new ComboBox<>();

    List<AxisType> axisTypeList;

    @Parameter
    PluginService pluginService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Override
    public Property valueProperty() {
        return axisTypeProperty;
    }

    @Override
    public Node getNode() {
        return axisTypeComboBox;
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        System.out.println("can i handle ?" + clazz);
        return clazz == AxisType.class;
    }

    public List<AxisType> getAxisList() {
        if (axisTypeList == null) {
            axisTypeList = new ArrayList();
            Dataset dataset = imageDisplayService.getActiveDataset();
            CalibratedAxis[] calibratedAxis = new CalibratedAxis[dataset.numDimensions()];
            dataset.axes(calibratedAxis);
            for (CalibratedAxis ca : calibratedAxis) {
                if (ca.type() != Axes.X && ca.type() != Axes.Y) {
                    axisTypeList.add(ca.type());
                }
            }

        }

        return axisTypeList;
    }

    @Override
    public void init(Input<AxisType> input) {
        System.out.println("Initiliazing");
        axisTypeComboBox.getItems().addAll(getAxisList());
        axisTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> axisTypeProperty.setValue(newValue));
    }
}
