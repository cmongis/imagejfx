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
package ijfx.ui.input.widgets;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.widget.AbstractInputWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.TextWidget;
import org.scijava.widget.WidgetModel;

/**
 *
 * @author cyril
 */
@Plugin(type = InputWidget.class)
public class TextWidgetFX extends AbstractFXInputWidget<String> implements TextWidget<Node> {

    TextArea textArea;

    TextField textField;

    PasswordField passwordField;

    StringProperty textProperty = new SimpleStringProperty();

    ModelBinder<String> modelBinder;
    
    @Override
    public void set(WidgetModel model) {

        super.set(model);

        
       
        
        if (isStyle(AREA_STYLE)) {
            textArea = new TextArea();
            //textArea.textProperty().bindBidirectional(textProperty);
            bindProperty(textArea.textProperty());
            
            
            
        } else if (isStyle(PASSWORD_STYLE)) {
            passwordField = new PasswordField();
            bindProperty(passwordField.textProperty());
        } else {
            textField = new TextField();
            bindProperty(textField.textProperty());
        }
        
    }

   


    private String getWidgetStyle() {
        return get().getItem().getWidgetStyle();
    }

    private boolean isStyle(String style) {
        return getWidgetStyle().equals(style);
    }

    @Override
    public Node getComponent() {
        if (isStyle(AREA_STYLE)) {
            return textArea;
        } else if (isStyle(PASSWORD_STYLE)) {
            return passwordField;
        } else {
            return textField;
        }
    }

    @Override
    public Class<Node> getComponentType() {
       return Node.class;
    }
    
    @Override
    public boolean supports(WidgetModel model) {
        return super.supports(model) && model.isText();
    }

}
