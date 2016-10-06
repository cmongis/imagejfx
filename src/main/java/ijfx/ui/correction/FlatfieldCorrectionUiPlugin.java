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
import ijfx.service.workflow.Workflow;
import java.io.File;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import mongis.utils.FileButtonBinding;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = CorrectionUiPlugin.class, label = "Flatfield Correction")
public class FlatfieldCorrectionUiPlugin extends AbstractCorrectionUiPlugin {

    private Property<DatasetAsset> datasetAssetProperty = new SimpleObjectProperty();

    private ChannelSelector channelSelector = new ChannelSelector("Select channel");

    Button selectionButton = new Button("Select flatfield image");
    
    Property<File> flatfieldImage;
    
    FileButtonBinding binding = new FileButtonBinding(selectionButton);
    
    private static String EXPLANATION_FORMAT = "Corrects channel *%d* with *%s*";
    private static String ALL_CHANNEL = "Correct *ALL* channels with *%s*";
    @Override
    public void init() {
        
    }

    public FlatfieldCorrectionUiPlugin() {
        super(null);
        setLeft(channelSelector);
        setRight(selectionButton);
        
        
        binding.setButtonDefaultText("Choose flatfield image...");
        binding.setOpenFile(true);
        flatfieldImage = binding.fileProperty();
        
        bind(datasetAssetProperty,this::createDatasetAsset, flatfieldImage);
        
        bind(explanationProperty,this::generateExplanation,flatfieldImage,channelSelector.selectedChannelProperty());
       
        bind(workflowProperty,this::generateWorkflow,datasetAssetProperty,channelSelector.selectedChannelProperty());
        
    }

    protected DatasetAsset createDatasetAsset() {
        return new DatasetAsset(flatfieldImage.getValue());
    }
    
    protected Workflow generateWorkflow() {
        
        
        return null;
    }
    
    protected String generateExplanation() {
        
        int selectedChannel = channelSelector.selectedChannelProperty().getValue();
        if(flatfieldImage.getValue() == null) {
            return "*Please select the channel to correct and a flatfield*";
        }
        else {
            
            if(selectedChannel == -1) {
                return String.format(ALL_CHANNEL,flatfieldImage.getValue());
            }
            
            return String.format(EXPLANATION_FORMAT,selectedChannel,flatfieldImage.getValue().getName());
        }
    }
    
    @Override
    protected void onExampleDatasetChanged(Observable obs, Dataset oldValue, Dataset newValue) {

        Long channelNumber;

        if (newValue == null) {
            channelNumber = new Long(0);
        } else {

            channelNumber = newValue.dimension(Axes.CHANNEL);
        }

        channelSelector.channelNumberProperty().setValue(channelNumber);
    }

    protected void onTotalChannelChanged(Observable obs, Number oldValue, Number newValue) {

        int n = newValue.intValue();

        if (n == 0) {

        }

    }

}
