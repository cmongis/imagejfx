/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.ui.project_manager;

import mongis.utils.FXUtilities;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Cyril Quinton
 */
public class FileAlreadyExistsController extends GridPane implements Initializable {
    
    private final File file;
    private boolean overwrite = false;
    @FXML
    Button cancelButton;
    @FXML
    Button overwriteButton;
    @FXML
    Label messageLabel;
    
    public FileAlreadyExistsController(File file) {
        this.file = file;
        FXUtilities.loadView(getClass().getResource("FileAlreadyExists.fxml"), this, true);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String message = resources.getString("fileAlreadyExists") + " " + file.getName()
                + "\n" + resources.getString("overwriteQuestion");
        messageLabel.setText(message);
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                overwrite = false;
                close();
            }
        });
        overwriteButton.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                overwrite = true;
                close();
            }
        });
        
    }
    
    public boolean overwrite() {
        return overwrite;
    }
    
    private void close() {
        FXUtilities.close(this);
    }
    
}
