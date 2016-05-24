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
package ijfx.plugins;

import ij.ImagePlus;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Plugin;
/**
 * 
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Plugins>DefaultWholeAdapter", attrs = {
    @Attr(name = "no-legacy")})
public class DefaultWholeWrapper extends AbstractImageJ1PluginAdapter {

    @Override
    public ImagePlus processImagePlus(ImagePlus input) {
        return input;
    }

    @Override
    public void run() {
        dataset = processDatasetWholeWrap(dataset);
    }

}
