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
package ijfx.ui.project_manager.search;

import ijfx.core.project.event.PossibleMetaDataKeysChangeEvent;
import ijfx.core.project.query.SimpleSelector;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class SearchByMetaDataPanel extends GridPane {
    
    
    private Context context;

    private final static Logger logger = ImageJFX.getLogger();
    
    @FXML
    private ComboBox<String> keyComboBox;
    
    @FXML
    private TextField valueTextField;
    
    private SearchHandler searchHandler;
    
    public SearchByMetaDataPanel(Context context) {
        super();
        this.context = context;
        context.inject(this);  
        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void setSearchHandler(SearchHandler searchHandler) {
        this.searchHandler = searchHandler;
    }
    
    
    
    @FXML
    public void search() {
        searchHandler.search(new SimpleSelector(keyComboBox.getValue(),valueTextField.getText()));
    }
    
    @EventHandler
    public void onNewTagList(PossibleMetaDataKeysChangeEvent event) {
        keyComboBox.getItems().clear();
        keyComboBox.getItems().addAll(event.getKeys());
    }
    
    
    
    
}
