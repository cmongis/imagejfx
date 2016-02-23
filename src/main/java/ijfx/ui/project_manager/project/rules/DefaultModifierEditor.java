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
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import mongis.utils.FXUtilities;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;

/**
 *
 * @author cyril
 */
public class DefaultModifierEditor extends BorderPane implements ModifierSelector<Node> {

    @Parameter
    PluginService pluginService;

    Collection<ModifierEditor> modifierWidgetList = new ArrayList<>();

    Property<ModifierPlugin> editedModifier = new SimpleObjectProperty<>();

    Property<ModifierEditor> widget = new SimpleObjectProperty<>();

    ToggleGroup group = new ToggleGroup();

    @FXML
    FlowPane flowPane;

    @FXML
    BorderPane borderPane;

   Logger logger = ImageJFX.getLogger();
    
    public DefaultModifierEditor() {
        try {
            FXUtilities.injectFXML(this);
            flowPane.getStyleClass().add("flow-pane");
            
        } catch (IOException ex) {
            Logger.getLogger(DefaultModifierEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void afterContextInjection() {
        modifierWidgetList.addAll(pluginService.createInstancesOfType(ModifierEditor.class));

        createToggle(flowPane.getChildren());

        //group.selectedToggleProperty().addListener((obs,oldValue,newValue)->widget.setValue(converter.convert(newValue)));
        group.selectedToggleProperty().addListener(this::onToggleChanged);
        //widget.addListener((obs,oldVAlue,newValue)->group.selectToggle(group.getToggles().filtered(toggle->toggle.getUserData() == newValue).stream().findFirst().orElse(null)));
        //editedModifier.addListener(this::onModifierChanged);

    }

    public void createToggle(Collection<Node> nodes) {
        for (ModifierEditor widget : modifierWidgetList) {
            try {
            System.out.println("Adding widget " + widget.phraseMe());
            widget.getIcon().getStyleClass().add("big-icon");
            VBox vbox = new VBox();
            vbox.setAlignment(Pos.CENTER);
            Label label = new Label(widget.phraseMe());
            label.setTextAlignment(TextAlignment.CENTER);
            label.setWrapText(true);
            vbox.getChildren().addAll(widget.getIcon(),label);
            ToggleButton toggleButton = new ToggleButton(null, vbox);

            toggleButton.setUserData(widget);
            toggleButton.setContentDisplay(ContentDisplay.TOP);

            toggleButton.getStyleClass().add("square");
            group.getToggles().add(toggleButton);
            nodes.add(toggleButton);
            }
            catch(Exception e) {
                logger.log(Level.SEVERE, "Error when initializing ModifierEditor", e);
            }
        }
    }

    public void onModifierChanged(Observable obs, ModifierPlugin oldValue, ModifierPlugin newValue) {
         System.out.println("***It changed !");
        ModifierEditor modifierEditor = null;
        if (newValue != null) {
            modifierEditor = modifierWidgetList.stream().filter(editor -> editor.configure(newValue)).findFirst().orElse(null);
            //richMessageDisplayer.setMessage(modifierEditor.editedModifierPluginProperty().getValue().phraseMe());
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
        ModifierEditor widget = (ModifierEditor) newValue.getUserData();

        editedModifier.unbind();
        if (editedModifier.getValue() == null || !widget.configure(editedModifier.getValue())) {
            System.out.println("creating");
            editedModifier.setValue(widget.create());
            widget.configure(editedModifier.getValue());
            widget.editedModifierPluginProperty().setValue(editedModifier.getValue());
            
        }
        
        editedModifier.bind(widget.editedModifierPluginProperty());
        System.out.println(widget.editedModifierPluginProperty().getValue());
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

    public ModifierEditor getWidgetFromToggle(Toggle toggle) {
        return (ModifierEditor) toggle.getUserData();
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

}
