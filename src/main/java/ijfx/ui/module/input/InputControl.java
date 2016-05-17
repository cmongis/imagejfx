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
package ijfx.ui.module.input;


import ijfx.ui.module.InputEvent;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;

/**
 * Standard and Generic Control for value input. The control should be use for Module Input and WorkflowStep inputs
 * but has been made generic enough so it can be reused in other situation.
 * If the backbone relies mostly on JavaFX, the skins are however all dependant from the SciJava
 * 
 * The InputControl should be created from an @Input object and from an InputFactory that will
 * generate a Skin depending of the @Input class type.
 * 
 * 
 * @author Cyril MONGIS, 2015
 */
public class InputControl<T extends Object> extends Control {
    
    
    protected SimpleObjectProperty<T> chosenValue = new SimpleObjectProperty<>();
    
    protected ModuleItem<T> moduleItem;
    
    protected Module module;
    
    protected Class<T> type;
    
    protected SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);
    
    public static final String INPUT_CSS_CLASS = "input";
    
    
    protected StringProperty labelProperty = new SimpleStringProperty();
    
    
    private InputControl() {
        super();
            
    }
    
    Input<T> input;
    
    /**
     * 
     * @param factory InputSkinFacotry that should generate InputSkin depending on the input
     * @param input Class implementing the Input interface reprenting the input and containing the method for input setting
     */
    public InputControl(InputSkinFactory factory, Input input) {
        this();
        
        this.input = input;
        
        labelProperty.setValue(input.getLabel());
        
        InputSkin skin = factory.createSkin(input);
         skin.setSkinnable(this);
        setSkin(skin);
       
        
        chosenValueProperty().bindBidirectional(skin.valueProperty());
        chosenValueProperty().addListener(this::onValueChanged);
        
        skin.getNode().getStyleClass().add(INPUT_CSS_CLASS);
        chosenValueProperty().setValue((T)input.getDefaultValue());
    }
    
    @Override
    public Skin<InputControl> createDefaultSkin() {
        return null;
    }
    
    
    protected void onValueChanged(Observable object, T oldValue, T newValue) {
        
        System.out.println("The value has changed to "+newValue);
        //System.out.println("new input event : "+newValue.toString());
        if(input.getValue() != newValue) input.setValue(newValue);
        this.fireEvent(new InputEvent(input, newValue));
    }
    
    
    /**
     * 
     * @return returns a property that contains the value set by the control and the input
     */
    public SimpleObjectProperty<T> chosenValueProperty() {
        return chosenValue;
    }
    
    /**
     *  
     * @return the value set by control for the input
     */
    public T getValue() {
        return chosenValue.getValue();
    }
    
    /**
     * Set the control and input value
     * @param value 
     */
    public void setValue(T value) {
        chosenValue.setValue(value);
    }

    /**
     * Get the input default value
     * @return the input default value
     */
    T getDefaultValue() {
        return input.getDefaultValue();
        
    }
    
    /**
     * Return the label of the input control
     * @return 
     */
    public String getLabel() {
        return labelProperty.getValue();
    }
    
    /**
     * Set the control label
     * @param label 
     */
    public void setLabel(String label) {
        labelProperty.setValue(label);
    }
    
    /**
     * Returns a property representing the input label
     * @return 
     */
    public StringProperty labelProperty() {
        return labelProperty;
    }
    
    
    /**
     * Returns a boolean property indicating if the input has been correctly filled or not
     * @return 
     */
    public BooleanProperty validProperty() {
        return validProperty;
    }
    
    /**
     * Sets the input as valid and correctly filled
     * @param valid 
     */
    public void setValid(boolean valid) {
      
        validProperty().setValue(valid);
    }
    
    /**
     * @return true if the property has been correctly filled
     */
    public boolean isValid () {
        return validProperty().getValue();
    }

    /**
     * 
     * @return the input object handled by the control 
     */
    public Input getInput() {
        return input;
    }
    
    
    
}
