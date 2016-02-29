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
package ijfx.core.project;

import javafx.application.Application;
import net.imagej.ImageJ;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */


public class BasicProjectOperationTest {
    
    
    public static ImageJ imagej;
    
    
    @Parameter
    ProjectManagerService projectService;
    
    @Parameter
    ProjectIoService projectIOService;
    
    public static ImageJ getImageJ() {
        if(imagej == null) {
            Context context = new Context();
            imagej = new ImageJ(context);
        }
        return imagej;
    }
    
    
    public void init() {
        
        
        getImageJ().getContext().inject(this);
    }
    
    @Test
    public void testProjectSettings() {
        
      //  init();
        
        System.out.println(projectService);
        
        
    }
    
    
}
