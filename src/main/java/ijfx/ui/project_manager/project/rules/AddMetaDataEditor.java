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
import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.modifier.MetaDataModifier;
import ijfx.core.project.modifier.ModifierPlugin;
import ijfx.ui.project_manager.projectdisplay.ProjectDisplayService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = ModifierEditorWidget.class)
public class AddMetaDataEditor extends HBox implements ModifierEditorWidget {

    TextField keyNameTextField = new TextField();

    TextField valueNameTextField = new TextField();

    MenuButton menuButton = new MenuButton(" ");

    @Parameter
    ProjectDisplayService projectDisplayService;

    @Parameter
    ProjectManagerService projectManagerService;

    ObjectProperty<ModifierPlugin> plugin = new SimpleObjectProperty<>();

    FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.FILE_TEXT);

    public AddMetaDataEditor() {
        TextFields.bindAutoCompletion(keyNameTextField, this::getKeyNameSuggestions);
        TextFields.bindAutoCompletion(valueNameTextField, this::getValueSuggestions);
        keyNameTextField.getStyleClass().add("half-button-left");
        menuButton.getStyleClass().add("half-button-right");

        FontAwesomeIconView view = new FontAwesomeIconView(FontAwesomeIcon.ARROW_LEFT);

        getChildren().addAll(keyNameTextField, menuButton, view, valueNameTextField);

    }

    @Override
    public boolean configure(ModifierPlugin modifier) {
        System.out.println(modifier);
        if (modifier == null) {
            return false;
        }
        if (MetaDataModifier.class.isAssignableFrom(modifier.getClass())) {

            MetaDataModifier metadataModifier = (MetaDataModifier) modifier;

            reconfigure();

            keyNameTextField.setText(metadataModifier.getKeyName());
            valueNameTextField.setText(metadataModifier.getValue());

            return true;
        } else {
            return false;
        }
    }

    public void reconfigure() {
        System.out.println("reconfiguring");
        ArrayList<String> differentKeys = new ArrayList<>(projectManagerService.getAllPossibleMetadataKeys(getCurrentProject()));

        Collections.sort(differentKeys);

        // Initializing the Menu Button that will contain all the possible metadata key as suggestion
        menuButton.getItems().clear();
        menuButton.setText(null);
        menuButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.SUPPORT));
        menuButton.getStyleClass().add("icon");
        menuButton.getItems().addAll(differentKeys.stream().map(keyName -> {
            MenuItem item
                    = new MenuItem(keyName);
            item.setOnAction(event -> keyNameTextField.setText(keyName));
            return item;
        }).collect(Collectors.toList()));

        // Initializing the textfield suggestion for the KeyValue
    }

    public Collection<String> getKeyNameSuggestions(AutoCompletionBinding.ISuggestionRequest request) {
        System.out.println("Getting them !");
        return projectManagerService.getAllPossibleMetadataKeys(getCurrentProject()) // we get all the possible keys
                .stream() // we stream it
                .filter(keyName -> keyName.toLowerCase().contains(request.getUserText().toLowerCase())) // filter the key names that contain the text entered by the user
                .collect(Collectors.toList()); // we collect the results in a list that we return
    }

    public Collection<String> getValueSuggestions(AutoCompletionBinding.ISuggestionRequest request) {
        return getCurrentProject()
                .getValues(keyNameTextField.getText())
                .stream()
                .filter(value -> value.toLowerCase().contains(request.getUserText().toLowerCase()))
                .collect(Collectors.toList());
    }

    public Project getCurrentProject() {
        return projectDisplayService.getActiveProjectDisplay().getProject();
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public Property<ModifierPlugin> editerModifierPluginProperty() {
        return plugin;
    }

    @Override
    public String phraseMe() {
        return "Add a metadata";
    }

    @Override
    public Node getIcon() {
        return icon;
    }

    @Override
    public ModifierPlugin create() {
        return new MetaDataModifier();
    }

}
