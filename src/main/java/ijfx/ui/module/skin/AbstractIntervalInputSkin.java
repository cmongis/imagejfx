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
package ijfx.ui.module.skin;

import ijfx.plugins.DefaultInterval;
import ijfx.plugins.LongInterval;
import ijfx.ui.module.InputSkinPlugin;
import ijfx.ui.module.input.Input;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.controlsfx.control.RangeSlider;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */

public abstract class AbstractIntervalInputSkin<T extends LongInterval> extends AbstractInputSkinPlugin<T> {

   private T interval;

    private ObjectProperty<LongInterval> value = new SimpleObjectProperty<>();

 
    private RangeSlider rangeSlider = new RangeSlider();

   private  Label lowValueLabel = new Label();
    private Label highValueLabel = new Label();
    
    private HBox hbox = new HBox(lowValueLabel,rangeSlider,highValueLabel);
    
    public AbstractIntervalInputSkin() {
        hbox.setSpacing(10);
        lowValueLabel.textProperty().bind(rangeSlider.lowValueProperty().asString("%.0f"));
        highValueLabel.textProperty().bind(rangeSlider.highValueProperty().asString("%.0f"));
        
    }
    
    @Override
    public Property valueProperty() {
        return value;
    }

    @Override
    public Node getNode() {
        return hbox;
    }

    @Override
    public void dispose() {
    }

   

    protected abstract T createInitialInterval();
    
    @Override
    public void init(Input<T> input) {

        
        
        
        
        DefaultInterval defInterval = new DefaultInterval(0, 100);

        defInterval.minProperty().bind(rangeSlider.minProperty());
        defInterval.maxProperty().bind(rangeSlider.maxProperty());
        defInterval.lowValueProperty().bind(rangeSlider.lowValueProperty());
        defInterval.highValueProperty().bind(rangeSlider.highValueProperty());
        interval = (T) defInterval;

        
        T initialInterval;
        long min, max, high, low;
        // if the interval was predefined (from the step for instance)
        // we copy it to the editable interval
        if (input.getValue() != null) {

            initialInterval = input.getValue();
            
        }
        // otherwise, the extending clas should proving a way to create
        // an interval dependin on the situation
        else {
            initialInterval = createInitialInterval();
        }
            
            min = initialInterval.getMinValue();
            max = initialInterval.getMaxValue();
            high = initialInterval.getHighValue();
            low = initialInterval.getLowValue();

       
        
        rangeSlider.setMin(min);
        rangeSlider.setMax(max);
        rangeSlider.setLowValue(low);
        rangeSlider.setHighValue(high);
        
        input.setValue(interval);
        
        
        
        
    }

}
