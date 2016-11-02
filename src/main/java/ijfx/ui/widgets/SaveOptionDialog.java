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
package ijfx.ui.widgets;

import ijfx.ui.main.ImageJFX;
import ijfx.ui.save.DefaultSaveOptions;
import ijfx.ui.save.SaveOptions;
import ijfx.ui.save.SaveType;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class SaveOptionDialog extends Dialog<SaveOptions>{
    
    SaveOptions options;
    
    
    public SaveOptionDialog() {
        super();
        getDialogPane().getStylesheets().add(ImageJFX.getStylesheet());
        DefaultSaveOptions saveOptionsPane = new DefaultSaveOptions();
        options = saveOptionsPane;
        
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.NEXT);
        getDialogPane().setContent(saveOptionsPane.getContent());
        
        getDialogPane().lookupButton(ButtonType.NEXT).disableProperty().bind(Bindings.createBooleanBinding(this::canStart, options.saveType(),options.suffix(),options.folder()).not());
        setResultConverter(this::getResult);
    }
    
    
    public SaveOptions options() {
        return options;
    }
    
    
    public boolean canStart() {
        
        SaveType saveType = options.saveType().getValue();
        File folder = options.folder().getValue();
        String prefix = options.suffix().getValue();
        
       if(saveType == SaveType.REPLACE) {
           return true;
       }
       else {
           if(folder  == null) return false;
           else if(prefix == null || "".equals(prefix.trim())) return false;
           return true;
       }
    }
    
    
    public SaveOptions getResult(ButtonType type) {
        if(type == ButtonType.CANCEL) {
            return null;
        }
        else {
            return options();
        }
    }
    
}
