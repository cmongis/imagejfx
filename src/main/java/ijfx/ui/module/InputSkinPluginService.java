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
package ijfx.ui.module;

import ijfx.ui.module.input.InputSkinFactory;
import net.imagej.ImageJService;
import org.scijava.module.Module;
import org.scijava.service.Service;

/**
 * The InputSkinPluginService takes care of loading and create the right skin for the right type of input.
 * It also provides useful like which input inside a module can be handled.
 *  
 * @author Cyril MONGIS, 2015
 */

public interface InputSkinPluginService extends Service,ImageJService,InputSkinFactory{
    
    
    
    
    public int getHandledInputs(Module module);
    
}
