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
package ijfx.ui.module.skin;

import ijfx.ui.module.InputSkinPlugin;
import ijfx.ui.module.input.Input;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javax.swing.text.NumberFormatter;
import mongis.utils.StringUtils;
import org.scijava.plugin.Plugin;
import org.scijava.util.NumberUtils;
import org.scijava.widget.NumberWidget;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = InputSkinPlugin.class)
public class DoubleInputSkin extends AbstractInputSkinPlugin<Double> {

    private TextField field;

    EventHandler<KeyEvent> changeListener = (event) -> {
        onChange(field.getText());
    };

    private Slider slider;

    private Input<Double> input;
    private Label sliderLabel = new Label();
    private HBox hbox = new HBox();
    private boolean isSlider = false;

    public static String CLASS_INVALID = "danger";

    private DoubleProperty value = new SimpleDoubleProperty();

    public DoubleInputSkin() {
        super();

    }

    @Override
    public Node getNode() {
        if (isSlider) {
            return hbox;
        } else {
            return field;
        }
    }

    @Override
    public void dispose() {
    }

    public void onChange(String newValue) {
        try {
            Double value = Double.parseDouble(newValue);
            valueProperty().setValue(value);

            validProperty().setValue(true);

            field.getStyleClass().remove(CLASS_INVALID);

        } catch (Exception e) {
            if (field.getStyleClass().contains(CLASS_INVALID) == false) {

                validProperty().setValue(false);
                field.getStyleClass().add(CLASS_INVALID);

            }
        }
    }

    @Override
    public Property<Double> valueProperty() {
        return value.asObject();
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return clazz == double.class || clazz == Double.class;
    }

    @Override
    public void init(Input<Double> input) {

        this.input = input;

        value.setValue(input.getValue());

        if (input.getWidgetType() != null && ( input.getWidgetType().equals(NumberWidget.SLIDER_STYLE) || input.getWidgetType().equals(NumberWidget.SCROLL_BAR_STYLE))) {
            isSlider = true;

        } else {
            isSlider = false;
        }

        if (isSlider) {
            slider = new Slider();
            //slider.setShowTickLabels(true);
            slider.setMin(input.getMinimumValue()+1);
            slider.setMax(input.getMaximumValue());

            double range = input.getMaximumValue() - input.getMinimumValue();
            
            if(range <= 10) {
                slider.setMajorTickUnit(0.1);
            }
            else {
                slider.setMajorTickUnit(1.0);
            }
            
            value.bindBidirectional(slider.valueProperty());
            
            hbox.getChildren().addAll(sliderLabel,slider);
            sliderLabel.setPrefWidth(60);
            sliderLabel.setMaxWidth(60);
            sliderLabel.getStyleClass().add("warning");
            hbox.setSpacing(10.0);
            
            sliderLabel.textProperty().bind(Bindings.createStringBinding(()->StringUtils.numberToString(value.getValue(),3), value));
            

        } else {
            field = new TextField();

            field.setText(input.getValue().toString());
            field.addEventHandler(KeyEvent.KEY_RELEASED, changeListener);
        }
    }

}
