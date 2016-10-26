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
package ijfx.ui.main;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.Cursor;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Command.class, menuPath=" Plugins > Test > Open/Saving")
public class ImageJTest extends ContextCommand{

   
    @Parameter
    DatasetIOService datasetIOService;

    @Parameter
    DatasetService datasetService;

    @Parameter
    UIService uiService;
   
    public void go() throws Exception {

        AxisType[] axes = {Axes.X, Axes.Y, Axes.CHANNEL, Axes.TIME, Axes.get("Series")};
        
        
        /*
        Dataset created = datasetService.create(new UnsignedShortType(), new long[]{200, 200, 4, 2, 5}, "some name", axes);

        Cursor<RealType<?>> cursor = created.cursor();

        cursor.reset();
        long[] position = new long[5];
        while (cursor.hasNext()) {
            cursor.fwd();
            cursor.localize(position);
            cursor.get().setReal(getPositionId(position));
        }*/
        

        String pathToLoad = "/Users/cyril/test_img/daniel/nd_file_read.nd2";
        String pathToSave = "/Users/cyril/test_img/daniel/nd_file_read.tif";
        
        
        Dataset created = datasetIOService.open(pathToLoad);
        Cursor<RealType<?>> cursor = created.cursor();
        
        created.setCompositeChannelCount(5);
        created.update();
        created.setDirty(false);
        for(int i = 0;i!=5;i++)
        created.setChannelMinimum(i, 200);
        datasetIOService.save(created,pathToSave);
        
        
        Dataset reloaded = datasetIOService.open(pathToSave);
        
        cursor.reset();
        Cursor<RealType<?>> loadedCursor = reloaded.cursor();
        loadedCursor.reset();
        while (cursor.hasNext()) {
            cursor.fwd();
            loadedCursor.fwd();
            
            if(cursor.get().getRealDouble() != loadedCursor.get().getRealDouble()) {
               uiService.showDialog("Haha ! It was different !");
               break;
            }
            
        }
        uiService.showDialog("It did worked...");
      
    }

    public long getPositionId(long[] position) {

        long result = 0;
        long m = 0;
        for (long i : position) {
            
                result += (i * m++);
            
        }

        return result;

    }

    @Override
    public void run() {
        try {
        go();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
