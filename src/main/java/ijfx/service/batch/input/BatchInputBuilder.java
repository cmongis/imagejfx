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
package ijfx.service.batch.input;

import ijfx.service.batch.BatchSingleInput;
import ijfx.ui.explorer.DatasetHolder;
import java.io.File;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 * Work in progress, allow to build any BatchSingleInput from any situation. Uses the Builder and Decorator Pattern.
 *
 * @author cyril
 */
public class BatchInputBuilder {

    @Parameter
    Context context;

    BatchSingleInput input;

    public BatchInputBuilder getFrom(ImageDisplay imageDisplay) {

        NaiveBatchInput input = new NaiveBatchInput();
        input.setDisplay(imageDisplay);

        return this;
    }

    public BatchInputBuilder getFrom(Dataset dataset) {
        return this;
    }

    public BatchInputBuilder getFrom(DatasetHolder holder) {
        return this;
    }

    public BatchInputBuilder saveTo(File file) {
        return this;
    }

}
