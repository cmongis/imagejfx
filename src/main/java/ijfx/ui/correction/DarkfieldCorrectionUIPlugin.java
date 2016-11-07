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

import ijfx.plugins.flatfield.DarkfieldSubstraction;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowBuilder;
import java.io.File;
import javafx.beans.Observable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import mongis.utils.FileButtonBinding;
import net.imagej.Dataset;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = CorrectionUiPlugin.class,label = "Darkfield substraction")
public class DarkfieldCorrectionUIPlugin extends AbstractCorrectionUiPlugin{

    HBox hbox = new HBox();

    Button fileButton = new Button();
    
    FileButtonBinding binding = new FileButtonBinding(fileButton);
    
    CheckBox multichannelCheckBox = new CheckBox("Multichannel substraction image");
    
    @Parameter
    Context context;
    public DarkfieldCorrectionUIPlugin() {
        super(null);
        
        setCenter(hbox);
        hbox.getStyleClass().add("hbox");
        
        hbox.getChildren().add(new Label("Select flatfield image : "));
        hbox.getChildren().add(fileButton);
        setBottom(multichannelCheckBox);
        binding.setOpenFile(true);
        bindP(workflowProperty,this::generateWorkflow,binding.fileProperty(),multichannelCheckBox.selectedProperty());
        bindP(explanationProperty,this::explain,binding.fileProperty(),multichannelCheckBox.selectedProperty());
    }
    
    
    
    @Override
    protected void onExampleDatasetChanged(Observable obs, Dataset oldValue, Dataset newValue) {
        
        
    }

    @Override
    public void init() {
        
    }
    
    public File getFile() {
        return binding.fileProperty().getValue();
    }
    
    
    private boolean isMultichannel() {
        return multichannelCheckBox.isSelected();
    }
    
    
    public Workflow generateWorkflow() {
        
        
        
        
        if(getFile() != null) {
            return new WorkflowBuilder(context)
                    .addStep(DarkfieldSubstraction.class, "file",getFile(),"multichannel",isMultichannel())
                    .getWorkflow("");   
        }
        else {
            return null;
        }   
    }
    
    public String explain() {
        if(getFile() == null) {
            return "!Please specifiy a darkfield image!";
        }
        else {
            return String.format("Darkfield substraction using *%s*",getFile().getName());
        }
    }
    
}
