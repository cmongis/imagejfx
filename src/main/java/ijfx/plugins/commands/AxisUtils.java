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
package ijfx.plugins.commands;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class AxisUtils {

    public static CalibratedAxis[] getAxes(Dataset dataset) {
        CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
        dataset.axes(axes);
        return axes;
    }
    
    
    public static AxisType getSliceAxis(Dataset dataset) {
        AxisType axisType = null;
        if (hasAxisType(dataset, Axes.Z)) {
            axisType = Axes.Z;
        }
        if (axisType == null && hasAxisType(dataset, Axes.TIME)) {
            axisType = Axes.TIME;
        }
        return axisType;
    }

    public static AxisType getSliceAxis(ImageDisplay display) {
        AxisType axisType = null;
        if (hasAxisType(display, Axes.Z)) {
            axisType = Axes.Z;
        }
        if (axisType == null && hasAxisType(display, Axes.TIME)) {
            axisType = Axes.TIME;
        }
        return axisType;
    }

    public static CalibratedAxis getAxis(Dataset dataset, AxisType type) {
        CalibratedAxis[] calibratedAxes = new CalibratedAxis[dataset.numDimensions()];
        dataset.axes(calibratedAxes);

        for (CalibratedAxis a : calibratedAxes) {
            if (a.type().equals(type)) {
                return a;
            }
        }
        return null;

    }
    
    public static CalibratedAxis[] getAxes(ImgPlus img) {
        CalibratedAxis[] axes = new CalibratedAxis[img.numDimensions()];
        img.axes(axes);
        return axes;
    }
    
    public static long[] getDimensions(ImgPlus img) {
        long[] dims = new long[img.numDimensions()];
        img.dimensions(dims);
        return dims;
    }
    

    public static long getAxisMax(Dataset dataset, AxisType axisType) {
        int d = dataset.dimensionIndex(axisType);
        if(d == -1) return 0;
        return dataset.max(d);
    }

    public static long getChannelNumber(Dataset dataset) {
        return getAxisMax(dataset, Axes.CHANNEL) + 1;
    }

    public static boolean hasAxisType(ImageDisplay display, AxisType axisType) {
        CalibratedAxis[] calibratedAxises = new CalibratedAxis[display.numDimensions()];
        display.axes(calibratedAxises);
        for (CalibratedAxis calibratedAxis : calibratedAxises) {
            if (calibratedAxis.type() == axisType) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAxisType(Dataset dataset, AxisType axisType) {
        CalibratedAxis[] calibratedAxises = new CalibratedAxis[dataset.numDimensions()];
        dataset.axes(calibratedAxises);
        for (CalibratedAxis calibratedAxis : calibratedAxises) {
            if (calibratedAxis.type() == axisType) {
                return true;
            }
        }
        return false;
    }

    public static long calcNumPlanes(long[] dims, AxisType[] axes) {
        long num = 1;
        for (int i = 0; i < dims.length; i++) {
            AxisType type = axes[i];
            if (type == Axes.X || type == Axes.Y) {
                continue;
            }
            num *= dims[i];
        }
        return num;
    }

    /**
     * Calculates a plane number from a position within a dimensional space.
     */
    public static int planeNum(final long[] dims, final long[] pos) {
        int plane = 0;
        int inc = 1;
        // TODO - assumes X & Y are 1st two dims
        for (int i = 2; i < dims.length; i++) {
            plane += pos[i] * inc;
            inc *= dims[i];
        }
        return plane;
    }

}
