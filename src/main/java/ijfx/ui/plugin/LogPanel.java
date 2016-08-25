/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.ui.plugin;

import ijfx.ui.UiPlugin;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import ijfx.service.log.DefaultLoggingService;
import ijfx.service.log.DefaultLoggingService.LogErrorEvent;
import ijfx.ui.report.ReportDialog;
import java.io.IOException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import mercury.core.LogEntry;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import mongis.utils.EasyCellFactory;
import mongis.utils.FXUtilities;
import ijfx.ui.UiConfiguration;

/**
 *
 * @author Cyril MONGIS, 2015
 */

@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "log-panel", context = "webapp imagej explorerActivity segmentation", localization = Localization.TOP_LEFT)
public class LogPanel extends BorderPane implements UiPlugin {

    @FXML
    private ListView<LogEntry> listView;

    private static final ObservableList<LogEntry> logEntryList = FXCollections.observableArrayList();

   
    @FXML
    private Label itemCountLabel;

    PopOver popover;


    
    LogPanel errorPanel;

    ToggleButton toggleButton = new ToggleButton();

    @Parameter
    EventService eventService;

    @Parameter
    Context context;
    
     @Parameter
    private DefaultLoggingService logService;

   

    
    // Injecting the FXML to the controller
    public LogPanel() throws IOException {
        FXUtilities.injectFXML(this, "LogPanel.fxml");

        // setting the cell factory and the items list
        listView.setCellFactory(new EasyCellFactory<>(LogEntryListCellCtrl.class));
        listView.setItems(logEntryList);
        toggleButton.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.INFO_CIRCLE));
        toggleButton.getStyleClass().add("icon");
        
    }
    
      @Override
    public UiPlugin init() {
                
        // taking the error list from the log error service
        logEntryList.addAll(logService.getErrorList());
        
        // creating the PopOver
        popover = new PopOver();
        
        // the content of the Popover is the ErrorPanel itself
        popover.setContentNode(this);
        popover.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
        toggleButton.selectedProperty().addListener((obs,oldvalue,newvalue)->{
          
            if(newvalue) {
                popover.show(toggleButton);
                
            }
            else popover.hide();
        
        });
        
        popover.showingProperty().addListener((obs,oldvalue,newvalue)->{
        
            if(!newvalue) {
                toggleButton.selectedProperty().setValue(false);
            }
        });
        
        //toggleButton.selectedProperty().bind(popover.showingProperty());
        
        
        return this;
    }
    
    
    @Override
    public Node getUiElement() {
        
        // the returned wigdet is actually a toggle button
        // that will display the panel in a pop over
        // when clicked by the user
        return toggleButton;
    }

    @EventHandler
    public void handleEvent(LogErrorEvent event) {
        
        // in case of log error event,
        // the entry is added to the log list
        Platform.runLater(() -> {
            logEntryList.add(event.getError());
            itemCountLabel.setText(String.format("%d errors", logService.getErrorCount()));
        });

        Platform.runLater(() -> updateButton());
    }

    
    @FXML
    // Deletes all the entry from the display
    public void dismissAll() {
        
        logEntryList.clear();
        logService.resetErrorCount();
        fireEvent(new Event(ErrorButton.POPOVER_CLOSE_REQUEST));
    }

    @FXML
    // closes the panel
    public void closePanel() {
        popover.hide();
    }

    @FXML
    public void sendToDevelopers() {
        
        
        
        ReportDialog reportDialog = new ReportDialog(context);
        reportDialog.showAndWait();
       
    }

    // returns the toggle button
    public ToggleButton getButton() {
        return toggleButton;
    }

  
    // change the color of the button when errors are in the list
    public void updateButton() {
        
        
        FXUtilities.toggleCssStyle(getButton(),ImageJFX.BUTTON_DANGER_CLASS,logService.getErrorCount() > 0);
        
        if (logService.getErrorCount() > 0) {
           
            getButton().setText(logService.getErrorCount() + "");
        } else {
            getButton().setText("");
        }
    }

    // update the button when... what is this ?
    @EventHandler
    public void handleEvent(DefaultLoggingService.CountResetChange event) {
        Platform.runLater(() -> updateButton());
    }

}
