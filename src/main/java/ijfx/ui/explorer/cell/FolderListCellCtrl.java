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
package ijfx.ui.explorer.cell;

import ijfx.ui.explorer.Folder;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import mongis.utils.FXUtilities;
import mongis.utils.ListCellController;
import static org.reactfx.util.Tuples.t;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class FolderListCellCtrl extends VBox implements ListCellController<Folder> {

    @FXML
    Label titleLabel;

    @FXML
    Label subtitleLabel;

    Folder currentFolder;

    Property<Task> currentTaskProperty = new SimpleObjectProperty<>();
    
    BooleanProperty taskRunningProperty = new SimpleBooleanProperty();
    
    public FolderListCellCtrl() {
        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            Logger.getLogger(FolderListCellCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setItem(Folder t) {

        currentFolder = t;

        if (t != null) {
            titleLabel.setText(t.getName());
            updateLabel();
        }
    }

    public void updateLabel() {
        subtitleLabel.textProperty().unbind();
        subtitleLabel.setText(String.format("%d images", currentFolder.getFileList().size()));
    }
    
    @Override
    public Folder getItem() {
        return currentFolder;
    }

    
    public void forceUpdate() {
        if(currentFolder != null) {
            setItem(currentFolder);
        }
    }
    
    
    
    private void onCurrentTaskChanged(Observable obs, Task oldValue, Task newValue) {
        
        subtitleLabel.textProperty().bind(newValue.messageProperty());
        taskRunningProperty.bind(newValue.runningProperty());
        
        
    }
    
    private void onTaskStatusChanged(Observable obs, Boolean oldValue, Boolean isRunning) {
        if(!isRunning) {
            updateLabel();
        }
        
        
    }
    
}
