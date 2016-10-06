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
package ijfx.plugins.convertype;


import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.display.ColorTables;
import net.imagej.space.SpaceUtils;
import net.imagej.types.BigComplex;
import net.imagej.types.DataType;
import net.imagej.types.DataTypeService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import org.scijava.ItemIO;

import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
/**
 * TypeChanger changes the type of the data in a {@link Dataset}. The
 * {@link DataType} of the data is chosen from the types discovered at runtime
 * by the {@link DataTypeService}. Channels can be combined in the process if
 * desired. Combination is done via channel averaging. After conversion data
 * values are preserved as much as possible but do get clamped to the new data
 * type's valid range.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class)
public class TypeChangerIJFX<U extends RealType<U>, V extends RealType<V> & NativeType<V>>
	extends ContextCommand
{

	// TODO: expects types to be based on RealType and sometimes NativeType. The
	// as yet to be used unbounded types defined in the data types package don't
	// support NativeType. At some point we need to relax these constraints such
	// that U and V just extend Type<U> and Type<V>. The DatasetService must be
	// able to make Datasets that have this kind of signature:
	// ImgPlus<? extends Type<?>>. And the Img opening/saving routines also need
	// to be able to encode arbitrary types.

	// -- Parameters --

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private DataTypeService dataTypeService;

	@Parameter(type = ItemIO.BOTH)
	private Dataset data;

	@Parameter(label = "Type", persist = false)
	protected String typeName;

	@Parameter(label = "Combine channels", persist = false)
	private boolean combineChannels;

	// -- Command methods --

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Class<?> typeClass = data.getImgPlus().firstElement().getClass();
		DataType<U> inType =
			(DataType<U>) dataTypeService.getTypeByClass(typeClass);
		DataType<V> outType = (DataType<V>) dataTypeService.getTypeByName(typeName);
		int chAxis = data.dimensionIndex(Axes.CHANNEL);
		long channelCount = (chAxis < 0) ? 1 : data.dimension(chAxis);
		Dataset newData;
		if (combineChannels && channelCount > 1 &&
			channelCount <= Integer.MAX_VALUE)
		{
			newData =
				channelAveragingCase(inType, outType, chAxis, (int) channelCount);
		}
		else { // straight 1 for 1 pixel casting
			newData = channelPreservingCase(inType, outType);
		}
		data.setImgPlus(newData.getImgPlus());
		data.setRGBMerged(false); // we never end up with RGB merged data
	}



	// -- helpers --

	@SuppressWarnings("unchecked")
	private Dataset channelAveragingCase(DataType<U> inType, DataType<V> outType,
		int chAxis, int count)
	{
		BigComplex[] temps = new BigComplex[count];
		for (int i = 0; i < count; i++) {
			temps[i] = new BigComplex();
		}
		BigComplex combined = new BigComplex();
		BigComplex divisor = new BigComplex(count, 0);
		long[] dims = calcDims(Intervals.dimensionsAsLongArray(data), chAxis);
		AxisType[] axes = calcAxes(SpaceUtils.getAxisTypes(data), chAxis);
		Dataset newData =
			datasetService.create(outType.createVariable(), dims, "Converted Image",
				axes);
		long[] span = Intervals.dimensionsAsLongArray(data).clone();
		span[chAxis] = 1;
		PointSet combinedSpace = new HyperVolumePointSet(span);
		PointSetIterator iter = combinedSpace.iterator();
		RandomAccess<U> inAccessor =
			(RandomAccess<U>) data.getImgPlus().randomAccess();
		RandomAccess<V> outAccessor =
			(RandomAccess<V>) newData.getImgPlus().randomAccess();
		while (iter.hasNext()) {
			long[] pos = iter.next();
			inAccessor.setPosition(pos);
			for (int i = 0; i < count; i++) {
				inAccessor.setPosition(i, chAxis);
				inType.cast(inAccessor.get(), temps[i]);
			}
			combined.setZero();
			for (int i = 0; i < count; i++) {
				combined.add(temps[i]);
			}
			int d = 0;
			for (int i = 0; i < count; i++) {
				if (i == chAxis) continue;
				outAccessor.setPosition(pos[i], d++);
			}
			combined.div(divisor);
			outType.cast(combined, outAccessor.get());
		}
		copyMetaDataChannelsCase(data.getImgPlus(), newData.getImgPlus());
		return newData;
	}

	private Dataset
		channelPreservingCase(DataType<U> inType, DataType<V> outType)
	{
		Dataset newData =
			datasetService.create(outType.createVariable(), Intervals
				.dimensionsAsLongArray(data), "Converted Image", SpaceUtils
				.getAxisTypes(data));
		Cursor<U> inCursor = (Cursor<U>) data.getImgPlus().cursor();
		RandomAccess<V> outAccessor =
			(RandomAccess<V>) newData.getImgPlus().randomAccess();
		BigComplex tmp = new BigComplex();
		while (inCursor.hasNext()) {
			inCursor.fwd();
			outAccessor.setPosition(inCursor);
			dataTypeService.cast(inType, inCursor.get(), outType, outAccessor.get(),
				tmp);
		}
		copyMetaDataDefaultCase(data.getImgPlus(), newData.getImgPlus());
		return newData;
	}

	private void copyMetaDataDefaultCase(ImgPlus<?> src, ImgPlus<?> dest) {

		// dims and axes already correct

		// name
		dest.setName(src.getName());

		// color tables
		int tableCount = src.getColorTableCount();
		dest.initializeColorTables(tableCount);
		for (int i = 0; i < tableCount; i++) {
			dest.setColorTable(src.getColorTable(i), i);
		}
		
		// channel min/maxes
		int chAxis = src.dimensionIndex(Axes.CHANNEL);
		int channels;
		if (chAxis < 0) channels = 1;
		else channels = (int) src.dimension(chAxis);
		for (int i = 0; i < channels; i++) {
			double min = src.getChannelMinimum(i);
			double max = src.getChannelMaximum(i);
			dest.setChannelMinimum(i, min);
			dest.setChannelMaximum(i, max);
		}

		// dimensional axes
		for (int d = 0; d < src.numDimensions(); d++) {
			dest.setAxis(src.axis(d).copy(), d);
		}
	}

	private void copyMetaDataChannelsCase(ImgPlus<?> src, ImgPlus<?> dest) {

		int chAxis = src.dimensionIndex(Axes.CHANNEL);

		// dims and axes already correct

		// name
		dest.setName(src.getName());

		// color tables
		// ACK what is best here?
		int tableCount = (int) calcTableCount(src, chAxis);
		dest.initializeColorTables(tableCount);
		for (int i = 0; i < tableCount; i++) {
			dest.setColorTable(ColorTables.GRAYS, i);
		}

		// channel min/maxes
		double min = src.getChannelMinimum(0);
		double max = src.getChannelMaximum(0);
		int channels;
		if (chAxis < 0) channels = 1;
		else channels = (int) src.dimension(chAxis);
		for (int i = 1; i < channels; i++) {
			min = Math.min(min, src.getChannelMinimum(i));
			max = Math.max(max, src.getChannelMaximum(i));
		}
		dest.setChannelMinimum(0, min);
		dest.setChannelMaximum(0, max);

		// dimensional axes
		int dDest = 0;
		for (int dSrc = 0; dSrc < src.numDimensions(); dSrc++) {
			if (dSrc == chAxis) continue;
			dest.setAxis(src.axis(dSrc).copy(), dDest++);
		}
	}

	private long[] calcDims(long[] dims, int chAxis) {
		long[] outputDims = new long[dims.length - 1];
		int d = 0;
		for (int i = 0; i < dims.length; i++) {
			if (i == chAxis) continue;
			outputDims[d++] = dims[i];
		}
		return outputDims;
	}

	private AxisType[] calcAxes(AxisType[] axes, int chAxis) {
		AxisType[] outputAxes = new AxisType[axes.length - 1];
		int d = 0;
		for (int i = 0; i < axes.length; i++) {
			if (i == chAxis) continue;
			outputAxes[d++] = axes[i];
		}
		return outputAxes;
	}

	private long calcTableCount(ImgPlus<?> src, int chAxis) {
		long count = 1;
		for (int i = 0; i < src.numDimensions(); i++) {
			if (i == chAxis) continue;
			count *= src.dimension(i);
		}
		return count;
	}
}
