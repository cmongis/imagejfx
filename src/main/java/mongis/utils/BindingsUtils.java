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
package mongis.utils;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.scene.Node;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class BindingsUtils {
    
    
    public static void bindNodeToClass(Node node, ObservableValue<Boolean> property,String styleClass) {
        
        property.addListener((obs,oldValue,newValue)-> {
            if(newValue && node.getStyleClass().contains(styleClass) == false) {
                node.getStyleClass().add(styleClass);
            }
            else if(!newValue) {
                node.getStyleClass().remove(styleClass);
            }
        });
        
        if(property.getValue()) node.getStyleClass().add(styleClass);
        
    }
    
    public static void bindNodeToPseudoClass(PseudoClass pseudoClass, Node node,ReadOnlyProperty<Boolean> booleanProperty) {
        
        node.pseudoClassStateChanged(pseudoClass, booleanProperty.getValue());
        
        booleanProperty.addListener((obs,oldValue,newValue)->{
            node.pseudoClassStateChanged(pseudoClass, newValue);
        });
    }
    
    public static void bindNodeToPseudoClass(PseudoClass pseudoClass, Node node, BooleanBinding booleanBinding) {
        node.pseudoClassStateChanged(pseudoClass, booleanBinding.getValue());
        
        booleanBinding.addListener((obs,oldValue,newValue)->{
            node.pseudoClassStateChanged(pseudoClass, newValue);
        });
        
    }
}
