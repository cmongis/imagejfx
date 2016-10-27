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
package ijfx.core.logger;

import ijfx.ui.main.ImageJFX;
import java.util.logging.Level;

/**
 *
 * @author cyril
 */
public class IJFXLogger implements Logger{

   java.util.logging.Logger logger = ImageJFX.getLogger();
    
    @Override
    public void info(Throwable throwable, String text, Object... params) {
         logger.log(Level.INFO,String.format(text,params));
    }

    @Override
    public void severe(Throwable throwable, String text, Object... params) {
         logger.log(Level.INFO,String.format(text,params));
    }

    @Override
    public void warning(Throwable throwable, String text, Object... params) {
         logger.log(Level.INFO,String.format(text,params));
    }
    
}
