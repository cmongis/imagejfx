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
package ijfx.ui.explorer.smartaction;

import ijfx.core.metadata.MetaData;
import ijfx.ui.explorer.Folder;
import net.imagej.measure.MeasurementService;
import net.imagej.measure.StatisticsService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class GetImageStatsAction implements FolderSmartAction {

    @Parameter
    MeasurementService measurementService;
    
    @Parameter
    StatisticsService statisticsService;
    
    @Override
    public boolean check(Folder folder) {
        long itemWithoutStats = folder
                .getFileList()
                .stream()
                .map(explorable -> explorable.getMetaDataSet())
                .filter(m -> m.containsKey(MetaData.STATS_PIXEL_MIN) == false)
                .count();
        return itemWithoutStats > 0;
    }

    @Override
    public String getActionQuestion() {
        return "We can compute statistics about your images so you can filter them easier. Do you want to get such statistics ?";
    }

    @Override
    public String getActionButtonLabel() {
        return "compute image statistics";
    }

    @Override
    public void apply(Folder t) {
        
        
        
    }

}
