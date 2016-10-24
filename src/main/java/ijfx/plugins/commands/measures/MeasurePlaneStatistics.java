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
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.metadata.MetaDataSetType;
import ijfx.core.stats.IjfxStatisticService;
import ijfx.plugins.commands.AxisUtils;
import ijfx.service.ImagePlaneService;
import ijfx.ui.datadisplay.metadataset.MetaDataSetDisplay;
import ijfx.ui.datadisplay.metadataset.MetaDataSetDisplayService;
import java.util.Map;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Analyze > Compute statistics > Current slice", headless = false)
public class MeasurePlaneStatistics extends ContextCommand {

    @Parameter
    IjfxStatisticService ijfxStatisticsSrv;

    @Parameter
    ImageDisplayService imageDisplaySrv;

    @Parameter
    ImagePlaneService imagePlaneService;

    @Parameter(type = ItemIO.BOTH)
    ImageDisplay imageDisplay;

    @Parameter
    UIService uiService;

    @Parameter
    MetaDataSetDisplayService metaDataSetDisplaySrv;

    @Parameter
    MetaDataExtractionService metadataService;

    public void run() {

        long[] position = new long[imageDisplay.numDimensions()];
        imageDisplay.localize(position);
        //position = DimensionUtils.planarToNonPlanar(position);

        DescriptiveStatistics planeDescriptiveStatistics = ijfxStatisticsSrv.getPlaneDescriptiveStatistics(imageDisplaySrv.getActiveDataset(imageDisplay), position);

        Map<String, Double> statsMap = ijfxStatisticsSrv.descriptiveStatisticsToMap(planeDescriptiveStatistics);

        MetaDataSet metaDataSet = new MetaDataSet();
        metaDataSet.merge(statsMap);
        CalibratedAxis[] axes = AxisUtils.getAxes(imageDisplaySrv.getActiveDataset());

        metadataService.fillPositionMetaData(metaDataSet, axes, position);
        metaDataSet.putGeneric(MetaData.NAME, imageDisplay.getName());
        metaDataSet.setType(MetaDataSetType.PLANE);
        metaDataSetDisplaySrv.addMetaDataSetToDisplay(metaDataSet, MetaDataSetDisplay.PLANE_MEASURES);

    }

}
