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

import ijfx.core.assets.DatasetAsset;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBox;
import net.imagej.Dataset;

/**
 *
 * @author cyril
 */
public class FlatfieldCorrectionUiPlugin extends AbstractCorrectionUiPlugin{

    
    Property<DatasetAsset> datasetAssetProperty = new SimpleObjectProperty();
    
    IntegerProperty channelProperty = new SimpleIntegerProperty();
    
    IntegerProperty totalChannel = new SimpleIntegerProperty();
    
    ComboBox<Integer> channelCombox;
    
     @Override
    public void init() {
    
    }
    
    
    public FlatfieldCorrectionUiPlugin() {
        super("/ijfx/ui/correction/FlatfieldCorrectionUiPlugin.fxml");
        
        
        totalChannel.addListener(this::onTotalChannelChanged);
        
    }
    
    @Override
    protected void onExampleDatasetChanged(Observable obs, Dataset oldValue, Dataset newValue) {
        
    }

    protected void onTotalChannelChanged(Observable obs, Number oldValue, Number newValue) {
        
        int n = newValue.intValue();
        
        if( n == 0) {
            
        }
        
    }
   
    
}
