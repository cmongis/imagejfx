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
import ijfx.core.project.command.Command;
import ijfx.core.project.command.CommandList;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.modifier.ModifierPlugin;
import java.util.ArrayList;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 *
 * @author cyril
 */
public abstract class DummyModifierBase extends Label implements ModifierEditor{

    private final String name;
    private final FontAwesomeIcon icon;
    
    private final FontAwesomeIconView viewIcon;
    SimpleObjectProperty<ModifierPlugin> editedModifier = new SimpleObjectProperty<>();
    
    public DummyModifierBase(String name,FontAwesomeIcon icon) {
        this.name = name;
        this.icon = icon;
        this.viewIcon = new FontAwesomeIconView(icon);
        setText("Coming soon :-)");
        
        
    }
    
    
    
    @Override
    public ModifierPlugin create() {
        return new DummyModifier();
    }

    @Override
    public boolean configure(ModifierPlugin modifier) {
        return true;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public Property<ModifierPlugin> editedModifierPluginProperty() {
        return editedModifier;
    }

    @Override
    public Node getIcon() {
        return viewIcon;
    }

    @Override
    public String phraseMe() {
        return name;
    }
    
    
    public class DummyModifier implements ModifierPlugin {

        @Override
        public boolean configure(String query) {
            return true;
        }

        @Override
        public Command getModifyingCommand(PlaneDB planeDB) {
            return new CommandList(new ArrayList<>());
        }

        @Override
        public boolean wasApplied(PlaneDB planeDB) {
            return true;
        }

        @Override
        public String phraseMe() {
            return "Should "+name.toLowerCase();
        }
    
    }
    
}
