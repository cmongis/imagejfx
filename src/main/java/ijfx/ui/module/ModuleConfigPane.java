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
package ijfx.ui.module;

import ijfx.ui.module.input.InputControl;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.module.input.Input;
import ijfx.service.workflow.WorkflowService;
import ijfx.service.workflow.WorkflowStep;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import net.imagej.widget.HistogramBundle;
import org.scijava.ItemVisibility;
import org.scijava.command.CommandService;
import org.scijava.module.Module;
import org.scijava.plugin.Parameter;
import mongis.utils.FXUtilities;
import org.scijava.module.ModuleService;

/**
 * This pane generate a input fields for Module inputs.
 *
 * @author Cyril MONGIS, 2015
 */
public class ModuleConfigPane extends BorderPane {

    @FXML
    GridPane gridPane;

    @FXML
    Button closeButton;

    @FXML
    Label titleLabel;

    @FXML
    TextField editableLabel;

    @FXML
    VBox messageBox;
    @FXML
    FlowPane buttonBox;

    @FXML
    VBox graphicsBox;

    // number of fields added to the pane
    protected int fieldCount = -1;

    // not sure if it's still used...
    protected final int fieldColumn = 1;

    // same...
    protected int labelColumn = 0;

    // Map containing the @InputControl
    Map<String, InputControl> inputControlMap = new HashMap<>();

    // Module currently configured
    Module module;

    // Workflow step currently configured
    WorkflowStep step;

    @Parameter
    CommandService commandService;

    @Parameter
    DefaultInputSkinService inputSkinPluginService;

    @Parameter
    ModuleService moduleService;

    // Property indicating if the all the fields are filled correctly.
    BooleanProperty validProperty = new SimpleBooleanProperty(true);

