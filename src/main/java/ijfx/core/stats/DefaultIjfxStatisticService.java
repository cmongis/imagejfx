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
import ijfx.service.sampler.PositionIterator;
import ijfx.service.sampler.SamplingDefinition;
import ijfx.service.sampler.SparsePositionIterator;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;
import net.imagej.axis.Axes;
import net.imagej.sampler.AxisSubrange;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.type.numeric.RealType;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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

    @Parameter
    ImageDisplayService imageDisplayService;

    @Override
    public SummaryStatistics getSummaryStatistics(Dataset dataset) {
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
            Timer t = timerService.getTimer("getStatistics(File)");
            t.start();
            Dataset dataset = datasetIoService.open(file.getAbsolutePath());
            t.elapsed("open dataset");
            SummaryStatistics stats = getSummaryStatistics(dataset);
            t.elapsed("read dataset");
            return stats;
        } catch (IOException e) {
            return new SummaryStatistics();
        }
    }

    @Override
    public DescriptiveStatistics getDatasetDescriptiveStatistics(Dataset dataset) {
        DescriptiveStatistics summary = new DescriptiveStatistics();
        Cursor<RealType<?>> cursor = dataset.cursor();
        cursor.reset();

        while (cursor.hasNext()) {
            cursor.fwd();
            double value = cursor.get().getRealDouble();
            summary.addValue(value);

        }
        return summary;
    }

    @Override
    public DescriptiveStatistics getPlaneDescriptiveStatistics(Dataset dataset, long[] position) {
        DescriptiveStatistics stats = new DescriptiveStatistics();

        double width = dataset.max(0) + 1;
        double height = dataset.max(1) + 1;

        RandomAccess<RealType<?>> randomAccess = dataset.randomAccess();
        randomAccess.setPosition(position);
        for (int x = 0; x < width; x++) {
            randomAccess.setPosition(x, 0);
            for (int y = 0; y < height; y++) {
                randomAccess.setPosition(y, 1);
                stats.addValue(randomAccess.get().getRealDouble());
            }
        }
        return stats;
    }

    
    public SummaryStatistics getChannelStatistics(Dataset dataset, int channelPosition) {
        SummaryStatistics stats = new SummaryStatistics();
        if (dataset.dimensionIndex(Axes.CHANNEL) == -1) {
            Cursor<RealType<?>> cursor = dataset.cursor();
            cursor.reset();
            while (cursor.hasNext()) {
                cursor.fwd();
                stats.addValue(cursor.get().getRealDouble());
            }
        } else {
            SamplingDefinition def = new SamplingDefinition(dataset);

            def.constrain(Axes.CHANNEL, new AxisSubrange(channelPosition));

            PositionIterator itr = new SparsePositionIterator(def);

            RandomAccess<RealType<?>> randomAccess = dataset.randomAccess();
            while (itr.hasNext()) {
                randomAccess.setPosition(itr.next());
                stats.addValue(randomAccess.get().getRealDouble());
            }
        }
        return stats;
    }

    @Override
    public <T extends RealType<T>> SummaryStatistics getSummaryStatistics(Cursor<T> cursor) {
        SummaryStatistics stats = new SummaryStatistics();
        cursor.reset();
        while (cursor.hasNext()) {
            cursor.fwd();
            stats.addValue(cursor.get().getRealDouble());
        }
        return stats;

    }

    
}
