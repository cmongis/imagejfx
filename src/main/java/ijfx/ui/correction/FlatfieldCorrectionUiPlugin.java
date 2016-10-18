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
import ijfx.plugins.flatfield.DarkfieldSubstraction;
import ijfx.plugins.flatfield.FlatFieldCorrection;
import ijfx.plugins.flatfield.MultiChannelFlatfieldCorrection;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowBuilder;
import java.io.File;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import mongis.utils.FileButtonBinding;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = CorrectionUiPlugin.class, label = "Flatfield Correction")
public class FlatfieldCorrectionUiPlugin extends AbstractCorrectionUiPlugin {

    private ChannelSelector channelSelector = new ChannelSelector(null);

    @FXML
    Button flatfieldButton;

    @FXML
    Group channelSelectorGroup;

    @FXML
    Button darkfieldButton;

    final Property<File> flatfieldImage;

    final Property<File> darkfieldImage;

    final FileButtonBinding flatfieldBinding;// = new FileButtonBinding(flatfieldButton);

    final FileButtonBinding darkfieldBinding;

    @FXML
    CheckBox multichannelCheckbox;

    @FXML
    CheckBox darkfieldCorrectionCheckBox;

    @Parameter
    Context context;

    private static String EXPLANATION_FORMAT = "%s correction of %s with *%s* (%s)";

    @Override
    public void init() {

    }

    public FlatfieldCorrectionUiPlugin() {
        super("/ijfx/ui/correction/FlatfieldCorrectionUiPlugin.fxml");
        // setTop(multichannelCheckbox);
        //setLeft(channelSelector);
        //setBottom(flatfieldButton);
        flatfieldButton.setMaxWidth(Double.POSITIVE_INFINITY);
        getStyleClass().add("with-padding");

        channelSelectorGroup.getChildren().add(channelSelector);

        flatfieldBinding = new FileButtonBinding(flatfieldButton);
        flatfieldBinding.setButtonDefaultText("Choose Flatfield image...");
        flatfieldBinding.setOpenFile(true);
        flatfieldImage = flatfieldBinding.fileProperty();

        darkfieldBinding = new FileButtonBinding(darkfieldButton)
                .setButtonDefaultText("Choose darkfield image...")
                .setOpenFile(true);
        darkfieldImage = darkfieldBinding.fileProperty();

        //bind(datasetAssetProperty, this::createDatasetAsset, flatfieldImage);
        bindP(explanationProperty, this::generateExplanation, flatfieldImage, darkfieldImage, channelSelector.selectedChannelProperty(), multichannelCheckbox.selectedProperty());

        bindP(workflowProperty, this::generateWorkflow, flatfieldImage, darkfieldImage, channelSelector.selectedChannelProperty(), multichannelCheckbox.selectedProperty());

        channelSelector.disableProperty().bind(multichannelCheckbox.selectedProperty());

    }

    protected DatasetAsset createDatasetAsset() {
        return new DatasetAsset(flatfieldImage.getValue());
    }

    protected boolean handleDarkfield() {
        return !darkfieldCorrectionCheckBox.isSelected();
    }

    protected Workflow generateWorkflow() {

        if (getSelectedFile() == null) {
            return null;
        }

        return new WorkflowBuilder(context)
                // if darkfield should be handled for the files and for the flatfield, we had a step to the workflow
                .addStepIfTrue(handleDarkfield(), DarkfieldSubstraction.class, "file", darkfieldImage.getValue(), "multichannel", isMultiChannel())
                
                // if it's multichannel flatfield, let's handle it
                .addStepIfTrue(isMultiChannel(), MultiChannelFlatfieldCorrection.class, "flatfield", flatfieldImage.getValue(), "darkfield", darkfieldImage.getValue())
                
                // if not, we add a normal flatfield correction to the workflow
                .addStepIfTrue(!isMultiChannel(), FlatFieldCorrection.class, "channel", getSelectedChannel(), "flatfield", flatfieldImage.getValue(), "darkfield", darkfieldImage.getValue())
                .getWorkflow("Multichannel Flatfield correction");

    }

    protected Integer getSelectedChannel() {
        return channelSelector.selectedChannelProperty().getValue();
    }

    protected File getSelectedFile() {
        return flatfieldImage.getValue();
    }

    protected boolean isMultiChannel() {
        return multichannelCheckbox.isSelected();
    }

    protected String generateExplanation() {

        if (flatfieldImage.getValue() == null) {
            return "!Please select the flafield image.!";
        }

        int selectedChannel = channelSelector.selectedChannelProperty().getValue();

        final String correctionType, target, file, isMultiChannel;

        isMultiChannel = isMultiChannel() ? "*multi*-channel" : "*mono*-channel";

        target = isMultiChannel() || selectedChannel == -1 ? "ALL" : "channel " + (selectedChannel + 1);

        correctionType = "Flafield";

        file = flatfieldImage.getValue().getName();

        return String.format(EXPLANATION_FORMAT, correctionType, target, file, isMultiChannel);

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

    @FXML
    public void multichannelHelp() {

    }

    @FXML
    public void darkfieldHelp() {

    }

}
