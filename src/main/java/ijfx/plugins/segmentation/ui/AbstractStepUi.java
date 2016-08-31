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

import ijfx.plugins.segmentation.MLSegmentationService;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Pierre BONNEAU
 */
public abstract class AbstractStepUi  extends BorderPane implements StepUi{
    
    protected final String STEP_TITLE;
    protected SegmentationStep stepType;
    protected boolean initCalled;
    protected boolean injected;
    
    @Parameter
    MLSegmentationService mLSegmentationService;
    
    public AbstractStepUi(String title, SegmentationStep type){
        STEP_TITLE = new String(title);
        stepType = type;
        initCalled = false;
        injected = false;
    }

    @Override
    public String getTitle() {
        return this.STEP_TITLE;
    }

    @Override
    public Node getNode() {
        return this;
    }
    
    @Override
    public SegmentationStep getType(){
        return this.stepType;
    }
    
    
    @Override
    public boolean isInitCalled(){
        return initCalled;
    }
    
    @Override
    public boolean isInjected(){
        return injected;
    }
    
    @Override
    public void setInitCalled(boolean called){
        initCalled = called;
    }
    
    @Override
    public void setInjected(boolean injected){
        this.injected = injected;
    }
}
