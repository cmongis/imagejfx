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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 */
public class IntegerField extends InputField {
    /**
     * The value of the IntegerField. If null, the value will be treated as "0", but
     * will still actually be null.
     */
    private IntegerProperty value = new SimpleIntegerProperty(this, "value");
    public final int getValue() { return value.get(); }
    public final void setValue(int value) { this.value.set(value); }
    public final IntegerProperty valueProperty() { return value; }

    /**
     * Creates a new IntegerField. The style class is set to "money-field".
     */
    public IntegerField() {
        getStyleClass().setAll("integer-field");
    }
}
