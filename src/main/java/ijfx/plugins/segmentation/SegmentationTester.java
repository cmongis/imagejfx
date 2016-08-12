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
package ijfx.plugins.segmentation;

import ijfx.plugins.segmentation.ui.MLSegmentationUi;
import ijfx.plugins.segmentation.ui.SegUi;
import ijfx.ui.main.ImageJFX;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Pierre BONNEAU
 */

@Plugin(type = Command.class, menuPath = "Plugins>Sandbox>Segmentation with NN")

public class SegmentationTester implements Command{
    
    @Parameter
    Context context;
                    
    @Override
    public void run(){
        
        Platform.runLater(()-> {

//            Popup popup = new Popup();
//            PopupControl popup = new PopupControl();
//            popup.getScene().setRoot(new SegmentationUI());
//            popup.getContent().add(new SegmentationUI());
//            popup.show(ImageJFX.PRIMARY_STAGE);

            Stage stage = new Stage(StageStyle.UTILITY);
            
            MLSegmentationUi segmentationUI = new MLSegmentationUi();
            
            context.inject(segmentationUI);
            
            Scene scene = new Scene(segmentationUI);

//            SegUi segUi = new SegUi();
//            
//            context.inject(segUi);
//            
//            Scene scene = new Scene(segUi);

            String style = ImageJFX.class.getResource("flatterfx.css").toExternalForm();
            
            scene.getStylesheets().add(style);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.show();            
        });
    }
    
}
