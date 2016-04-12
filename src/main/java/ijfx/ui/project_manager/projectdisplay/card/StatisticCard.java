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
import ijfx.core.metadata.GenericMetaData;
import ijfx.core.project.Project;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import mongis.utils.AsyncCallback;
import mongis.utils.FXUtilities;
import mongis.utils.FileButtonBinding;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = ProjectCard.class, priority = 10.0)
public class StatisticCard extends BorderPane implements ProjectCard{

    public static final String NAME = "Settings & Stats";
    
    public static final String FILE_NUMBER = "%d";
    
    public static final String PLANE_NUMBER = "%d";
    
    ObjectProperty<File> saveFolder;
    
    @FXML
    Button saveFolderButton;
    
    @FXML
    Label fileNumberLabel;
    
    @FXML
            Label planeNumberLabel;
    
    FileButtonBinding fileButtonBinding;
    
    public StatisticCard()  {
        try {
            FXUtilities.injectFXML(this);
            
            fileButtonBinding = new FileButtonBinding(saveFolderButton);
            
        } catch (IOException ex) {
            Logger.getLogger(StatisticCard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public Task<Boolean> update(Project project) {
        return new AsyncCallback<Project,Boolean>()
                .setInput(project)
                .run(this::updateValues);
                
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public FontAwesomeIcon getIcon() {
        return FontAwesomeIcon.BAR_CHART;
    }
    
    public Boolean updateValues(Project project) {
            
             Platform.runLater(()->updatePlaneAndFileNumbers(project));
           bindSaveFolder(project);
          
           return true;
    }
    
    public void bindSaveFolder(Project project) {
        
        
        
        
        saveFolder = new SimpleObjectProperty<File>() {
            @Override
            public File get() {
              
                System.out.println(project.getSettings().getOrDefault("saveFolder", new GenericMetaData("saveFolder","./")));
                return new File(project.getSettings().getOrDefault("saveFolder",new GenericMetaData("saveFolder", "./")).getStringValue());
            }
            
            @Override
            public void set(File f) {
                System.out.println("Setting things");
                project.getSettings().put(new GenericMetaData("saveFolder", f.getAbsolutePath()));
            }
        };
        
        // unbinding the file button property just in case
        fileButtonBinding.fileProperty().unbind();
        
        // setting the value to the current folder
        fileButtonBinding.fileProperty().setValue(saveFolder.getValue());
        
        // binding the value to the File Button
        saveFolder.bind(fileButtonBinding.fileProperty());
        System.out.println("Here is the end");
    }
    
    public void updatePlaneAndFileNumbers(Project project) {
        
        planeNumberLabel.setText(String.format(PLANE_NUMBER, project.getImages().getSize()));
        
        // I love Java 8 : counting all the different files in one line thanks to stream
        
        int fileNumber = project
                .getImages()
                .stream()
                .map(image->image.getFile())
                .collect(Collectors.toCollection(TreeSet::new))
                .size();
       
        System.out.println(fileNumber);
        fileNumberLabel.setText(String.format(FILE_NUMBER,fileNumber));
        
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
