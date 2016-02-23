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
package ijfx.ui.project_manager.project.rules;

import ijfx.core.project.AnnotationRule;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import javafx.fxml.FXML;
import mongis.utils.AsyncCallback;
import net.imagej.ImageJService;
import org.scijava.event.EventService;
import org.scijava.event.SciJavaEvent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class RuleEditorService extends AbstractService implements ImageJService {

    EditableRule editedRule;

    @Parameter
    EventService eventService;

    @Parameter
    ProjectManagerService projectManagerService;

    public void createRule() {
        editRule(new EditableRule());
    }

    public void editRule(AnnotationRule rule) {

        if (rule instanceof EditableRule) {
            editedRule = (EditableRule) editedRule;
        } else {
            editedRule = new EditableRule(rule);
        }

        eventService.publishLater(new EditedRuleChangeEvent());

    }

    public AnnotationRule getEditedRule() {
        return editedRule;
    }

    public void saveRuleToProject() {
        projectManagerService.getCurrentProject().getAnnotationRules().add(editedRule);
    }

    public class EditedRuleChangeEvent extends SciJavaEvent {

    }

    @FXML
    public void save() {
       
    }

    @FXML
    public void cancel() {

    }

}
