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
package ijfx.ui.datadisplay.image;

import ijfx.service.log.DefaultLoggingService;
import ijfx.ui.datadisplay.table.AbstractDisplayWindow;
import ijfx.ui.tool.ToolChangeEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.Pane;
import net.imagej.display.ImageDisplay;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class DefaultImageWindow extends AbstractDisplayWindow<ImageDisplay>{

    ImageDisplayPane pane;

    @Parameter
    Context context;
    
    @Parameter
    DefaultLoggingService logService;
    
    public DefaultImageWindow(Context context) {
        super(context);
     
    }
    
    
    
    @Override
    protected void display(ImageDisplay display) {
       pane.display(display);
    }

    @Override
    protected Pane init() {
         try {
            pane = new ImageDisplayPane(context);
            
            titleProperty().bind(pane.titleProperty());
        } catch (IOException ex) {
            logService.severe(ex);
        } 
    
    return pane;
    }
    
        /**
     *
     * SciJava Events
     *
     */
    @EventHandler
    protected void onToolChangedEvent(ToolChangeEvent event) {
        if(pane != null)
        pane.setCurrentTool(event.getTool());
    }
}
