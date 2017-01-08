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
package ijfx.plugins;

import ijfx.plugins.commands.MergeStacks;
import ijfx.service.ui.CommandRunner;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.imagej.Dataset;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */

@Plugin(type = Command.class,menuPath = "Plugins > KnopLab > Prepare dataset")
public class GroupFiles extends ContextCommand{
    
    
    @Parameter(label = "Data directory",style = "save folder")
    File inputDirectory;

    @Parameter(label = "Saves in ",style = "save folder")
    File outputDirectory;
    
    @Parameter
    DatasetIOService datasetIOService;
    
    @Parameter
    Context context;
    
    @Parameter
    StatusService statusService;
    
    private int totalFiles;
    
    private int treated = 0;
    
    @Override
    public void run() {
      
        
        
        int totalFiles = inputDirectory.listFiles().length;
        final LongAdder count = new LongAdder();
        Stream
                .of(inputDirectory.listFiles())
                .filter(f->f.getName().endsWith("DIB"))
                
                .collect(Collectors.groupingBy(this::classifyFile))
                .forEach(this::mergeAndSave);
                
                
        
        
        
        
        
    }
    
    Pattern filePattern = Pattern.compile("(\\d+)\\-\\w+\\.DIB");
    
    public String classifyFile(File file) {
        Matcher m = filePattern.matcher(file.getName());
        if(m.find()) {
            return m.group(1);
        }
        else {
            return null;
        }
    }
    
    public void mergeAndSave(String key, List<File> files) {
        
        try {
            Dataset dataset = merge(files);
            datasetIOService.save(dataset, new File(outputDirectory,String.format("%s.tif",key)).getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(GroupFiles.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        
        
    }
    
    private void logLoading(File file) {
        statusService.showStatus(treated++, totalFiles, String.format("Loading %s...",file.getName()));
    }
    
    public Dataset merge(List<File> files) {
        
        Dataset[] datasets = files
                .stream()
                .peek(this::logLoading)
                .map(f->{
                    try {
                       
                        
                        return datasetIOService.open(f.getAbsolutePath());
                    }
                    catch(Exception e) {
                        return null;
                    }
                 })
                .toArray(s->new Dataset[s]);
        
       return new CommandRunner(context)
                .set("inputs", datasets)
                .runSync(MergeStacks.class)
                .getOutput("output");
        
    }
    
    
    
}
