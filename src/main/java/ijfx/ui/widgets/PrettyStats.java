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
package ijfx.ui.widgets;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class PrettyStats extends HBox{
        
    Label number = new Label();
    Label text = new Label();
    
    IntegerProperty valueProperty = new SimpleIntegerProperty();
    
    public PrettyStats() {
        super();
        getStyleClass().add("pretty-stats");
        number.getStyleClass().addAll("number","warning");
        text.getStyleClass().add("next-to-number");
        
        getChildren().addAll(number,text);
        
        number.textProperty().bind(Bindings.createStringBinding(this::getNumber, valueProperty));
       
    }
    
    public PrettyStats(String text) {
        this();
        textProperty().setValue(text);
    }
    
    
    public IntegerProperty valueProperty() {
        return valueProperty;
    }
    public StringProperty textProperty() {
        return text.textProperty();
    }
    
    public String getNumber() {
        return valueProperty.getValue().toString();
    }
}
