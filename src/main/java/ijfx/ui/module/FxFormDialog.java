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
package ijfx.ui.module;

import ijfx.ui.main.ImageJFX;
import ijfx.ui.module.input.InputControl;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class FxFormDialog extends GridPane {

    @FXML
    GridPane gridPane;

    protected int fieldCount = -1;
    protected final int fieldColumn = 1;
    protected int labelColumn = 0;

    
    HashMap<String,InputControl> inputMap = new HashMap<>();
    public FxFormDialog() {
        super();


        try {
          FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
        
        
        
        

    }
    
    public void checkField() {

        for(InputControl input : inputMap.values()) {

            if(input.isValid() == false) {
                validProperty.setValue(false);

                return;
            }
        }
        validProperty.setValue(true);
    }
    
    BooleanProperty validProperty = new SimpleBooleanProperty(true);

    
    public BooleanProperty validProperty() {
        return validProperty;
    }
    
    
    public void addField(String id, String label, InputControl input) {
        
         
        fieldCount++;
        inputMap.put(id,input);
        
        gridPane.add(new Label(label), labelColumn, fieldCount);
        gridPane.add(input, fieldColumn, fieldCount);
        
    }
    
  
    
    
    public HashMap<String,Object> getHashMap() {
        HashMap<String,Object> resultMap = new HashMap<>();
        inputMap.forEach((key,input)-> {
            
            resultMap.put(key, input.chosenValueProperty().getValue());
        
        });
        return resultMap;
    }

    public int inputCount() {
        return inputMap.size();
    }
    
    
    

}
