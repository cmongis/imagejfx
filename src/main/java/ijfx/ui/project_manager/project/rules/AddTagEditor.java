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
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.modifier.AddTagModifier;
import ijfx.core.project.modifier.ModifierPlugin;
import ijfx.ui.project_manager.project.TagCompletionCallback;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.controlsfx.control.textfield.TextFields;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = ModifierEditorWidget.class)
public class AddTagEditor extends VBox implements ModifierEditorWidget {

    ObjectProperty<ModifierPlugin> editedModifierProperty = new SimpleObjectProperty<>();

    FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.TAGS);

    TextField textField = new TextField();

    @Parameter
    ProjectManagerService projectManagerService;

    TagCompletionCallback completionCallback = null;

    @Override
    public ModifierPlugin create() {
        return new AddTagModifier();
    }

    public void initialize() {
        System.out.println("I can't believe it");
        if (completionCallback == null) {
            completionCallback = new TagCompletionCallback(projectManagerService);

            // binding to a callback that will return the possible tags of the current project
            TextFields.bindAutoCompletion(textField, completionCallback);
        }
    }

    @Override
    public boolean configure(ModifierPlugin modifier) {

        initialize();
       
        if (AddTagModifier.class.isAssignableFrom(modifier.getClass())) {

            return true;
        } else {
            return false;
        }
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public Property<ModifierPlugin> editerModifierPluginProperty() {
        return editedModifierProperty;
    }

    @Override
    public Node getIcon() {
        return icon;
    }

    @Override
    public String phraseMe() {
        return "Add tags";
    }

}
