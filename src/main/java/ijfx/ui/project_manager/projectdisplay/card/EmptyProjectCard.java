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
import ijfx.ui.project_manager.project.BrowserUIService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import mongis.utils.AsyncCallback;
import mongis.utils.FXUtilities;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = ProjectCard.class,priority=100.0)
public class EmptyProjectCard extends GridPane implements ProjectCard {

    public static final String NAME = "Hello";
    
    @Parameter
    BrowserUIService browserUIService;
    
    public EmptyProjectCard() {
        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            Logger.getLogger(EmptyProjectCard.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    
    
    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public Task<Boolean> update(Project source) {
        
        return new AsyncCallback<Project,Boolean>()
                .setInput(source)
                .run(this::countImages)
                .then(result->decorator.dismissed().setValue(result));
        
    }

    @Override
    public String getName() {
        return NAME;
        
    }

    @Override
    public FontAwesomeIcon getIcon() {
        return FontAwesomeIcon.SMILE_ALT;
    }

    /**
     * Returns true if the card should be dismissed
     * @param project
     * @return if the card should be dismissed
     */
    public Boolean countImages(Project project) {
        
        
        if(project.getImages().size() >0) {
            return true;
        }
        else {
            return false;
        }
        
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
    
    @FXML
    private void scanFolder() {
        browserUIService.addImageFromDirectoryAction();
    }
    
    @FXML
    private void addImages() {
        browserUIService.addImageAction();
    }

}
