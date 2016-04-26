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
package ijfx.ui.plugin.panel;

import ijfx.service.overlay.OverlaySelectionService;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import mongis.utils.FXUtilities;
import mongis.utils.ListCellController;
import mongis.utils.ListCellControllerFactory;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.event.OverlayCreatedEvent;
import net.imagej.overlay.Overlay;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayActivatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */

@UiConfiguration(id = "overlay-manager-panel", context = "image-open+imagej+overlay-selected", localization = Localization.RIGHT)
public class OverlayManagerPanel extends TitledPane implements UiPlugin {

    Logger logger = ImageJFX.getLogger();

    @FXML
    ListView<Overlay> overlayListView;

    ObservableList<Overlay> displayedOverlayList = FXCollections.observableArrayList();

    //EasyCellFactory<Overlay,OverlayListCellController> easyCellFactory  = new EasyCellFactory<>(OverlayListCellController.class);
    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    DisplayService displayService;

    @Parameter
    OverlayService overlayService;

    @Parameter
    OverlaySelectionService overlaySelectionService;

    ListCellControllerFactory<Overlay> listCellControllerFactory = new ListCellControllerFactory<>(this::createListCellController);

    Display currentDisplay;
    
    public OverlayManagerPanel() {
        try {
            FXUtilities.injectFXML(this);

            overlayListView.setCellFactory(listCellControllerFactory);
            overlayListView.setItems(displayedOverlayList);

            overlayListView.getSelectionModel().selectedItemProperty().addListener(this::onListOverlaySelectionChanged);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {
        updateList();
        return this;
    }

    @EventHandler
    public void onActiveDisplayChanged(DisplayActivatedEvent event) {
        
        if(event.getDisplay() != currentDisplay) {
            currentDisplay = event.getDisplay();
            Platform.runLater(() -> updateList());
            
        }
    }

    @FXML
    public void updateList() {

        logger.info("" + overlayService);
        if (overlayService == null) {
            return;
        }

        ImageDisplay currentDisplay = imageDisplayService.getActiveImageDisplay();
        if (currentDisplay == null) {
            return;
        }
        displayedOverlayList.clear();
        displayedOverlayList.addAll(
                overlayService.getOverlays(currentDisplay));
    }

    @EventHandler
    public void onOverlayCreated(OverlayCreatedEvent event) {

    }

    private class OverlayListCellController extends Label implements ListCellController<Overlay> {

        public OverlayListCellController() {

            getStyleClass().add("overlay-ctrl");
        }

        @Override
        public void setItem(Overlay t) {
            if (t.getName() == null) {
                setText("Untitled overlay");
            } else {
                setText(t.getName());
            }

        }

        @Override
        public Overlay getItem() {
            return null;
        }

    }

    private OverlayListCellController createListCellController() {
        return new OverlayListCellController();
    }

    private void onListOverlaySelectionChanged(Observable obs, Overlay old, Overlay newValue) {
        if (newValue == null) {
            return;
        }
        overlaySelectionService.selectOnlyOneOverlay(displayService.getActiveDisplay(ImageDisplay.class), newValue);

    }

}
