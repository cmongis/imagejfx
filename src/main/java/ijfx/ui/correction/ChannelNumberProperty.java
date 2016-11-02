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
package ijfx.ui.correction;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyIntegerPropertyBase;
import javafx.beans.property.ReadOnlyProperty;
import net.imagej.Dataset;
import net.imagej.axis.Axes;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class ChannelNumberProperty extends ReadOnlyIntegerPropertyBase {

    
    int channelNumber = 0;
    public ChannelNumberProperty(ReadOnlyProperty<Dataset> datasetProperty) {

        super();
        
        datasetProperty.addListener(this::onDatasetChanged);

    }

    protected void onDatasetChanged(Observable obs, Dataset oldValue, Dataset newValue) {
        if (newValue == null) {
            channelNumber = 0;
        } else {
            channelNumber = (int)newValue.dimension(Axes.CHANNEL);
        }
        fireValueChangedEvent();
    }

   
    
    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return "CannelNumberProperty";
    }

    @Override
    public int get() {
        return channelNumber;
    }

}
