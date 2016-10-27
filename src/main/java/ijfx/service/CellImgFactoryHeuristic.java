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

import io.scif.Metadata;
import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgFactoryHeuristic;
import io.scif.img.cell.SCIFIOCellImgFactory;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.NativeType;

/**
 *
 * @author cyril
 */
public class CellImgFactoryHeuristic implements ImgFactoryHeuristic {
    
    public CellImgFactoryHeuristic() {
        
    }
    
    @Override
    public <T extends NativeType<T>> ImgFactory<T> createFactory(Metadata mtdt, SCIFIOConfig.ImgMode[] ims, T t) throws IncompatibleTypeException {
        return new SCIFIOCellImgFactory<>(20);
    }
    
}