    public ModuleConfigPane() {
        super();

        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);;
        }

        // add the necessary listener.
        // this one check the editableLabel (WorkflowSteps only)
        editableLabel.textProperty().addListener(this::onEditableLabelChange);
    }

    // check if all fields are valid and change the valid property according to.
    public void checkField(ObservableValue<? extends Boolean> listener, Boolean oldValue, Boolean newValue) {

        for (InputControl input : inputControlMap.values()) {

            if (input.isValid() == false) {
                validProperty.setValue(false);

                return;
            }
        }
        validProperty.setValue(true);
    }

    //return the validProperty
    public BooleanProperty validProperty() {
        return validProperty;
    }

    public GridPane getGridPane() {
        return gridPane;
    }

    // Adds a InputControl
    public InputControl addField(String id, InputControl input) {

        fieldCount++;
        inputControlMap.put(id, input);
        Label label = new Label();
        label.getStyleClass().add("input-label");
        label.textProperty().bind(input.labelProperty());
        gridPane.add(label, labelColumn, fieldCount);
        gridPane.add(input, fieldColumn, fieldCount);
        return input;
    }

    // Returns an input control with the id;
    public InputControl getField(String id) {
        return inputControlMap.get(id);
    }

    // return a HashMap with all the inputs and the values
    // filled
    public HashMap<String, Object> getHashMap() {
        HashMap<String, Object> resultMap = new HashMap<>();
        inputControlMap.forEach((key, input) -> {

            resultMap.put(key, input.chosenValueProperty().getValue());

        });
        return resultMap;
    }

    // returns the number of input handled
    public int inputCount() {
        return inputControlMap.size();
    }

    // set up the pane according to a workflow step
    public void configure(WorkflowStep step) {

        cleanUp();

        setStep(step);

        editableLabel.setText(step.getId());

        configure(step.getModule());

        step.getParameters().forEach((key, value) -> {
            InputControl input = getField(key);
            if (input != null) {
                System.out.println(value);
                input.setValue(value);
            }
        });

        editableLabel.setVisible(true);

    }

    // Cleans up the pane by removing all the @InputControl.
    public void cleanUp() {

        //reseting the field count
        fieldCount = -1;

        // removing the handlers associated to this map
        inputControlMap.forEach((key, input) -> {
            input.removeEventHandler(InputEvent.FIELD_CHANGED, this::handleInputValueChanged);
        });

        if (step == null) {
            editableLabel.setVisible(false);
        }

        // unbinding the labels
        titleLabel.textProperty().unbind();
        editableLabel.textProperty().unbind();

        gridPane.getChildren().clear();

    }

    public void configure(Module module) {

        // cleaning up the table
        cleanUp();

        // setting the module
        setModule(module);
        
        // puting the module name as title
        getTitleLabel().setText(WorkflowService.getModuleLabel(module));

        // for each input, a field is generated
        // The InputControl object can deal with different type of value,
        // each value associated to a special skin
        module.getInfo().inputs().forEach(inputInfo -> {

            Input input = new ModuleInputWrapper(module, inputInfo);
            
            // generating the input
            InputControl inputControl;
            
            System.out.println(inputInfo.getName() + " : " + inputInfo.getIOType());
            
            //if(inputInfo.getIOType() == ItemIO.INPUT || inputInfo.getIOType() == ItemIO.BOTH) return;
            if (module.isResolved(inputInfo.getName()) && step == null) {
                return;
            }
            
            if(step != null && (inputInfo.getLabel() == null || inputInfo.getLabel().equals(""))) return;
            
            //if( inputInfo.getType() == Dataset.class && module.isResolved(inputInfo.getName()))
            // return;
            
            {
                if (inputSkinPluginService.canCreateSkinFor(input)) {

                    // the control is created
                    inputControl = new InputControl(inputSkinPluginService, input);
                    
                    // setting the value to the control
                    inputControl.setValue(input.getValue());

                    // if it's a message, the control is added to the MessageBox
                    if (inputInfo.getVisibility() == ItemVisibility.MESSAGE) {
                        //inputControl.setSkin(new StringMessageInput());
                        //inputControl.setDefaultValue(defaultInput);
                        inputControl.getStyleClass().add("module-config-pane-message");
                        messageBox.getChildren().add(inputControl);
                    } // if it's an HistogramBundle, it's added to the GraphicsBox
                    else if (inputInfo.getType() == HistogramBundle.class) {
                        graphicsBox.getChildren().add(inputControl);
                    } // if it's a Button, it's added to the Button Box
                    else if (inputInfo.getType() == org.scijava.widget.Button.class) {
                        buttonBox.getChildren().add(inputControl);
                    } // other wise, it's added to the gridpane containing the other fields
                    else {
                        addField(inputInfo.getName(), inputControl);
                    }

                    // when a input is changed, the panel will be notified
                    inputControl.addEventHandler(InputEvent.ALL, this::handleInputValueChanged);

                }
            }

        });
    }

    // Fires a InputEvent (used by other components to act when a parameter is changed)
    private void handleInputValueChanged(InputEvent event) {
        fireEvent(event);

        if (step != null) {
            step.getParameters().put(event.getInput().getName(), event.getInput().getValue());
        }

    }

    // Returns the editable label
    public TextField getEditableLabel() {
        return editableLabel;
    }

    // Returns the close Button
    public Button getCloseButton() {
        return closeButton;
    }

    // Returns the TitleLabel
    public Label getTitleLabel() {
        return titleLabel;
    }

    // return the Module currently modified
    public Module getModule() {
        if (step == null) {
            return module;
        } else {
            return getStep().getModule();
        }
    }

    // Sets the currently configured @Module
    public void setModule(Module module) {
        this.module = module;
    }

    // Returns the currently configured @WorkflowStep
    public WorkflowStep getStep() {
        return step;
    }

    // Sets the current @WorkflowStep
    public void setStep(WorkflowStep step) {
        this.step = step;

    }

    // When the editable label is changed, the name of the step is updated
    public void onEditableLabelChange(ObservableValue<? extends String> obs, String oldValue, String newValue) {
        if (step != null && newValue != null) {
            step.setId(newValue);
        }
    }

    // execute the callback of the method
    public static void executeCallback(Module module, String callback) {

        Object object = module.getDelegateObject();

        try {

            Method m = object.getClass().getMethod(callback);

            m.invoke(object);
            ImageJFX.getLogger().info("call back executed " + callback);
        } catch (NoSuchMethodException ex) {
            ImageJFX.getLogger().log(Level.WARNING, null, ex);;

        } catch (SecurityException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);;
        } catch (IllegalAccessException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);;
        } catch (IllegalArgumentException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);;
        } catch (InvocationTargetException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);;
        }
    }

}
