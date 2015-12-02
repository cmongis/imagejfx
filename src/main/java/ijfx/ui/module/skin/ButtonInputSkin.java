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
package ijfx.ui.module.skin;

import ijfx.ui.module.InputSkinPlugin;
import ijfx.ui.module.input.Input;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.widget.Button;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = InputSkinPlugin.class)
public class ButtonInputSkin extends AbstractInputSkinPlugin<Button> {

    javafx.scene.control.Button button;

    ObjectProperty<Button> value = new SimpleObjectProperty<>(null);

    @Parameter
    UIService uiService;

    private static final String ERROR_MSG = "The callback '%s' is undefined";

    @Parameter
    ModuleService service;

    public ButtonInputSkin() {
        super();
       
    }
    
    @Override
    public void init(Input input) {
        button = new javafx.scene.control.Button();
         button.setText(input.getLabel());
        button.setOnAction(this::onAction);
    }

    

    public void onAction(ActionEvent event) {
        button.textProperty().bind(getSkinnable().labelProperty());

        getSkinnable().getInput().callback();

    }

    @Override
    public Property<Button> valueProperty() {
        return value;
    }

   

    @Override
    public Node getNode() {
        
        return button;
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean canHandle(Class<?> clazz) {

        return clazz == Button.class;
    }

}
