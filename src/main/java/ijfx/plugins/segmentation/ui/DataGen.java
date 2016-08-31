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
import ijfx.plugins.segmentation.ProfileEvent;
import ijfx.ui.messageBox.DefaultMessage;
import ijfx.ui.messageBox.DefaultMessageBox;
import ijfx.ui.messageBox.MessageBox;
import ijfx.ui.messageBox.MessageType;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import mongis.utils.FXUtilities;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Pierre BONNEAU
 */
public class DataGen extends AbstractStepUi{
    
    @FXML
    RadioButton trainingRadio;
    
    @FXML
    RadioButton testRadio;
    
    @FXML
    TextField widthField;
    
    @FXML
    Text numTrainingData;
    
    @FXML
    Button seedingBtn;
    
    @FXML
    Text numTestData;
    
    @FXML
    Button clearDataBtn;
    
    @FXML
    Button generateBtn;
    
    @FXML
    VBox feedbackBox;
    
    MessageBox msgBox;
    
    public DataGen(){
        super("1. Extract data from images", SegmentationStep.DATA_GEN);
        
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
        
        msgBox = new DefaultMessageBox();
        feedbackBox.getChildren().add(msgBox.getContent());
        
        generateBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::generate);
        clearDataBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onClearDataClicked);
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
            mLSegmentationService.generateTrainingSet();
        }
        else{
            mLSegmentationService.generateTestSet();
        }
    }
    
    public void onClearDataClicked(MouseEvent me){
        msgBox.messageProperty().setValue(null);
        if(!initCalled)
            init();
        mLSegmentationService.clearData();
    }
    
    @EventHandler
    public void onProfileEvent(ProfileEvent event){
        switch(event.getType()){
            case TRAIN :
                msgBox.messageProperty().setValue(null);
                msgBox.messageProperty().setValue(new DefaultMessage("INFO : "+event.getNum()+" profiles have been added to the training set", MessageType.SUCCESS));
//                numTrainingData.textProperty().setValue(mLSegmentationService.datasetSize(mLSegmentationService.getTrainingSet()).toString());
                break;
            case TEST :
                msgBox.messageProperty().setValue(null);
                msgBox.messageProperty().setValue(new DefaultMessage("INFO : "+event.getNum()+" profiles have been added to the training set", MessageType.SUCCESS));
//                numTestData.textProperty().setValue(mLSegmentationService.datasetSize(mLSegmentationService.getTestSet()).toString());
                break;
            default: throw new AssertionError(event.getType().name());
        }
            
    }

    @Override
    public void init() {
        widthField.textProperty().setValue(mLSegmentationService.membraneWidth().toString());        
        super.initCalled = true;
    }
    
    public int datasetSize(){
        return 0;
    }    
}
