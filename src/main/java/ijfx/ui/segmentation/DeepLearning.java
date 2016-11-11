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
package ijfx.ui.segmentation;

import ijfx.service.workflow.Workflow;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import net.imagej.display.ImageDisplay;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = SegmentationUiPlugin.class,label="Deep learning",priority=0.01)
public class DeepLearning implements SegmentationUiPlugin<FakeSegmentation>{

    Label label = new Label("Not available yet");

    Property<Img<BitType>> maskProperty = new SimpleObjectProperty<>();
    
    BooleanProperty activated = new SimpleBooleanProperty();
    
    public DeepLearning() {
        label.getStyleClass().addAll("with-padding","warning");
    }
    
    
    
   

    @Override
    public Node getContentNode() {
        return label;
    }



   


    @Override
    public FakeSegmentation createSegmentation(ImageDisplay imageDisplay) {
       return new FakeSegmentation();
    }

    @Override
    public void bind(FakeSegmentation t) {
       
    }

   
    
}
