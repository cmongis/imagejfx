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
package ijfx.ui.plugin.panel;

import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.batch.WorkflowPanel;
import ijfx.ui.main.Localization;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongis.utils.FXUtilities;
import net.imagej.ops.Ops.Threshold;
import net.imagej.plugins.commands.binary.Binarize;
import net.imagej.plugins.commands.imglib.GaussianBlur;
import net.imglib2.algorithm.binary.Thresholder;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type=UiPlugin.class)
@UiConfiguration(id = "segmentation-panel",context = "segmentation",localization = Localization.RIGHT)
public class SegmentationPanel extends BorderPane implements UiPlugin{
    
    @Parameter
    private Context context;
    
    @FXML
    private VBox workflowVBox;
    
    
    WorkflowPanel workflowPanel;
    
    public SegmentationPanel() {
        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            Logger.getLogger(SegmentationPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public UiPlugin init() {
        
        workflowPanel = new WorkflowPanel(context);
        
        
        workflowVBox.getChildren().add(workflowPanel);
        
        workflowPanel.addStep(GaussianBlur.class);
        workflowPanel.addStep(Binarize.class);
        
        return this;
    }

    @Override
    public Node getUiElement() {
        return this;
    }
    
    
}
