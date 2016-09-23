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
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
@ViewController(value = "FolderWorkflow.fxml")
public class FolderWorkflow extends CorrectionFlow {

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
    GridPane gridPane;

    @FXML
    Button chooseFolder;

    @FXML
    BorderPane borderPane;
    
    ImageDisplayPane imageDisplayPaneLeft;

    ImageDisplayPane imageDisplayPaneRight;


    ObjectProperty<long[]> positionLeftProperty = new SimpleObjectProperty<>();

    ObjectProperty<long[]> positionRightProperty = new SimpleObjectProperty<>();

    public FolderWorkflow() {
        try {
            CorrectionActivity.getStaticContext().inject(this);
            imageDisplayPaneLeft = new ImageDisplayPane(context);
            imageDisplayPaneRight = new ImageDisplayPane(context);
            
        } catch (IOException ex) {
            Logger.getLogger(FolderWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @PostConstruct
    public void init() {
        
        
      
        
        // TODO : prevent direct access to the model
        workflowModel.positionLeftProperty.bindBidirectional(positionLeftProperty);
        workflowModel.positionRightProperty.bindBidirectional(positionRightProperty);

        
        
        gridPane.add(imageDisplayPaneLeft, 1, 1);
        gridPane.add(imageDisplayPaneRight, 2, 1);

        initListView();
        ImageDisplayPane[] imageDisplayPanes = new ImageDisplayPane[]{imageDisplayPaneLeft, imageDisplayPaneRight};
        bindPaneProperty(Arrays.asList(imageDisplayPanes));
        
        
        
        
//        Only when the user come back
        if (listView.getItems().size() > 0) {
            workflowModel.openImage(this.imageDisplayPaneLeft, this.imageDisplayPaneRight, listView.getItems().get(0))
                    .thenRunnable(this::applyPosition)
                    .start();
        }
        
        initWebView();
        
        if (listView.getItems().size() > 0) {
            nextButton.setDisable(false);
        }
        
        Platform.runLater(this::initWebView);
        
        

    }
    
    @PreDestroy
    public void destroy() {
         // binding to the model
       
        workflowModel.positionLeftProperty.unbindBidirectional(positionLeftProperty);
        workflowModel.positionRightProperty.unbindBidirectional(positionRightProperty);
    }
    
    public void initWebView() {
        WebView webView = new WebView();
        borderPane.setTop(webView);
        initWebView(webView, "FolderWorkflow.md");
    }

  
 

    /**
     * Save the position whan the axis Change
     *
     * @param event
     */
    @EventHandler
    public void handleEvent(AxisPositionEvent event) {
        System.out.println(imageDisplayPaneLeft.getImageDisplay());
        if (imageDisplayPaneLeft.getImageDisplay() == event.getDisplay()) {
            savePosition(event.getDisplay(), positionLeftProperty);
        } else if (imageDisplayPaneRight.getImageDisplay() == event.getDisplay()) {
            savePosition(event.getDisplay(), positionRightProperty);

        }

    }

    /**
     * Save the positions of the imageDisplay
     *
     * @param imageDisplay
     * @param property
     */
    private void savePosition(ImageDisplay imageDisplay, ObjectProperty<long[]> property) {
        long[] position = new long[imageDisplay.numDimensions()];
        imageDisplay.localize(position);
        property.set(position);
    }

    /**
     *
     */
    private void initListView() {
        
          List<File> selectedFiles = workflowModel.getSelectedFiles();
       
        listView.getItems().addAll(selectedFiles);
 
        setCellFactory(listView);

        listView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends File> obs, File old, File newValue) -> {
            workflowModel.openImage(imageDisplayPaneLeft, imageDisplayPaneRight, newValue)
                    .thenRunnable(() -> {

                        if (workflowModel.getPositionLeft().length == imageDisplayPaneLeft.getImageDisplay().numDimensions()) {
                            applyPosition();
                        } else {
                            savePosition(imageDisplayPaneLeft.getImageDisplay(), positionLeftProperty);
                            savePosition(imageDisplayPaneRight.getImageDisplay(), positionRightProperty);
                        }
                    })
                    .start();

        });
    }

    private void applyPosition() {
        workflowModel.setPosition(workflowModel.getPositionLeft(), imageDisplayPaneLeft.getImageDisplay());
        workflowModel.setPosition(workflowModel.getPositionRight(), imageDisplayPaneRight.getImageDisplay());

    }

}
