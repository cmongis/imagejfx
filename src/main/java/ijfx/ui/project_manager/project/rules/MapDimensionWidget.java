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
package ijfx.ui.project_manager.project.rules;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.project.modifier.MapDimensionsModifier;
import ijfx.core.project.modifier.ModifierPlugin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type=ModifierEditor.class)
public class MapDimensionWidget implements ModifierEditor{


    
    ObjectProperty<ModifierPlugin> editedModifierProperty = new SimpleObjectProperty<>(this, "editedModifier");
    FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.ARROWS_ALT);
    public static final String PHRASE = "Stack to hyperstack";
    @Override
    public ModifierPlugin create() {
        return new MapDimensionsModifier();
    }

    @Override
    public boolean configure(ModifierPlugin modifier) {
        if(modifier instanceof MapDimensionsModifier) {
            setEditedModifier((MapDimensionsModifier)modifier);             
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public Node getNode() {
        return icon;
    }

    @Override
    public Property<ModifierPlugin> editedModifierPluginProperty() {
        return editedModifierProperty;
    }

    @Override
    public Node getIcon() {
        return icon;
        
    }

    @Override
    public String phraseMe() {
        return PHRASE;
    }

    public void setEditedModifier(MapDimensionsModifier editedModifier) {
        
        editedModifierProperty.setValue(editedModifier);
    }

    public MapDimensionsModifier getEditedModifier() {
        return (MapDimensionsModifier)editedModifierProperty.getValue();
    }
    
    
    
}
