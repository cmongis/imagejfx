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

import ijfx.plugins.bunwarpJ.BUnwarpJConfigurator;
import ijfx.plugins.bunwarpJ.ElasticCorrection;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowBuilder;
import ijfx.ui.module.InputSkinPluginService;
import java.io.File;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import mongis.utils.FileButtonBinding;
import net.imagej.Dataset;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = ChromaticCorrectionUiPlugin.class, label = "Chromatic Correction")
public class ChromaticCorrectionUiPlugin extends AbstractCorrectionUiPlugin {

    @FXML
    Group channel1Group;

    @FXML
    Group channel2Group;

    @FXML
    Button landmarkFileButton;
    
    @FXML
    ScrollPane advancedParameterScrollPane;

    @Parameter
    InputSkinPluginService skinService;
    @Parameter
    Context context;

    ChannelNumberProperty channelNumberProperty;

    //ChannelSelector sourceSelector = new ChannelSelector(null);

    ChannelSelector targetSelector = new ChannelSelector(null);

    private static final String FINAL_EXPLANATION = "Correcting *channel %d* using %s.";

    BUnwarpJConfigurator configurator = new BUnwarpJConfigurator();

    Property<File> landmarkFileProperty = new SimpleObjectProperty();

    FileButtonBinding buttonBinding;
    
    public ChromaticCorrectionUiPlugin() {
        super("ChromaticCorrectionUiPlugin.fxml");

       // sourceSelector.setAllowAllChannels(false);
        targetSelector.setAllowAllChannels(false);
        
        buttonBinding = new FileButtonBinding(landmarkFileButton)
                .setButtonDefaultText("Select landmark file");
        buttonBinding.setOpenFile(true);
                
        
        configurator.getStyleClass().add("with-padding");
        //sourceSelector.channelNumberProperty().bind(Bindings.createIntegerBinding(this::getDatasetChannelNumber, dependencies));
        IntegerBinding channelNumber = Bindings.createIntegerBinding(this::getDatasetChannelNumber, exampleDataset());

      //  sourceSelector.channelNumberProperty().bind(channelNumber);
        targetSelector.channelNumberProperty().bind(channelNumber);

       // channel1Group.getChildren().add(sourceSelector);
        channel2Group.getChildren().add(targetSelector);
        
        landmarkFileProperty.bind(buttonBinding.fileProperty());
        
        
        bindP(explanationProperty, this::getMessage,targetSelector.selectedChannelProperty(),landmarkFileProperty);

        advancedParameterScrollPane.setContent(configurator);

        bindP(workflowProperty, this::generateWorkflow, landmarkFileProperty, targetSelector.selectedChannelProperty());

    }

    @Override
    protected void onExampleDatasetChanged(Observable obs, Dataset oldValue, Dataset newValue) {
        System.out.println(newValue);
    }

    @Override
    public void init() {

        channelNumberProperty = new ChannelNumberProperty(datasetProperty);
        context.inject(configurator);
        configurator.setParameterSet(new bunwarpj.Param());
        System.out.println(configurator.getParameterSet());

    }

    protected Workflow generateWorkflow() {

        int channel = targetSelector.getSelectedChannel();
        File landmarkFile = landmarkFileProperty.getValue();
        bunwarpj.Param parameters = configurator.getParameterSet();
        Workflow workflow =  new WorkflowBuilder(context)
                .addStep(ElasticCorrection.class, "landmarkFile", landmarkFile, "channel", channel, "parameters", parameters)
                .getWorkflow("Elactic correction workflow");
        
        return workflow;

    }

    protected String getMessage() {

        // int channel1 = sourceSelector.getSelectedChannel();
        int channel2 = targetSelector.getSelectedChannel();
        File landmarkFile = landmarkFileProperty.getValue();
        if (landmarkFile == null) {
            return "!First, select a landmark file.!";
        }

        if (channel2 == -1) {
            return "*Now select the channel that will be modified*";
        } else {
            return String.format(FINAL_EXPLANATION, channel2, landmarkFile.getName());
        }

    }

}
