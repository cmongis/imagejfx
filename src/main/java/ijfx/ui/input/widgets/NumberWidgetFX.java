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
package ijfx.ui.input.widgets;

import ijfx.ui.utils.ConvertedProperty;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.scijava.plugin.Plugin;
import org.scijava.widget.InputWidget;
import org.scijava.widget.NumberWidget;
import org.scijava.widget.WidgetModel;

/**
 *
 * @author cyril
 */
@Plugin(type = InputWidget.class)
public class NumberWidgetFX extends AbstractFXInputWidget<Number> implements NumberWidget<Node> {

    private Node component;

    public static String CLASS_INVALID = "danger";

    ConvertedProperty<Number, Double> converter = new ConvertedProperty<Number, Double>()
            .forward(Number::doubleValue)
            .backward(Double::new);

    Property<Number> property = new SimpleDoubleProperty();

    public NumberWidgetFX() {

    }

    @Override
    public void set(WidgetModel model) {
        super.set(model);

       Number min = model.getSoftMin();
       Number max = model.getSoftMax();
       Number stepSize = model.getStepSize();
       
        
        if (model.isStyle((SCROLL_BAR_STYLE))) {
            Slider slider = new Slider();
            slider.setValue(getValue().doubleValue());
            slider.setMin(min.doubleValue());
            slider.setMax(max.doubleValue());
            slider.setBlockIncrement(stepSize.doubleValue());
            bindProperty(slider.valueProperty());
            component = slider;

        } else {

            SpinnerValueFactory factory;
            if (model.isType(Double.class) || model.isType(double.class)) {
                factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(min.doubleValue(), max.doubleValue(), min.doubleValue(), stepSize.doubleValue());
            }
            else if(model.isType(long.class) || model.isType(Long.class)) {
                factory = new LongSpinnerValueFactory(min.longValue(), max.longValue(), min.longValue(), stepSize.longValue());
            }
            
            else {
                factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min.intValue(), max.intValue(), min.intValue(), stepSize.intValue());
            }

            //factory = new NumberSpinnerValueFactory(min, max, 0, stepSize, (Class<? extends Number>) model.getItem().getType());
            Spinner spinner = new Spinner(factory);
            spinner.setEditable(true);

            bindProperty(factory.valueProperty());

            //bindProperty(converter.frontProperty());
            component = spinner;
        }

    }

    @Override
    public Node getComponent() {
        return component;
    }

    @Override
    public Class<Node> getComponentType() {
        return Node.class;
    }

    @Override
    public boolean supports(WidgetModel model) {
        return super.supports(model) && model.isNumber();
    }

    private class LongSpinnerValueFactory extends SpinnerValueFactory<Long> {

        final long min, max, initial, stepSize;

        public LongSpinnerValueFactory(long min, long max, long initial, long stepSize) {
            this.min = min;
            this.max = max;
            this.initial = initial;
            this.stepSize = stepSize;
        }

        public long get() {
            return getValue();
        }

        @Override
        public void decrement(int steps) {
            long value = get() - (stepSize * steps);
            setValue(Math.max(min, value));
        }

        @Override
        public void increment(int steps) {
            long value = get() + (stepSize * steps);
            setValue(Math.min(max, value));
        }
    }

}
