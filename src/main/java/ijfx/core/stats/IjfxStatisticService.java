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

import ijfx.service.IjfxService;
import java.io.File;
import java.util.Map;
import javafx.util.Pair;
import net.imagej.Dataset;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public interface IjfxStatisticService extends IjfxService{
    
    
    public SummaryStatistics getSummaryStatistics(Dataset dataset);
    public <T extends RealType<T>> SummaryStatistics getSummaryStatistics(Cursor<T> rai);
    public DescriptiveStatistics getDatasetDescriptiveStatistics(Dataset dataset);
    public DescriptiveStatistics getPlaneDescriptiveStatistics(Dataset dataset, long[]position);
    public SummaryStatistics getStatistics(File file);
    public SummaryStatistics getChannelStatistics(Dataset dataset, int channelPosition);
    public <T extends RealType<T>> Double[] getValues(RandomAccessibleInterval<T> inteverval);
    public Map<String,Double> summaryStatisticsToMap(StatisticalSummary stats);
    public Map<String,Double> descriptiveStatisticsToMap(DescriptiveStatistics descriptiveStats);
    
    public double[] getChannelMinMax(Dataset dataset, int channelPosition);
    
    public  <T extends RealType<T>> double[] getMinMax(RandomAccessibleInterval<T> interval);
    
}
