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
package ijfx.ui.explorer.view;

import ijfx.core.metadata.MetaData;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBox;

/**
 *
 * @author Tuan anh TRINH
 */
public class ComboBoxMetadata extends ComboBox<Field> {

    ObjectProperty<Field> metaDataProperty = new SimpleObjectProperty();

    public Property metaDataProperty() {
        return metaDataProperty;
    }

    public ComboBoxMetadata() {
        System.out.println("Initiliazing");
        this.getItems().addAll(getStaticStringFields());

        this.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> metaDataProperty.setValue(newValue));

    }

    public List<Field> getStaticStringFields() {
        Field[] declaredFields = MetaData.class.getDeclaredFields();
        List<Field> fields = new ArrayList<Field>();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                fields.add(field);
            }
        }
        return fields;
    }

}
