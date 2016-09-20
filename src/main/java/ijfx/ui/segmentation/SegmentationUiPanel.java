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

import ijfx.service.workflow.Workflow;
import ijfx.ui.UiPlugin;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import net.imagej.display.ImageDisplay;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

/**
 *
 * @author cyril
 */
@Plugin(type = UiPlugin.class)
public class SegmentationUiPanel extends Accordion implements UiPlugin{
    
    @Parameter
    PluginService pluginService;
    
    public SegmentationUiPanel(){
       
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {
        
       pluginService
               .createInstancesOfType(SegmentationUiPlugin.class)
               .stream()
               .map(PluginWrapper::new)
               .forEach(this::addPlugin);
        
       return this;
    }
    
   private void addPlugin(SegmentationUiPlugin plugin) {
       
       getPanes().add((TitledPane)plugin.getContentNode());
       
       
   }
    
    
   private class PluginWrapper extends TitledPane implements SegmentationUiPlugin {

       
       private final SegmentationUiPlugin plugin;

        public PluginWrapper(SegmentationUiPlugin plugin) {
            this.plugin = plugin;
            
            setContent(plugin.getContentNode());
            
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
