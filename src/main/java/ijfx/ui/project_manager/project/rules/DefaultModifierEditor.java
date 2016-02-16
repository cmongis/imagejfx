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

import ijfx.core.project.modifier.ModifierPlugin;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.main.Localization;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import mongis.utils.FXUtilities;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

/**
 *
 * @author cyril
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "modifier", localization = Localization.BOTTOM_CENTER, context = "debug")
public class DefaultModifierEditor extends BorderPane implements ModifierEditor, UiPlugin {

    @Parameter
    PluginService pluginService;

    Collection<ModifierEditorWidget> modifierWidgetList = new ArrayList<>();

    Property<ModifierPlugin> editedModifier = new SimpleObjectProperty<>();

    Property<ModifierEditorWidget> widget = new SimpleObjectProperty<>();

    ToggleGroup group = new ToggleGroup();

    @FXML
    FlowPane flowPane;

    @FXML
    BorderPane borderPane;

    public DefaultModifierEditor() {
        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            Logger.getLogger(DefaultModifierEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void afterContextInjection() {
        modifierWidgetList.addAll(pluginService.createInstancesOfType(ModifierEditorWidget.class));

        createToggle(flowPane.getChildren());

        //group.selectedToggleProperty().addListener((obs,oldValue,newValue)->widget.setValue(converter.convert(newValue)));
        group.selectedToggleProperty().addListener(this::onToggleChanged);
        //widget.addListener((obs,oldVAlue,newValue)->group.selectToggle(group.getToggles().filtered(toggle->toggle.getUserData() == newValue).stream().findFirst().orElse(null)));
        //editedModifier.addListener(this::onModifierChanged);

    }

    public void createToggle(Collection<Node> nodes) {
        for (ModifierEditorWidget widget : modifierWidgetList) {
            System.out.println("Adding widget " + widget.phraseMe());
            widget.getIcon().getStyleClass().add("big-icon");
            ToggleButton toggleButton = new ToggleButton(widget.phraseMe(), widget.getIcon());

            toggleButton.setUserData(widget);
            toggleButton.setContentDisplay(ContentDisplay.TOP);

            toggleButton.getStyleClass().add("square");
            group.getToggles().add(toggleButton);
            nodes.add(toggleButton);
        }
    }

    public void onModifierChanged(Observable obs, ModifierPlugin oldValue, ModifierPlugin newValue) {

        ModifierEditorWidget modifierEditor = null;
        if (newValue != null) {
            modifierEditor = modifierWidgetList.stream().filter(editor -> editor.configure(newValue)).findFirst().orElse(null);
        }
        if (modifierEditor == null) {
            System.out.println("No Editor for this type of plugin " + newValue.phraseMe());
            return;
        }

        widget.setValue(modifierEditor);
        modifierEditor.configure(newValue);
    }

    public void onToggleChanged(Observable obs, Toggle oldValue, Toggle newValue) {
        if (newValue == null) {

            borderPane.setCenter(null);
            editedModifier.unbind();
            editedModifier.setValue(null);

            return;
        };
        ModifierEditorWidget widget = (ModifierEditorWidget) newValue.getUserData();

        editedModifier.unbind();
        if (editedModifier.getValue() == null || !widget.configure(editedModifier.getValue())) {
            System.out.println("creating");
            editedModifier.setValue(widget.create());
            widget.configure(editedModifier.getValue());
        }

        editedModifier.bind(widget.editerModifierPluginProperty());
        borderPane.setCenter(widget.getNode());

    }

    @Override
    public void setEditedModifier(ModifierPlugin modifier) {
        if (modifier != null) {
            Toggle handler = group
                    .getToggles()
                    .stream()
                    .filter(toggle -> getWidgetFromToggle(toggle).configure(modifier))
                    .findFirst().orElse(null);

            if (handler != null) {
                group.selectToggle(handler);
            }
        }
    }

    public ModifierEditorWidget getWidgetFromToggle(Toggle toggle) {
        return (ModifierEditorWidget) toggle.getUserData();
    }

    @Override
    public ModifierPlugin getEditedModifier() {
        return editedModifier.getValue();
    }

    @Override
    public Property<ModifierPlugin> editedModifierProperty() {
        return editedModifier;
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {
        afterContextInjection();
        return this;
    }

}
