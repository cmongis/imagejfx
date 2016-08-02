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

import ijfx.core.imagedb.ImageLoaderService;
import ijfx.service.ImagePlaneService;
import ijfx.service.batch.SilentImageDisplay;
import ijfx.service.dataset.DatasetUtillsService;
import ijfx.ui.datadisplay.image.ImageDisplayPane;
import io.datafx.controller.ViewController;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import net.imagej.Dataset;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugins.commands.io.OpenFile;

/**
 *
 * @author Tuan anh TRINH
 */
@ViewController(value = "FlatfieldWorkflow.fxml")
public class FlatfieldWorkflow extends AbstractCorrectionActivity {

    @Inject
    WorkflowModel workflowModel;

    @FXML
    Button folderButton;

    @Parameter
    ImageLoaderService imageLoaderService;

    @Parameter
    CommandService commandService;

    @Parameter
    Context context;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    ImagePlaneService imagePlaneService;
    protected ImageDisplayPane imageDisplayPane;

    @Parameter
    DatasetUtillsService datasetUtillsService;

    @Parameter
    DisplayService displayService;

    public FlatfieldWorkflow() throws IOException {
        CorrectionActivity.getStaticContext().inject(this);
        imageDisplayPane = new ImageDisplayPane(context);
    }

    @PostConstruct
    public void init() {
        imageDisplayPane.setOnMouseClicked(e -> displayService.setActiveDisplay(imageDisplayPane.getImageDisplay()));
        borderPane.setCenter(imageDisplayPane);
        folderButton.setOnAction(this::openImage);
        workflowModel.getFlatFieldImageDisplay().ifPresent((e) -> imageDisplayPane.display(e));
    }

    protected void openImage(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        try {
            Dataset flatFieldDataset = imagePlaneService.openVirtualDataset(file);
            ImageDisplay imageDisplay = displayDataset(flatFieldDataset);
            workflowModel.setFlatFieldImageDisplay1(imageDisplay);
        } catch (IOException ex) {
            Logger.getLogger(FlatfieldWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected ImageDisplay displayDataset(Dataset flatFieldDataset) {
        ImageDisplay imageDisplay = new SilentImageDisplay(context, flatFieldDataset);
        imageDisplay.display(flatFieldDataset);
        imageDisplayPane.display(imageDisplay);
        return imageDisplay;
    }

}
