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
package ijfx.ui.correction;

import ijfx.service.workflow.Workflow;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;
import net.imagej.Dataset;

/**
 *
 * @author cyril
 */
public abstract class AbstractCorrectionUiPlugin extends BorderPane implements CorrectionUiPlugin{

    protected final StringProperty explanationProperty = new SimpleStringProperty();
    
    protected final ObjectProperty<Dataset> datasetProperty = new SimpleObjectProperty<>();
    
    protected final ObjectProperty<Workflow> workflowProperty = new SimpleObjectProperty();
    
    Logger logger = ImageJFX.getLogger();
    
    
    
    
    public AbstractCorrectionUiPlugin(String fxmlUrl) {
        if(fxmlUrl != null) {
        try {
            FXUtilities.injectFXML(this,fxmlUrl);
        }
        catch(IOException ioe) {
            logger.log(Level.SEVERE,"Error when creating Correction Plugin",ioe);
        }
        }
        datasetProperty.addListener(this::onExampleDatasetChanged);
    }
    
    public <T> void bind(Property<T> property,Callable<T> callable,Observable... properties) {
       property.bind(Bindings.createObjectBinding(callable, properties));
    }
    
    @Override
    public ReadOnlyStringProperty explanationProperty() {
        return explanationProperty;
    }

    @Override
    public Property<Dataset> exampleDataset() {
        return datasetProperty;
    }

    @Override
    public ReadOnlyObjectProperty<Workflow> workflowProperty() {
        return workflowProperty;
    }
    
    protected void setWorkflow(Workflow workflow) {
        workflowProperty.setValue(workflow);
    }
    
    protected abstract void onExampleDatasetChanged(Observable obs, Dataset oldValue, Dataset newValue);
    
    
      @Override
    public Node getContent() {
        return this;
    }
}
