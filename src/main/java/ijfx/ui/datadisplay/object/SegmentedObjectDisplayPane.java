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
package ijfx.ui.datadisplay.object;

import ijfx.core.Handles;
import ijfx.plugins.commands.measures.SaveAsCsv;
import ijfx.service.batch.SegmentedObject;
import ijfx.service.overlay.OverlaySelectionService;
import ijfx.service.overlay.OverlayStatService;
import ijfx.service.overlay.OverlayUtilsService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.datadisplay.DisplayPanePlugin;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.view.SegmentedObjectExplorerWrapper;
import ijfx.ui.explorer.view.TableViewView;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.utils.ChartUpdater;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import net.imagej.display.ImageDisplayService;
import net.imagej.overlay.Overlay;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = DisplayPanePlugin.class)
@Handles(type = SegmentedObjectDisplay.class)
public class SegmentedObjectDisplayPane extends BorderPane implements DisplayPanePlugin<SegmentedObjectDisplay> {

    SegmentedObjectDisplay display;

    TableViewView view = new TableViewView();

    @Parameter
    OverlaySelectionService service;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    OverlayUtilsService overlayUtilsService;

    @Parameter
    EventService eventService;

    @Parameter
    CommandService commandService;
    
    @Parameter
            LoadingScreenService loadingScreenService;
    
    Logger logger = ImageJFX.getLogger();

    @FXML
    Tab tableTab;
    
    @FXML
    BorderPane tableBorderPane;
    
    @FXML
    AreaChart<Double,Double> areaChart;
    
    @Parameter
    OverlayStatService overlayStatsService;
    
    ChartUpdater chartUpdater;
    
    StringProperty titleProperty = new SimpleStringProperty();
    
    public SegmentedObjectDisplayPane(Context context) {
        try {
            logger.info("Creating display view");
            
            FXUtilities.injectFXML(this);

            context.inject(view);
            context.inject(this);
            tableBorderPane.setCenter(view.getNode());
            
            chartUpdater = new ChartUpdater(areaChart);
            chartUpdater.setMaximumBinNumber(200);
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }

    }

    public void display(SegmentedObjectDisplay display) {
        this.display = display;
        update();

    }

    public void update() {
        logger.info("Updating current display");
        view.setItem(display.
                stream()
                .map(this::transform)
                .collect(Collectors.toList())
        );

    }

    public SegmentedObjectExplorerWrapper bind(SegmentedObjectExplorerWrapper wrapper) {
        wrapper.selectedProperty().addListener((obs, o, newValue) -> {

            logger.info("Wrapper selected !");
            if (wrapper.getObject() instanceof DisplayedSegmentedObject) {
                DisplayedSegmentedObject object = (DisplayedSegmentedObject) wrapper.getObject();
                object.setSelection(newValue);
            }

        });
        return wrapper;
    }

   
    
    @EventHandler
    public void onDisplayUpdated(DisplayUpdatedEvent event) {
        if(event.getDisplay() == display) {
            Platform.runLater(this::update);
        }
    }

    public Explorable transform(SegmentedObject object) {
        SegmentedObjectExplorerWrapper wrapper = new SegmentedObjectExplorerWrapper(object);
        bind(wrapper);
        return wrapper;
    }

    @FXML
    public void removeSelectedOverlays() {
       
        List<Overlay> selectedOverlays = view.getSelectedItems()
                .stream()
                .map(explorable -> (SegmentedObjectExplorerWrapper) explorable)
                .map(explorable -> explorable.getObject().getOverlay())
                .collect(Collectors.toList());
        removeSelectedMeasures();
        overlayUtilsService.removeOverlay(imageDisplayService.getActiveImageDisplay(), selectedOverlays);
    }

    @FXML
    public void removeSelectedMeasures() {
        display.removeAll(view.getSelectedItems()
                .stream()
                .map(explorable -> (SegmentedObjectExplorerWrapper) explorable)
                .map(explorable -> explorable.getObject())
                .collect(Collectors.toList())
        );

        display.update();
        update();
    }
    
    @FXML
    public void saveAsCsv() {
        commandService.run(SaveAsCsv.class, true);
    }
    
    @FXML
    public void calculateHistogram() {
        
        List<Double> values = new ArrayList<>();
        
        for(SegmentedObject object : display) {
         
            if(object instanceof DisplayedSegmentedObject) {
                DisplayedSegmentedObject displayedObject = (DisplayedSegmentedObject) object;
                Collections.addAll(values, displayedObject.getPixelsValues());
            }
        
        }
        chartUpdater.setMaximumBinNumber(400);
        chartUpdater.setPossibleValue(values);
        
        new CallbackTask<>()
                .run(chartUpdater::updateChart)
                .submit(loadingScreenService)
                .setInitialProgress(50)
                .setName("Updating chart")
                .start();
        
    }

    @Override
    public void dispose() {
        display.clear();
        display.close();
    }

    @Override
    public StringProperty titleProperty() {
        return titleProperty();
    }

    @Override
    public Pane getPane() {
        return this;
    }
}
