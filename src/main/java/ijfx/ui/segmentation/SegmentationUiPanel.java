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
package ijfx.ui.segmentation;

import ijfx.service.batch.SegmentationService;
import ijfx.service.overlay.OverlayUtilsService;
import ijfx.service.workflow.Workflow;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.main.Localization;
import ijfx.ui.utils.ImageDisplayProperty;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayService;
import net.imagej.overlay.BinaryMaskOverlay;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.type.logic.BitType;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

/**
 *
 * @author cyril
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "segmentation-panel-2", localization = Localization.RIGHT, context = "segment segmentation")
public class SegmentationUiPanel extends BorderPane implements UiPlugin {

    @Parameter
    private Context context;

    @Parameter
    private PluginService pluginService;

    @Parameter
    private OverlayService overlayService;
   
    @Parameter
    private OverlayUtilsService overlayUtilsService;
    
    @Parameter
    private SegmentationService segmentationService;
    
    @FXML
    Accordion accordion;
    
    private final Map<TitledPane, SegmentationUiPlugin> nodeMap = new HashMap<>();

    private ImageDisplayProperty imageDisplayProperty;

    public SegmentationUiPanel() {
        
        try {
            FXUtilities.injectFXML(this,"/ijfx/ui/segmentation/SegmentationUiPanel.fxml");
            setPrefWidth(200);
            accordion.expandedPaneProperty().addListener(this::onExpandedPaneChanged);
        } catch (IOException ex) {
            Logger.getLogger(SegmentationUiPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {

        // initializing a fx property
        imageDisplayProperty = new ImageDisplayProperty(context);

        // when the display is changed, we want to notify only the current wrapper
        imageDisplayProperty.addListener(this::onImageDisplayChanged);
        
        
        // adding all the UI Plugins
        pluginService
                .createInstancesOfType(SegmentationUiPlugin.class)
                .stream()
                .map(PluginWrapper::new)
                .forEach(this::addPlugin);
        return this;
    }

    private SegmentationUiPlugin getActivePlugin() {
        return nodeMap.get(getExpandedPane());
    }

    
    private void onExpandedPaneChanged(Observable obs, TitledPane oldPane, TitledPane newPane) {
        nodeMap.get(newPane).setImageDisplay(getCurrentImageDisplay());
    }
    
    private void onMaskChanged(Observable obs, Img<BitType> oldMask, Img<BitType> newMask) {
        
        if(newMask != null && getCurrentImageDisplay() != null) {
            
            new CallbackTask()
                    .run(()->updateMask(getCurrentImageDisplay(),newMask))
                    .start();
            
        }
        
    }
    
    private ImageDisplay getCurrentImageDisplay() {
        return imageDisplayProperty.getValue();
    }
    
    //adds a wrapper
    private void addPlugin(PluginWrapper wrapper) {
        
        
        wrapper.maskProperty().addListener(this::onMaskChanged);
        // put it in a map with the corresponding plugin
        nodeMap.put(wrapper, wrapper.getPlugin());
        getPanes().add(wrapper);

    }

    private List<TitledPane> getPanes() {
        return accordion.getPanes();
    }
    
    private TitledPane getExpandedPane() {
        return accordion.getExpandedPane();
    }
    
    // 
    private void onImageDisplayChanged(Observable obs, ImageDisplay oldValue, ImageDisplay newValue) {
        
        if(getActivePlugin() != null)
        getActivePlugin().setImageDisplay(newValue);

    }

    private BinaryMaskOverlay getBinaryMask(ImageDisplay imageDisplay) {
        return overlayUtilsService.findOverlayOfType(imageDisplay, BinaryMaskOverlay.class);
    }
    
    private BinaryMaskOverlay createBinaryMaskOverlay(ImageDisplay imageDisplay,Img<BitType> mask) {
        
        BinaryMaskOverlay overlay = new BinaryMaskOverlay(context, new BinaryMaskRegionOfInterest<>(mask));
        
        overlayService.addOverlays(imageDisplay, Arrays.asList(overlay));
        
        return overlay;
        
    }
    
    private synchronized void updateMask(ImageDisplay imageDisplay, Img<BitType> mask) {
        
        BinaryMaskOverlay overlay = getBinaryMask(imageDisplay);
        if(overlay == null) {
            overlay = createBinaryMaskOverlay(imageDisplay, mask);
        }
        else {
            
            BinaryMaskRegionOfInterest regionOfInterest = (BinaryMaskRegionOfInterest) overlay.getRegionOfInterest();
            
            RandomAccessibleInterval<BitType> img = regionOfInterest.getImg();
            RandomAccess<BitType> randomAccess = img.randomAccess();
            Cursor<BitType> cursor = mask.cursor();
            
            cursor.reset();
            while(cursor.hasNext()) {
                cursor.fwd();
                randomAccess.setPosition(cursor);
                randomAccess.get().set(cursor.get());
            }
            
            
        }
        overlay.update();
    }
    
    // Wrapping class
    private class PluginWrapper extends TitledPane implements SegmentationUiPlugin {

        private final SegmentationUiPlugin plugin;

        public PluginWrapper(SegmentationUiPlugin plugin) {
            this.plugin = plugin;
            setText(plugin.getName());
            setContent(plugin.getContentNode());

        }

        public SegmentationUiPlugin getPlugin() {
            return plugin;
        }

        @Override
        public void setImageDisplay(ImageDisplay display) {
            plugin.setImageDisplay(display);
        }

        @Override
        public Node getContentNode() {
            return this;
        }

        @Override
        public Workflow getWorkflow() {
            return plugin.getWorkflow();
        }

        @Override
        public Property<Img<BitType>> maskProperty() {
            return plugin.maskProperty();
        }

        public String getName() {
            return plugin.getName();
        }

    }

}
