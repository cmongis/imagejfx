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
import ijfx.plugins.segmentation.ProfilesSet;
import ijfx.plugins.segmentation.MLSegmentationService;
import ijfx.plugins.segmentation.neural_network.NNType;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import mongis.utils.FXUtilities;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Pierre BONNEAU
 */
public class SegUi extends TabPane{
    
    @Parameter
    MLSegmentationService segmentationService;
    
    // TAB 1

    @FXML
    ChoiceBox nnChoice;
    
    // TAB 2
    
    @FXML
    Button trainingDataBtn;
    
    @FXML
    Button trainNetBtn;
    
    @FXML
    TextField membraneWidthField;
    
    @FXML
    Text dataSetSizeTxt;    
    
    // TAB 3
    
    public SegUi(){
        try {
            FXUtilities.injectFXML(this);
            logger.info("FXML injected");
        }
        catch (IOException ex) {
            Logger.getLogger(SegUi.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("SegmentationUI.fxml"));
//            loader.setRoot(this);
//            loader.setController(this);
//            loader.load();
//        }
//        catch (IOException ex) {
//            Logger.getLogger(SegUi.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        trainingDataBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onTrainingDataBtnClicked);
        trainNetBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onTrainBtnClicked);

        nnChoice.getItems().setAll(NNType.values());
        nnChoice.valueProperty().addListener(this::updateNNType);
    }
    
    public Node getNode(){
        return this;
    }
    
    public void onTrainingDataBtnClicked(MouseEvent e){
        
        int mbWidth = Integer.parseInt(membraneWidthField.textProperty().getValue());
        segmentationService.membraneWidthProperty().setValue(mbWidth);
        
        segmentationService.generateTrainingSet();
        
        int dataSetSize = 0;
        for (ProfilesSet trainingSet : segmentationService.getTrainingSet()) {
            dataSetSize += trainingSet.getProfiles().size();
        }
        dataSetSizeTxt.textProperty().setValue(dataSetSize + " profiles generated from " + segmentationService.getTrainingSet().size() +" dataset(s).");
        
    }
    
    public void updateNNType(Observable obs){
        String name = nnChoice.valueProperty().getValue().toString();
        switch(name){
            case "LSTM" : segmentationService.nnType().setValue(NNType.LSTM); break;
            case "BLSTM" : segmentationService.nnType().setValue(NNType.BLSTM); break;
        }
    }
    
    public void onTrainBtnClicked(MouseEvent e){
        segmentationService.train();
    }
    
    public void init(){
    }
}
