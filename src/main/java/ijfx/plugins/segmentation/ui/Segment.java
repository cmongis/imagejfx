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
package ijfx.plugins.segmentation.ui;

import static com.squareup.okhttp.internal.Internal.logger;
import ijfx.plugins.segmentation.MLSegmentationService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import mongis.utils.FXUtilities;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Pierre BONNEAU
 */
public class Segment extends AbstractStepUi{
    
    @FXML
    Button segmentBtn;
    
    @FXML
    Button dummyBtn;
    
    public Segment(){
        super("4. Start segmentation", SegmentationStep.SEGMENT);
        
        try {
            FXUtilities.injectFXML(this);
            logger.info("FXML injected");
        }
        catch (IOException ex) {
            Logger.getLogger(Segment.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        segmentBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onSegmentBtnClicked);
        dummyBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onDummyBtnClicked);
    }

    @Override
    public void init() {
        
        super.initCalled = true;
    }
    
    public void onSegmentBtnClicked(MouseEvent e){
        mLSegmentationService.segment();
    }
    
    public void onDummyBtnClicked(MouseEvent e){
        mLSegmentationService.dummySegmentation();
    }
    
}
