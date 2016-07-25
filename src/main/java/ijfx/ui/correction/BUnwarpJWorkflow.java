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

import ijfx.service.ImagePlaneService;
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
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import net.imagej.Dataset;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.plugin.Parameter;
import org.scijava.plugins.commands.io.OpenFile;

/**
 *
 * @author Tuan anh TRINH
 */
@ViewController(value = "BUnwarpJWorkflow.fxml")
public class BUnwarpJWorkflow extends AbstractCorrectionActivity {

    @Inject
    WorkflowModel workflowModel;

    @Parameter
    Context context;

    @Parameter
    ImagePlaneService imagePlaneService;

    protected ImageDisplayPane imageDisplayPaneLeft;

    protected ImageDisplayPane imageDisplayPaneRight;

    protected ImageDisplayPane imageDisplayPaneBottom;

    @FXML
    BorderPane imagesContainer;

    @FXML
    Button leftButton;

    @FXML
    Button rightButton;

    @FXML
    Button bottomButton;

    public BUnwarpJWorkflow() {
        CorrectionActivity.getStaticContext().inject(this);

    }

    @PostConstruct
    public void init() {
        try {
            imageDisplayPaneBottom = new ImageDisplayPane(context);
            imageDisplayPaneLeft = new ImageDisplayPane(context);
            imageDisplayPaneRight = new ImageDisplayPane(context);

            imageDisplayPaneRight.getCanvas().getCamera().addListener((e) -> imageDisplayPaneLeft.getCanvas().repaint());
        } catch (IOException ex) {
            Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
        rightButton.setOnAction(e -> {
            imagesContainer.setRight(imageDisplayPaneRight);
            openImage(imageDisplayPaneRight);
        });
        leftButton.setOnAction(e -> {
            imagesContainer.setLeft(imageDisplayPaneLeft);
            openImage(imageDisplayPaneLeft);
        });
        bottomButton.setOnAction(e -> {
            imagesContainer.setBottom(imageDisplayPaneBottom);
            openImage(imageDisplayPaneBottom);
        });

    }

    protected ImageDisplay displayDataset(Dataset dataset, ImageDisplayPane imageDisplayPane) {
        ImageDisplay imageDisplay = new DefaultImageDisplay();
        context.inject(imageDisplay);
        imageDisplay.display(dataset);
        imageDisplayPane.display(imageDisplay);
        return imageDisplay;
    }

    protected ImageDisplay openImage(ImageDisplayPane imageDisplayPane) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        Dataset flatFieldDataset = null;
        try {
            flatFieldDataset = imagePlaneService.openVirtualDataset(file);
        } catch (IOException ex) {
            Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
        return displayDataset(flatFieldDataset, imageDisplayPane);

    }
}
