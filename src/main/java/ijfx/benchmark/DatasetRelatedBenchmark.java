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
package ijfx.benchmark;

import io.scif.services.DatasetIOService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public abstract class DatasetRelatedBenchmark implements BenchMarkPlugin{
    Dataset dataset;
    
    @Parameter
    DatasetIOService datasetIOService;
    
    @Override
    public void init() {
        try {
            dataset = datasetIOService.open("src/test/resources/multidim.tif");
        } catch (IOException ex) {
            Logger.getLogger(PixelAccessBenchmark.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void finish() {
        dataset = null;
    }
}
