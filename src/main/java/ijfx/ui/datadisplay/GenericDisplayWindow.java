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
package ijfx.ui.datadisplay;

import ijfx.service.PluginUtilsService;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.scijava.Context;
import org.scijava.display.Display;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class GenericDisplayWindow extends AbstractDisplayWindow{

    @Parameter
    PluginUtilsService pluginUtilsService;
    
    DisplayPanePlugin displayPane;
    
    public GenericDisplayWindow(Context context) {
        super(context);
    }

    
    @Override
    protected void display(Display<?> display) {
        
        displayPane = pluginUtilsService.createHandler(DisplayPanePlugin.class, display.getClass());
        
        if(displayPane == null) {
            setContentPane(new AnchorPane(new Label("No pane plugin for this type of display.")));
        }
        else {
            
            setContentPane(displayPane.getPane());
            displayPane.display(display);
            
            Platform.runLater(()->{
            
                displayPane.titleProperty().setValue(display.getName());
                titleProperty().bind(displayPane.titleProperty());
            
            });
            
        }   
    }
}
