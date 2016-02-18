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
package ijfx.ui.project_manager.projectdisplay.card;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.core.project.Project;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;

/**
 *
 * @author cyril
 */
public class RulesCard extends BorderPane implements ProjectCard{

    
    @FXML
    Label ruleNumber;
    
    public RulesCard() {
        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            Logger.getLogger(RulesCard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public Task<Boolean> update(Project source) {
        Task<Boolean> task = new ProjectUpdateTask(source, project->{
            
            ruleNumber.setText(""+project.getAnnotationRules().size());
            
            return true;
        });
        
        
        return task;
    }

    @Override
    public String getName() {
        return "Annotation rules";
    }

    @Override
    public FontAwesomeIcon getIcon() {
        return FontAwesomeIcon.ANGELLIST;
    }

    
     DismissableCardDecorator<Project> decorator = new DismissableCardDecorator<>(this);
    
    @Override
    public Property<Boolean> dismissable() {
        return decorator.dismissable();
    }

    @Override
    public Property<Boolean> dismissed() {
        return decorator.dismissed();
    }
    
    
    
}
