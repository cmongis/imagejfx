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
package ijfx.plugins.stackconverter;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Plugins>Hyperstack to Stack", attrs = {@Attr(name = "no-legacy")})
public class HyperstackToStack implements Command{
    @Parameter
    UIService uIService;
    @Parameter(type = ItemIO.INPUT)
    protected Dataset dataset;

    @Parameter(type = ItemIO.OUTPUT)
    protected Dataset datasetOutput;

    @Parameter
    private DatasetService datasetService;
    @Override
    public void run() {
        AxisType[] axisType = new AxisType[3];
        axisType[0] = Axes.X;
        axisType[1] = Axes.Y;
        axisType[2] = Axes.TIME;
        long[] dims = new long[axisType.length];
        dims[0] = dataset.max(0) + 1;
        dims[1] = dataset.max(1) + 1;
        long nSlices = 1;
        for (int i =2; i < dataset.numDimensions(); i++)
        {
            nSlices = nSlices *(dataset.max(i) + 1);
        }
        dims[2] = nSlices;
        datasetOutput = datasetService.create(dims, dataset.getName(), axisType, dataset.getValidBits(), dataset.isSigned(), false);
        HyperStackConverter.copyDataset(dataset, datasetOutput, new MultiToOneDimension());
    }
    
        
    
}
