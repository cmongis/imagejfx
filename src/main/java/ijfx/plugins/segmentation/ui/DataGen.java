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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import mongis.utils.FXUtilities;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Pierre BONNEAU
 */
public class DataGen extends AbstractStepUi{
    
    @Parameter
    MLSegmentationService mLSegmentationService;
    
    @FXML
    RadioButton trainingRadio;
    
    @FXML
    RadioButton testRadio;
    
    @FXML
    TextField widthField;
    
    @FXML
    Button seedingBtn;
    
    @FXML
    TextField feedbackData;
    
    @FXML
    Button clearDataBtn;
    
    @FXML
    Button generateBtn;
    
    public DataGen(){
        super("1. Extract data from images");
        
        try {
            FXUtilities.injectFXML(this);
            logger.info("FXML injected");
        }
        catch (IOException ex) {
            Logger.getLogger(DataGen.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ToggleGroup toggleGroup = new ToggleGroup();
        trainingRadio.setToggleGroup(toggleGroup);
        testRadio.setToggleGroup(toggleGroup);
        widthField.disableProperty().bind(testRadio.selectedProperty());
        trainingRadio.selectedProperty().setValue(Boolean.TRUE);
        seedingBtn.disableProperty().bind(trainingRadio.selectedProperty());
        
        generateBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::generate);
    }
    
    public void generate(MouseEvent e){
        if(!initCalled)
            init();
        
        if(trainingRadio.isSelected()){
            try{
                int mbWidth = Integer.parseInt(widthField.textProperty().getValue());
                mLSegmentationService.membraneWidthProperty().setValue(mbWidth);
            }
            catch(NumberFormatException nfe){
                Logger.getLogger(DataGen.class.getName()).log(Level.SEVERE, null, nfe);
            }
        }
    }
    
    public void onClearDataClicked(MouseEvent me){
        if(!initCalled)
            init();
        mLSegmentationService.clearData();
    }

    @Override
    public void init() {
        widthField.textProperty().setValue(mLSegmentationService.membraneWidth().toString());
        
        clearDataBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onClearDataClicked);
        
        super.initCalled = true;
    }
}
