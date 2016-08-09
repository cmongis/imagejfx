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
import ijfx.ui.datadisplay.image.ImageDisplayPane;
import io.datafx.controller.ViewController;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import net.imagej.display.ImageDisplay;
import net.imagej.display.event.AxisPositionEvent;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.io.IOService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Tuan anh TRINH
 */
@ViewController(value = "WelcomeWorkflow.fxml")
public class WelcomeWorkflow extends CorrectionFlow {

    @Inject
    WorkflowModel workflowModel;

    @Parameter
    IOService iOService;

    @Parameter
    ImageLoaderService imageLoaderService;

    @Parameter
    Context context;
    @FXML
    ListView<File> listView;

    @FXML
    Button chooseFolder;

    ImageDisplayPane imageDisplayPaneLeft;

    ImageDisplayPane imageDisplayPaneRight;

    ObjectProperty<List<File>> listProperty = new SimpleObjectProperty<>();

    ObjectProperty<int[]> positionLeftProperty = new SimpleObjectProperty<>();

    ObjectProperty<int[]> positionRightProperty = new SimpleObjectProperty<>();

    public WelcomeWorkflow() {
        try {
            CorrectionActivity.getStaticContext().inject(this);
            imageDisplayPaneLeft = new ImageDisplayPane(context);
            imageDisplayPaneRight = new ImageDisplayPane(context);

        } catch (IOException ex) {
            Logger.getLogger(WelcomeWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @PostConstruct
    public void init() {
        workflowModel.bindWelcome(this);
        borderPane.setCenter(imageDisplayPaneLeft);
        borderPane.setRight(imageDisplayPaneRight);
        initListView();
        ImageDisplayPane[] imageDisplayPanes = new ImageDisplayPane[]{imageDisplayPaneLeft,imageDisplayPaneRight};
        bindPaneProperty(Arrays.asList(imageDisplayPanes));
        //Only when the user come back
//        if (listProperty.get().size() > 0) {
//            workflowModel.openImage(this.imageDisplayPaneLeft, this.imageDisplayPaneRight, listProperty.get().get(0));
//            applyPosition();
//        }
    }

    @FXML
    public void addFiles() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(null);
        List<File> list = (List<File>) imageLoaderService.getAllImagesFromDirectory(file);
        listProperty.set(list);
        listView.getItems().addAll(list);
        workflowModel.openImage(this.imageDisplayPaneLeft, this.imageDisplayPaneRight, list.get(0));
    }

    @EventHandler
    public void handleEvent(AxisPositionEvent event) {
        System.out.println(imageDisplayPaneLeft.getImageDisplay());
        if (imageDisplayPaneLeft.getImageDisplay() == event.getDisplay()) {
            savePosition(event.getDisplay(), positionLeftProperty);
        } else if (imageDisplayPaneRight.getImageDisplay() == event.getDisplay()) {
            savePosition(event.getDisplay(), positionRightProperty);

        }

    }

    private void savePosition(ImageDisplay imageDisplay, ObjectProperty<int[]> property) {
        int[] position = new int[imageDisplay.numDimensions()];
        imageDisplay.localize(position);
        property.set(position);
    }

    private void initListView() {
        listView.getItems().addAll(listProperty.get());
        setCellFactory(listView);
        
        listView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends File> obs, File old, File newValue) -> {
            workflowModel.openImage(imageDisplayPaneLeft, imageDisplayPaneRight, newValue);
            if (workflowModel.getPositionLeft().length == imageDisplayPaneLeft.getImageDisplay().numDimensions()) {
                workflowModel.setPosition(workflowModel.getPositionLeft(), imageDisplayPaneLeft.getImageDisplay());
                workflowModel.setPosition(workflowModel.getPositionRight(), imageDisplayPaneRight.getImageDisplay());
            } else {
                savePosition(imageDisplayPaneLeft.getImageDisplay(), positionLeftProperty);
                savePosition(imageDisplayPaneRight.getImageDisplay(), positionRightProperty);
            }

        });
    }

}
