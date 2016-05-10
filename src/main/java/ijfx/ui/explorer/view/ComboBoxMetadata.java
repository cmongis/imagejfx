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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBox;

/**
 *
 * @author Tuan anh TRINH
 */
public class ComboBoxMetadata extends ComboBox<String> {

    ObjectProperty<String> metaDataProperty = new SimpleObjectProperty();

    public ObjectProperty<String> metaDataProperty() {
        return metaDataProperty;
    }

    public ComboBoxMetadata() {
        super();
        System.out.println("Initiliazing");
        this.getItems().addAll(getStaticStringFields());

        this.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            metaDataProperty.set(newValue);
        });
        this.setValue(this.getItems().get(0));

    }

    public List<String> getStaticStringFields() {
        Field[] declaredFields = MetaData.class.getDeclaredFields();
        List<String> fields = new ArrayList<String>();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                try {
                    fields.add((String) field.get(MetaData.class));
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(ComboBoxMetadata.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(ComboBoxMetadata.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return fields;
    }

    public String getMetaDataPropertyValue() {
        return metaDataProperty.getValue();
    }

    public void setMetaDataPropertyValue(String field) {
        metaDataProperty.setValue(field);
    }

}
