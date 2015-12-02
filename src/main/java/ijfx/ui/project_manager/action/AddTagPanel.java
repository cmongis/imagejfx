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
package ijfx.ui.project_manager.action;

import ijfx.core.project.event.PossibleTagListChangeEvent;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class AddTagPanel extends GridPane {

    Context context;

    
    @FXML
    private TextField tagTextField;

    @FXML
    private ListView<String> suggestionListView;

    @FXML
    private Button actionButton;
    
    @FXML
    Label titleLabel;
    
    private static final Logger logger = ImageJFX.getLogger();

    
    private ObservableList<String> possibleTags = FXCollections.observableArrayList();
    
    private Callback<String,Void> action;
    
    public AddTagPanel(Context context) {
        this.context = context;
        context.inject(this);
        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/browser/search/SearchByTagPanel.fxml");
            
            
            titleLabel.setText("Add tag(s)");
            actionButton.setText("Add");
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    public void reset() {
        tagTextField.getText();
    }

    @FXML
    public void search() {
        action.call(tagTextField.getText());
        tagTextField.setText("");
        
    }

    public void setAction(Callback<String,Void> action) {
        this.action = action;
    }

    @EventHandler
    public void onPossibleTagListChanged(PossibleTagListChangeEvent tagEvent) {
        Set<String> tags = tagEvent.getTagList();
        
        suggestionListView.getItems().clear();
        suggestionListView.getItems().addAll(tags);
        
    }
    
    
}
