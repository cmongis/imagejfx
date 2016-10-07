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
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.fxml.FXML;
import javafx.scene.Group;
import net.imagej.Dataset;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = ChromaticCorrectionUiPlugin.class, label = "Chromatic Correction")
public class ChromaticCorrectionUiPlugin extends AbstractCorrectionUiPlugin{

    @FXML
    Group channel1Group;
    
    @FXML
    Group channel2Group;
    
    ChannelNumberProperty channelNumberProperty;
    
    ChannelSelector sourceSelector = new ChannelSelector(null);
    
    ChannelSelector targetSelector = new ChannelSelector(null);
    
    private static final String FINAL_EXPLANATION = "Aligning *channel %d* with *channel %d* using %s.";
    
    public ChromaticCorrectionUiPlugin() {
        super("ChromaticCorrectionUiPlugin.fxml");
        
         sourceSelector.setAllowAllChannels(false);
         targetSelector.setAllowAllChannels(false);
        
      
        //sourceSelector.channelNumberProperty().bind(Bindings.createIntegerBinding(this::getDatasetChannelNumber, dependencies));
        IntegerBinding channelNumber = Bindings.createIntegerBinding(this::getDatasetChannelNumber, exampleDataset());
        
        sourceSelector.channelNumberProperty().bind(channelNumber);
        targetSelector.channelNumberProperty().bind(channelNumber);
        
        
        channel1Group.getChildren().add(sourceSelector);
        channel2Group.getChildren().add(targetSelector);
        
        bind(explanationProperty, this::getMessage, sourceSelector.selectedChannelProperty(),targetSelector.selectedChannelProperty());
        
        
        
    }

    @Override
    protected void onExampleDatasetChanged(Observable obs, Dataset oldValue, Dataset newValue) {
        System.out.println(newValue);
    }

    @Override
    public void init() {
        
          channelNumberProperty = new ChannelNumberProperty(datasetProperty);
    }
    
    protected String getMessage() {
        
        int channel1 = sourceSelector.getSelectedChannel();
        int channel2 = targetSelector.getSelectedChannel();
        
        
        
        if(channel1 == -1 && channel2 == -1) {
            return "!Please configure!";
        }
        else if(channel1 == -1 || channel2 == -1) {
            return "*Yes, one more to go...*";
        }
        
        else {
            return String.format(FINAL_EXPLANATION,channel1,channel2,"nothing");
        }
        
    }
    
    
}
