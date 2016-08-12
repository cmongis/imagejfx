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
    
    private Property<SegmentationStep> step;
    private StepUi stepUi;
    
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
        
        step = new SimpleObjectProperty<>(SegmentationStep.DATA_GEN);
        stepUi = new DataGen();
        
        nextBtn.setVisible(false);
        
        ObjectBinding<Boolean> obinding = Bindings.createObjectBinding(this::isNotFirstStep, stepProperty());
        prevBtn.visibleProperty().bind(obinding);
        
        ObjectBinding<Boolean> obinding2 = Bindings.createObjectBinding(this::isNotLastStep, stepProperty());
        nextBtn.visibleProperty().bind(obinding2);
        
        step.addListener(this::onStepChanged);
        
        title.textProperty().setValue(getStepController().getTitle());
        
        this.setCenter(getStepController().getNode());
    }
    
    public void onNextBtnClicked(MouseEvent me){
        switch(getStep()){
            case DATA_GEN: stepProperty().setValue(SegmentationStep.ALGO_SELECT); break;
            case ALGO_SELECT: stepProperty().setValue(SegmentationStep.ALGO_TRAIN); break;
            case ALGO_TRAIN: stepProperty().setValue(SegmentationStep.SEGMENT); break;
            default: throw new AssertionError(getStep().name());
        }
    }
    
    public void onPrevBtnClicked(MouseEvent me){
        switch(getStep()){
            case ALGO_SELECT: stepProperty().setValue(SegmentationStep.DATA_GEN); break;
            case ALGO_TRAIN: stepProperty().setValue(SegmentationStep.ALGO_SELECT); break;
            case SEGMENT: stepProperty().setValue(SegmentationStep.ALGO_TRAIN); break;
            default: throw new AssertionError(getStep().name());
        }
    }
    
    public void onStepChanged(Observable obs){
        updateStepController();
        this.setCenter(getStepController().getNode());
        title.textProperty().setValue(getStepController().getTitle());
    }
    
    public boolean isNotFirstStep(){
        return getStep() != SegmentationStep.DATA_GEN;
    }
    
    public boolean isNotLastStep(){
        return getStep() != SegmentationStep.SEGMENT;
    }
    
    public void setStepController(StepUi stepUi){
        this.stepUi = stepUi;
        context.inject(stepUi);
        stepUi.init();
    }
    
    public StepUi getStepController(){
        return stepUi;
    }
    
    public void updateStepController(){
        switch(getStep()){
            case DATA_GEN: stepUi = new DataGen(); break;
            case ALGO_SELECT: stepUi = new AlgoSelect(); break;
            case ALGO_TRAIN: stepUi = new AlgoTrain(); break;
            case SEGMENT: stepUi = new Segment(); break;
            default:throw new AssertionError(getStep().name());
        }
        context.inject(stepUi);
        stepUi.init();
    }
    
    public Property<SegmentationStep> stepProperty(){
        return step;
    }
    
    public SegmentationStep getStep(){
        return step.getValue();
    }
}
            
