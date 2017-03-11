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
package ijfx.service.ui.choice;

import ijfx.core.Handles;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Choice.class, priority = Priority.VERY_LOW_PRIORITY)
@Handles(type = Object.class)
public class StringChoice implements Choice<Object> {

    private final Object object;

    public StringChoice() {
        this.object = null;
    }
    
    public StringChoice(Object string) {
        this.object = string;
    }

    public Choice<Object> create(Object obj) {
        return new StringChoice(obj);
    }

    @Override
    public String getTitle() {
        return object.toString();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public PixelRaster getPixelRaster() {
        return null;
    }

    @Override
    public Object getData() {
        return object;
    }

}
