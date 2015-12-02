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
package ijfx.ui.module.input;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.scene.control.Skin;

/**
 * Skin for an input. The type of input should 
 * 
 * @author Cyril MONGIS, 2015
 */
public interface InputSkin<T> extends Skin<InputControl<T>> {

    /**
     * 
     * @return the InputControl handled by the Skin. Can be used to get the Input itself 
     */
    InputControl<T> getSkinnable();

    /**
     *  Sets the InputControl
     * @param skinnable InputControl
     */
    void setSkinnable(InputControl<T> skinnable);
    /**
     *  Returns the property indicating if the input has been correctly filled
     * @return 
     */
    BooleanProperty validProperty();

    
    /**
     * Property containing the value filled by the user
     * @return 
     */
    Property<T> valueProperty();
    
}
