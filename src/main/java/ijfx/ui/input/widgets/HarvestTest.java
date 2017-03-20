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
package ijfx.ui.input.widgets;

import java.io.File;
import javafx.scene.control.Button;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;
import org.scijava.widget.NumberWidget;
import org.scijava.widget.TextWidget;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class,menuPath = "Plugins > Test > Input harvesting")
public class HarvestTest extends ContextCommand{

    @Parameter
    UIService uiService;
    
    @Parameter(label="Click me",callback="onClick",required = false)
    Button button;
    
    @Parameter(label="Increment",callback="increment",required = false)
    Button button2;
    
    @Parameter(label = "Some text")
    String text;
    
    @Parameter(label = "More text",style = TextWidget.AREA_STYLE)
    String moreText;
    
    @Parameter(label = "Integer",min = "2",max = "10",stepSize = "2")
    int number = 4;
    
    @Parameter(label = "Double",min = "-1.5",max = "10",stepSize = "0.5",style=NumberWidget.SLIDER_STYLE)
    double slidedNumber = 2.5;
    
    @Parameter(label = "Long",min="-10000",max="20000",stepSize="1000")
    long aLong = 7000;
    
    @Parameter(label = "Some file to open")
    File file;
    
    @Override
    public void run() {
      
    }
    
    public void increment() {
        number++;
        
        System.out.println(number);
        
        
    }
    
    public void onClick() {
        
        uiService.showDialog("They clicked me !", DialogPrompt.MessageType.INFORMATION_MESSAGE);
        number = number+20;
    }
    
    
    
}
