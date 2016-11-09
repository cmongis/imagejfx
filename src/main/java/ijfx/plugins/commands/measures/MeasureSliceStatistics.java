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
package ijfx.plugins.commands.measures;

import ijfx.core.imagedb.MetaDataExtractionService;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.metadata.MetaDataSetType;
import ijfx.core.stats.DefaultIjfxStatisticService;
import ijfx.core.utils.DimensionUtils;
import ijfx.plugins.commands.AxisUtils;
import ijfx.service.ImagePlaneService;
import ijfx.ui.datadisplay.metadataset.MetaDataSetDisplayService;
import java.util.ArrayList;
import java.util.List;
import net.imagej.Dataset;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Analyze > Compute statistics > Each slice")
public class MeasureSliceStatistics extends ContextCommand {

    @Parameter(type = ItemIO.INPUT)
    Dataset dataset;

    @Parameter
    ImagePlaneService imagePlaneService;

    @Parameter
    DefaultIjfxStatisticService statsService;

    @Parameter
    MetaDataExtractionService metaDataService;

    @Parameter
    MetaDataSetDisplayService metaDataDisplayService;
    
    @Override
    public void run() {
        if (isCanceled()) {
            return;
        }

        List<MetaDataSet> result = new ArrayList<>();

        long[] dimension = DimensionUtils.getDimension(dataset);
        long[][] planePossibilities = DimensionUtils.allNonPlanarPossibilities(dimension);
        CalibratedAxis[] axes = AxisUtils.getAxes(dataset);
        if (dimension.length == 0) {
            final DescriptiveStatistics stats = statsService.getDatasetDescriptiveStatistics(dataset);
            final MetaDataSet set = metaDataService.extractMetaData(dataset);
            set.setType(MetaDataSetType.PLANE);

            set.merge(statsService.descriptiveStatisticsToMap(stats));
            result.add(set);

        } else {

            for (long[] position : planePossibilities) {

                final MetaDataSet set = metaDataService.extractMetaData(dataset);
                final IntervalView plane = imagePlaneService.plane((RandomAccessibleInterval) dataset, position);
                final DescriptiveStatistics stats = statsService.getDescriptiveStatistics(plane);
                
                metaDataService.fillPositionMetaData(set, axes, DimensionUtils.planarToAbsolute(position));
                set.setType(MetaDataSetType.PLANE);
                set.merge(statsService.descriptiveStatisticsToMap(stats));
                result.add(set);
            }
        }

        String displayName = dataset.getName();

        metaDataDisplayService.findDisplay(String.format("Measure per plane for %s", displayName)).addAll(result);
    
    }

}
