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

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Pierre BONNEAU
 */
public abstract class AbstractStepUi  extends BorderPane implements StepUi{
    
    protected final String STEP_TITLE;
    protected boolean initCalled;
    
    public AbstractStepUi(String title){
        STEP_TITLE = new String(title);
        initCalled = false;
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
    public boolean isInitCalled(){
        return initCalled;
    }
}
