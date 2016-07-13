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
package ijfx.ui.plugin;

/**
 *
 * @author Tuan anh TRINH
 */
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

/**
 *
 */
public class WebColorField extends InputField {
    /**
     * The value of the WebColorField. If null, the value will be treated as "#000000" black, but
     * will still actually be null.
     */
    private ObjectProperty<Color> value = new SimpleObjectProperty<Color>(this, "value");
    public final Color getValue() { return value.get(); }
    public final void setValue(Color value) { this.value.set(value); }
    public final ObjectProperty<Color> valueProperty() { return value; }

    /**
     * Creates a new WebColorField. The style class is set to "webcolor-field".
     */
    public WebColorField() {
        getStyleClass().setAll("webcolor-field");
    }
}
