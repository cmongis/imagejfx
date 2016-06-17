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
package ijfx.service.dataset;

import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;
import net.imglib2.type.numeric.RealType;

/**
 *
 * @author Tuan anh TRINH
 */
public interface DatasetUtillsService extends ImageJService{
    
    public Dataset extractPlane(ImageDisplay imageDisplay);
    
    public ImageDisplay getImageDisplay(Dataset dataset);
    
    public  < T extends RealType< T>> Dataset divideDatasetByDataset(Dataset numerator, Dataset denominator);
    
    public Dataset divideDatasetByValue(Dataset dataset, double value);
    
}
