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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Pierre BONNEAU
 */
public class MLSegmentationUi extends BorderPane{
    
    private StepUi[] steps;
    private Property<StepUi> currStepUiProperty;
    private boolean initCalled = false;
    
    @Parameter
    Context context;
    
    @FXML
    Label title;
    
    @FXML
    Button nextBtn;
    
    @FXML
    Button prevBtn;
    
    
    public MLSegmentationUi(){
        try {
            FXUtilities.injectFXML(this);
            logger.info("FXML injected");
        }
        catch (IOException ex) {
            Logger.getLogger(MLSegmentationUi.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        nextBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onNextBtnClicked);
        prevBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onPrevBtnClicked);
        
        steps = new StepUi[4];
        steps[0] = new DataGen();
        steps[1] = new AlgoSelect();
        steps[2] = new AlgoTrain();
        steps[3] = new Segment();
        
        currStepUiProperty = new SimpleObjectProperty<>(steps[0]);
        
        nextBtn.setVisible(false);
        
        ObjectBinding<Boolean> obinding = Bindings.createObjectBinding(this::isNotFirstStep, stepUiProperty());
        prevBtn.visibleProperty().bind(obinding);
        
        ObjectBinding<Boolean> obinding2 = Bindings.createObjectBinding(this::isNotLastStep, stepUiProperty());
        nextBtn.visibleProperty().bind(obinding2);
        
        title.textProperty().setValue(getStepUi().getTitle());
        
        stepUiProperty().addListener(this::onStepChanged);
        
        this.setCenter(getStepUi().getNode());
    }
    
    public void onNextBtnClicked(MouseEvent me){
        switch(getStep()){
            case DATA_GEN: 
                if(steps[1].equals(null)) steps[1] = new AlgoSelect();
                stepUiProperty().setValue(steps[1]); break;
            case ALGO_SELECT:
                if(steps[2].equals(null)) steps[2] = new AlgoTrain();
                stepUiProperty().setValue(steps[2]); break;
            case ALGO_TRAIN:
                if(steps[3].equals(null)) steps[3] = new Segment();
                stepUiProperty().setValue(steps[3]); break;
            default: throw new AssertionError(getStep().name());
        }
        if(!getStepUi().isInjected()){
            context.inject(getStepUi());
            getStepUi().setInjected(true);
        }
        if(!getStepUi().isInitCalled())
            getStepUi().init();
        
    }
    
    public void onPrevBtnClicked(MouseEvent me){
        switch(getStep()){
            case ALGO_SELECT: stepUiProperty().setValue(steps[0]); break;
            case ALGO_TRAIN: stepUiProperty().setValue(steps[1]); break;
            case SEGMENT: stepUiProperty().setValue(steps[2]); break;
            default: throw new AssertionError(getStep().name());
        }
    }
    
    public void onStepChanged(Observable obs){
        this.setCenter(getStepUi().getNode());
        title.textProperty().setValue(getStepUi().getTitle());
    }
    
    public boolean isNotFirstStep(){
        return getStep() != SegmentationStep.DATA_GEN;
    }
    
    public boolean isNotLastStep(){
        return getStep() != SegmentationStep.SEGMENT;
    }
    
    public void setStepController(StepUi stepUi){
        context.inject(stepUi);
        stepUi.init();
        this.currStepUiProperty.setValue(stepUi);
    }
    
    public Property<StepUi> stepUiProperty(){
        return currStepUiProperty;
    }
    
    public StepUi getStepUi(){
        return currStepUiProperty.getValue();
    }
    
    public SegmentationStep getStep(){
        return currStepUiProperty.getValue().getType();
    }
    
    public void init(){
        context.inject(getStepUi());
        getStepUi().init();
        initCalled = true;
    }
    
    public boolean isInitCalled(){
        return initCalled;
    }
}
            
