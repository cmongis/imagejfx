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
package ijfx.ui.datadisplay.metadataset;

import ijfx.plugins.commands.measures.SaveMetaDataSetAsCsv;
import ijfx.ui.datadisplay.object.*;
import ijfx.service.overlay.OverlayUtilsService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.explorer.MetaDataSetExplorerWrapper;
import ijfx.ui.explorer.view.SegmentedObjectExplorerWrapper;
import ijfx.ui.explorer.view.TableViewView;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.utils.ChartUpdater;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;
import org.deeplearning4j.ui.UiServer;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
public class MetaDataSetDisplayPane extends BorderPane {

    MetaDataSetDisplay display;

    TableViewView view = new TableViewView();

    @Parameter
    OverlayUtilsService overlayUtilsService;

    @Parameter
    EventService eventService;

    @Parameter
    CommandService commandService;
    
    @Parameter
    UIService uiService;
    
    @Parameter
            LoadingScreenService loadingScreenService;
    
    Logger logger = ImageJFX.getLogger();

    @FXML
    Tab tableTab;
    
    @FXML
    BorderPane tableBorderPane;
    
    @FXML
    AreaChart<Double,Double> areaChart;
    
    
    ChartUpdater chartUpdater;
    
    public MetaDataSetDisplayPane(Context context) {
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

    public void display(MetaDataSetDisplay display) {
        this.display = display;
        update();

    }

    public void update() {
        logger.info("Updating current display");
        List<MetaDataSetExplorerWrapper> collect = display
                .stream()
                .map(m->new MetaDataSetExplorerWrapper(m))
                .collect(Collectors.toList());
        view.setItem(collect);
        

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
        logger.info("updating");
        if(event.getDisplay() == display) {
            Platform.runLater(this::update);
        }
    }

  

    @FXML
    public void removeSelectedMeasures() {
        display.removeAll(view.getSelectedItems()
                .stream()
                .map(explorable -> explorable.getMetaDataSet())
                
                .collect(Collectors.toList())
        );

        display.update();
        update();
    }
    
    @FXML
    public void saveAsCsv() {
        commandService.run(SaveMetaDataSetAsCsv.class, true);
    }
    
   
}
