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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.service.ui.HintService;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.batch.WorkflowPanel;
import ijfx.ui.main.Localization;
import ijfx.ui.plugin.AbstractContextButton;
import javafx.event.ActionEvent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type=UiPlugin.class,priority=1.0)
@UiConfiguration(context = "segment segmentation",id="explain-me-segmentation-button",localization = Localization.TOP_RIGHT)
public class ExplainMeSegmentationButton extends AbstractContextButton {

    
    @Parameter
    HintService hintService;
    
    @Parameter
    ActivityService activityService;
    
    public ExplainMeSegmentationButton() {
        super("Explain me", FontAwesomeIcon.INFO_CIRCLE);
        //getButton().setText("Explain me");
        getButton().getStyleClass().add("success");
    }
    
    @Override
    public void onAction(ActionEvent event) {
        playHint(SegmentationPanel.class);
    }
     
    
    public void playHint(Class<?> clazz) {
        
        hintService.displayHints(clazz, true);
        hintService.displayHints(WorkflowPanel.class,true);
        
    }
    
}
