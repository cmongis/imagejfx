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
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.plugins.segmentation.MLSegmentationService;
import ijfx.plugins.segmentation.neural_network.NNType;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import mongis.utils.FXUtilities;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Pierre BONNEAU
 */
public class AlgoSelect extends AbstractStepUi{
    
    private Text folderIcon;
    
    @Parameter
    MLSegmentationService mLSegmentationService;
    
    @FXML
    ChoiceBox algoChoiceBox;
    
    @FXML
    Button initBtn;
    
    @FXML
    Button loadModelBtn;
    
    public AlgoSelect(){
        super("2. Pick an algorithm");
        try {
            FXUtilities.injectFXML(this);
            logger.info("FXML injected");
        }
        catch (IOException ex) {
            Logger.getLogger(AlgoSelect.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        algoChoiceBox.getItems().setAll(NNType.values());
        algoChoiceBox.valueProperty().addListener(this::updateNNType);
        
        initBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onInitModelClicked);
        
        folderIcon = GlyphsDude.createIcon(FontAwesomeIcon.FOLDER_OPEN, "15");
        loadModelBtn.setGraphic(folderIcon);
        loadModelBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onLoadModelClicked);
    }
    
    public void onLoadModelClicked(MouseEvent me){
        FileChooser fileChooser = new FileChooser();
        File tempFile = fileChooser.showOpenDialog(null);
        
        if(tempFile != null)
            mLSegmentationService.loadModel(tempFile);            
    }
    
    public void onInitModelClicked(MouseEvent me){
        mLSegmentationService.initModel();
    }
    
    public void updateNNType(Observable obs){
        if(!initCalled)
            init();
        
        String name = algoChoiceBox.valueProperty().getValue().toString();
        switch(name){
            case "LSTM" : mLSegmentationService.nnType().setValue(NNType.LSTM); break;
            case "BLSTM" : mLSegmentationService.nnType().setValue(NNType.BLSTM); break;
        }
    }
    
    @Override
    public void init() {
        
        super.initCalled = true;
    }
    
    
}
