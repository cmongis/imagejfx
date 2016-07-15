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
package ijfx.service;

import java.io.File;
import java.io.IOException;
import net.imagej.Dataset;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;

/**
 *
 * @author cyril
 */
public interface ImagePlaneService extends IjfxService {

    <T extends RealType<T>> Dataset extractPlane(File file, long[] nonPlanarPosition) throws IOException;

    Dataset extractPlane(File file, int planeIndex) throws IOException;

    <T extends RealType<T>> Dataset isolatePlane(Dataset dataset, long[] position);
    
    Dataset createEmptyPlaneDataset(Dataset input);
    Dataset createEmptyPlaneDataset(Dataset input, long with, long height);
    Dataset openVirtualDataset(File file) throws IOException;
    
    
    
    public <T extends RealType<T>> IntervalView<T> planeView(Dataset ra,long[] position);
    
    public <T extends RealType<T>> RandomAccessibleInterval<T> openVirtualPlane(File file, long[] nonSpacialPosition) throws IOException;
    
    
}
