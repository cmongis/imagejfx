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
package ijfx.core.stats;

import ijfx.service.Timer;
import ijfx.service.TimerService;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import net.imagej.Dataset;
import net.imglib2.Cursor;
import net.imglib2.type.numeric.RealType;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class DefaultIjfxStatisticService extends AbstractService implements IjfxStatisticService {

    @Parameter
    TimerService timerService;

    @Parameter
    DatasetIOService datasetIoService;

    
    
    @Override
    public SummaryStatistics getDatasetStatistics(Dataset dataset) {
        SummaryStatistics summary = new SummaryStatistics();
        Cursor<RealType<?>> cursor = dataset.cursor();
        cursor.reset();
        
        while (cursor.hasNext()) {
            cursor.fwd();
            double value = cursor.get().getRealDouble();
            summary.addValue(value);
           
        }
        return summary;
    }

    public SummaryStatistics getStatistics(File file) {
        try {
            Timer t  = timerService.getTimer("getStatistics(File)");   
            t.start();
            Dataset dataset = datasetIoService.open(file.getAbsolutePath());
            t.elapsed("open dataset");
            SummaryStatistics stats = getDatasetStatistics(dataset);
            t.elapsed("read dataset");
            return stats;
        } catch (IOException e) {
            return new SummaryStatistics();
        }
    }

}
