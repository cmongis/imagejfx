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
package ijfx.ui.plugins;

import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.explorer.view.IconView;
import ijfx.ui.main.Localization;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id="same-folder-button",context="imagej image-open",localization=Localization.TOP_LEFT)
public class SameFolderButton extends ToggleButton implements UiPlugin{

    @Parameter
            Context context;
    
    IconView iconView = new IconView();

    @Parameter
    ImageDisplayService imageDisplayService;
    
    
    int radius = 2;
    
    public SameFolderButton() {
        iconView.setPrefWidth(300);
        iconView.setPrefHeight(300);
        
        
    }
    
    
    private void update() {
        
        Dataset dataset = imageDisplayService.getActiveDataset();
        
        File file = new File(dataset.getSource());

        List<File> toShow = new ArrayList<>();
        
        if(file.exists()) {
            
            File parent = file.getParentFile();
            
            List<File> files = Stream
                    .of(parent.listFiles())
                    .sorted()
                    .collect(Collectors.toList());
            
            if(files.size() > radius * 2) {
                int filePosition = indexOf(files,file);
                int maxIndex = files.size() - 1;
                int minIndex = 0;
                int start = filePosition - radius;
                if(filePosition - radius < minIndex) {
                    start = minIndex;
                }
                if(filePosition + radius > maxIndex) {
                    start = maxIndex - (radius * 2) - 1;
                }
                
                
                for(int i = start; i!= start+(radius*2)+1;i++) {
                    
                    if(files.get(i) != file) toShow.add(files.get(i));
                    
                    
                }
                
                
            }
            else {
                //Stream.of(parent.getF
            }
            
            
        }
        
    }
    
    private <T> int indexOf(List<T> list, T t) {
        
        for(int i = 0;i!=list.size();i++) {
            
            if(t.equals(list.get(i))) return i;
        }
        return -1;
        
    }
    
    @Override
    public Node getUiElement() {
       return this;
    }

    @Override
    public UiPlugin init() {
       return this;
    }
    
}
