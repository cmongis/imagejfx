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
package ijfx.plugins.commands;

import net.imagej.display.ImageDisplay;
import net.imagej.lut.LUTService;
import net.imglib2.display.ColorTable;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
*/
@Plugin(type = Command.class,label = "Apply an LUT",headless = true)
public class ApplyLUT implements Command {

    @Parameter
    LUTService lutService;
    
    
    @Parameter(label = "Choose an LUT",type = ItemIO.BOTH)
    ColorTable colorTable;
    
    @Parameter(type = ItemIO.BOTH)
    ImageDisplay imageDisplay;
    
  
    @Override
    public void run() {
       
        lutService.applyLUT(colorTable, imageDisplay);
    }
    

}
