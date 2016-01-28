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

import ijfx.core.project.event.PossibleMetaDataKeysChangeEvent;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.Pair;
import org.controlsfx.control.textfield.TextFields;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class AddMetaDataPanel extends GridPane{
    
    @FXML
    private TextField keyTextField;
    
    @FXML
    private TextField valueTextField;
    
    
    @Parameter
    Context context;

    ObservableList<String> existingKeys = FXCollections.observableArrayList();
    
    Callback<Pair<String,String>,Boolean> action;
    
    
    public AddMetaDataPanel(Context ctx) {
        
        super();
        try {
            FXUtilities.injectFXML(this);
            ctx.inject(this);
            
            TextFields.bindAutoCompletion(keyTextField, existingKeys);
            
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
            
    }

    public void setAction(Callback<Pair<String, String>, Boolean> action) {
        this.action = action;
    }
    
    
    
    
    @FXML
    public void addMetaData() {
        
        String key = keyTextField.getText();
        String value = valueTextField.getText();
        
        if(key.trim().equals("")) return;
        
        if(value.trim().equals("")) return;
        
        if(action.call(new Pair<String,String>(key,value))) {
            keyTextField.setText("");
            valueTextField.setText("");
        }
    }
    
    @EventHandler
    public void onPossibleKeyChange(PossibleMetaDataKeysChangeEvent event) {
        existingKeys.clear();
        existingKeys.addAll(event.getKeys());
    }
    
}
