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
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */

public class ProjectCardContainer extends BorderPane implements ProjectCard{

    
    @FXML
    Label titleLabel;
    
    @FXML
    FontAwesomeIconView icon;
    
    final ProjectCard projectCard;
    
    public ProjectCardContainer(ProjectCard projectCard) {
        this.projectCard = projectCard;
        try {
            FXUtilities.injectFXML(this);
            
            
            setCenter(projectCard.getContent());
            titleLabel.setText(projectCard.getName());
            icon.setIcon(projectCard.getIcon());
        } catch (IOException ex) {
            Logger.getLogger(ProjectCardContainer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public Task<Boolean> update(Project project) {
        return projectCard.update(project);
    }

    @Override
    public String getName() {
        return projectCard.getName();
    }

    @Override
    public FontAwesomeIcon getIcon() {
        return projectCard.getIcon();
    }

    @Override
    public Property<Boolean> dismissable() {
        return projectCard.dismissable();
    }

    @Override
    public Property<Boolean> dismissed() {
        return projectCard.dismissed();
    }
    
    
    
}
