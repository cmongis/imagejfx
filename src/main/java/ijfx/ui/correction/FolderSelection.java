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

import com.github.rjeschke.txtmark.Processor;
import ijfx.ui.widgets.ExplorableSelector;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import ijfx.core.imagedb.ImageLoaderService;
import ijfx.service.workflow.MyWorkflowService;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowBuilder;
import ijfx.ui.RichMessageDisplayer;
import ijfx.ui.activity.Activity;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.save.SaveOptions;
import ijfx.ui.widgets.SaveOptionDialog;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javax.annotation.PreDestroy;
import mongis.utils.FXUtilities;
import mongis.utils.FileButtonBinding;
import mongis.utils.transition.OpacityTransitionBinding;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
@Plugin(type = Activity.class, label = "correction-folder-selection", name = "Correction (Folder Selection)")
public class FolderSelection extends BorderPane implements Activity {

    @FXML
    Button folderButton;

    @FXML
    Button nextButton;

    @FXML
    Button redoButton;

   
    WebView webView;

    ExplorableSelector explorerSelector = new ExplorableSelector();

    FileButtonBinding fileButtonBinding;

    Logger logger = ImageJFX.getLogger();

    OpacityTransitionBinding opacityBinding;

    @Parameter
    ImageLoaderService imageLoaderService;

    @Parameter
    CorrectionUiService correctionUiService;

    @Parameter
    ActivityService activityService;

    @Parameter
    MyWorkflowService myWorkflowService;

    @Parameter
    Context context;

    @Parameter
            UIService uiService;
    
    RichMessageDisplayer displayer;

    public FolderSelection() {
        try {
            FXUtilities.injectFXML(this, "FolderSelection.fxml");
        } catch (IOException ex) {
            Logger.getLogger(FolderSelection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void init() {

        setCenter(explorerSelector);

        fileButtonBinding = new FileButtonBinding(folderButton)
                .setButtonDefaultText("Select folder to import");

     
        correctionUiService.sourceFolderProperty().bind(fileButtonBinding.fileProperty());
       
        // getting the observable list of items marked for processing
        ListProperty<Explorable> selectedItems = new SimpleListProperty(explorerSelector.markedItemProperty());
        ObservableValue<Boolean> selectionValid = selectedItems.emptyProperty();

        // creating the webview when it can and initializing it
        FXUtilities
                .createWebView()
                .then(this::initWebView);

        // the next button can only be pressed if items are selected
        nextButton.disableProperty().bind(selectionValid);
        redoButton.disableProperty().bind(selectionValid);

      
        opacityBinding = new OpacityTransitionBinding(explorerSelector, correctionUiService.fileListProperty().isNotNull());

        // listening for change of the list
        correctionUiService.fileListProperty().addListener(this::onAvailableFileChanged);

    }

    @PreDestroy
    private void destroy() {
        explorerSelector.dispose();
    }

    private void onAvailableFileChanged(Observable obs, List<? extends Explorable> oldValue, List<? extends Explorable> newValue) {

        explorerSelector.setItems(newValue);
    }

    @Override
    public Node getContent() {

        if (opacityBinding == null) {
            init();
        }

        return this;
    }

    @Override
    public Task updateOnShow() {

        return null;
    }

    protected void initWebView(WebView webView) {
        try {
            String resourceUrl = "FolderSelection.md";
            webView.setPrefHeight(150);
            setTop(webView);
            displayer = new RichMessageDisplayer(webView).addStringProcessor(Processor::process);
            displayer.setContent(this.getClass(), resourceUrl);
        } catch (IOException ex) {
            Logger.getLogger(FolderSelection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void next() {
        activityService.openByType(CorrectionSelector.class);
    }

    @FXML
    public void redo() {

        SaveOptionDialog options = new SaveOptionDialog();
        SaveOptions result = options.showAndWait().orElse(null);

        // abort if the user clicked on cancel
        if (result == null) {
            return;
        }

        Workflow importWorkflow = myWorkflowService.importWorkflow();

        // abort if the user clicked on cancel.
        if (importWorkflow == null) {
            return;
        }

        new WorkflowBuilder(context)
                .addInputFiles(correctionUiService.getSelectedFiles())
                .execute(importWorkflow)
                .saveUsingOptions(result)
                .startAndShow()
                .then(this::onWorkflowFinished);

    }
    
    public void onWorkflowFinished(Boolean result) {
       if(result) {
           uiService.showDialog("Workflow successfully applied");
       }
       else {
           uiService.showDialog("Damn it... something bad happened...");
       }
    }

}
