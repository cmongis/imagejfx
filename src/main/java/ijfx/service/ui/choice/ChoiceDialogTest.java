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

import ijfx.service.ui.UIExtraService;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath="Plugins > Test > ChoiceDialog")
public class ChoiceDialogTest extends ContextCommand{
    
    @Parameter
    UIExtraService uiExtraService;
    
    @Parameter
    Context context;
    
    public void run() {
        
        
        System.out.println("Hello !");
        
        List<File> selected = uiExtraService
                .promptChoice(Stream
                        .of(new File("/Users/cyril/Pictures/").listFiles())
                        .collect(Collectors.toList())
                        )
                .setTitle("Choose one or more files")
                
                .showAndWait();
        
    }
    
    
    
}
