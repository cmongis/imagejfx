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
import ijfx.service.batch.FileBatchInput;
import ijfx.service.batch.ImageDisplayBatchInput;
import ijfx.service.batch.SilentImageDisplay;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.utils.NamingUtils;
import java.io.File;
import java.util.function.Consumer;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import org.scijava.Context;

/**
 * Work in progress, allow to build any BatchSingleInput from any situation. Uses the Builder and Decorator Pattern.
 *
 * @author cyril
 */
public class BatchInputBuilder {

    //@Parameter
    Context context;

    BatchSingleInput input;

    private static String suffixSeparator = "_";
    
    
    
    public BatchInputBuilder(Context context) {
        this.context = context;
    }
    
    public BatchInputBuilder wrap(BatchSingleInput input) {
        this.input = input;
        return this;
    }
    
    public void tryInject(Object object) {
        try {
            context.inject(object);
        }
        catch(Exception e) {
            
        }
    }
    
    public BatchInputBuilder from(File file) {
        input = new FileBatchInputLoader(file);
        context.inject(input);
        return this;
    }
    
    public BatchInputBuilder from(ImageDisplay imageDisplay) {
        input = new ImageDisplayBatchInput(imageDisplay, false);
        
        return this;
    }

    public BatchInputBuilder from(Dataset dataset) {
        
        NaiveBatchInput naiveInput = new NaiveBatchInput();
        naiveInput.setDataset(dataset);
        naiveInput.setSourceFile(dataset.getSource());
        naiveInput.setDisplay(new SilentImageDisplay(context, dataset));
        input = naiveInput;
        return this;
    }
    
    public BatchInputBuilder from(Dataset dataset, long[] planePosition) {
        input = new DatasetPlaneWrapper(context, dataset, planePosition);
        return this;
    }

    public BatchInputBuilder from(Explorable holder) {
        input = new ExplorableBatchInputWrapper(holder);
        tryInject(input);
        return this;
    }

    public BatchInputBuilder saveTo(File file) {
        input = new SaveToFileWrapper(context, input, file);
        return this;
    }
    
    public BatchInputBuilder display() {
        input = new DisplayDatasetWrapper(context, input);
        return this;
    }
    
   
    
    public BatchInputBuilder overwriteOriginal() {
        input = new ReplaceOriginalFileSaver(context,input);
        return this;
    }
    
    public BatchInputBuilder onFinished(Consumer<BatchSingleInput> action) {
        input = new ConsumerBatchInputWrapper(input, action);
        return this;
    }
    
    
    public BatchInputBuilder saveIn(File directory) {
        input =  new SaveToFileWrapper(context,input, new File(directory,input.getName()));
        return this;
    }
    
    public BatchInputBuilder saveIn(File directory, String suffix) {
        input = new SaveToFileWrapper(context,input,new File(directory,input.getName()),suffix);
        return this;
    }
    
   public BatchInputBuilder saveNextToSourceWithPrefix(String suffix) {
       input = new SaveToFileWrapper(context, input, new File(input.getSourceFile()),suffix);
       return this;
   }
   public BatchInputBuilder saveNextToSourceWithPrefix(String suffix, String extension) {
       if(extension.startsWith(".") == false) extension = new StringBuilder().append(".").append(extension).toString();
       File f = NamingUtils.replaceWithExtension(new File(input.getSourceFile()),extension);
       input = new SaveToFileWrapper(context,input,f,suffix);
               return this;
   }
    public BatchSingleInput getInput() {
        return input;
    }

}
