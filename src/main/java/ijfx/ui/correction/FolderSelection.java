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


import ijfx.ui.widgets.ExplorableSelector;
import io.datafx.controller.ViewController;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import ijfx.core.imagedb.ImageLoaderService;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.widgets.FileExplorableWrapper;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import mongis.utils.FileButtonBinding;
import mongis.utils.transition.OpacityTransitionBinding;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
@ViewController(value = "FolderSelection.fxml")
public class FolderSelection extends CorrectionFlow {

    @FXML
    BorderPane borderPane;

    @FXML
    Button folderButton;

    WebView webView;

    ExplorableSelector explorerSelector = new ExplorableSelector();

    FileButtonBinding fileButtonBinding;

    @Inject
    WorkflowModel workflowModel;

    Logger logger = ImageJFX.getLogger();
    
    OpacityTransitionBinding opacityBinding;
    
    @Parameter
    ImageLoaderService imageLoaderService;
    
    public FolderSelection() {

    }

    @PostConstruct
    public void init() {
        
        CorrectionActivity.getStaticContext().inject(this);
        
        borderPane.setCenter(explorerSelector);

        fileButtonBinding = new FileButtonBinding(folderButton,workflowModel.getSelectedDirectory())
                .setButtonDefaultText("Select folder to import");
        
        
        //logger.info(String.format("Current forlder : %s, %s files selected",workflowModel.getSelectedDirectory(),workflowModel.));
        
        
        
        fileButtonBinding.fileProperty().bindBidirectional(workflowModel.directoryProperty());
        
        
        // getting the observable list of items marked for processing
        ListProperty<Explorable> selectedItems = new SimpleListProperty(explorerSelector.markedItemProperty());
        ObservableValue<Boolean> selectionValid = selectedItems.emptyProperty();
        
        
        Platform.runLater(()->{
            webView = new WebView();
            borderPane.setTop(webView);
            initWebView(webView, "FolderSelection.md");
        
        });
        
        // the next button can only be pressed if items are selected
        nextButton.disableProperty().bind(selectionValid);
        finishButton.disableProperty().bind(selectionValid);
        backButton.setDisable(true);
        
        //
        opacityBinding = new OpacityTransitionBinding(explorerSelector, fileButtonBinding.fileProperty().isNotNull());
       
        // setting the current list to the current value
        onAvailableFileChanged(null, null, workflowModel.fileListProperty().getValue());
        
        // listening for change of the list
        workflowModel.fileListProperty().addListener(this::onAvailableFileChanged);
        
        
    }

    @PreDestroy
    private void destroy() {
        
        workflowModel.directoryProperty().unbindBidirectional(fileButtonBinding.fileProperty());
        workflowModel.fileListProperty().removeListener(this::onAvailableFileChanged);
        explorerSelector.dispose();
    }
    

  
    
    private void onAvailableFileChanged(Observable obs, List<? extends Explorable> oldValue, List<? extends Explorable> newValue) {
       
        explorerSelector.setItems(newValue);
    }
    
}
