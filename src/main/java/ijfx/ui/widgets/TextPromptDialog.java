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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class TextPromptDialog extends Dialog<String> {

    
    TextPromptContent content;
    
    public TextPromptDialog() {
        super();
        
        getDialogPane().getStylesheets().add(ImageJFX.STYLESHEET_ADDR);
        content = new TextPromptContent();
        getDialogPane().setContent(content);

        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(content.validProperty().not());
        getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add("success");
        getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().add("danger");
        setResultConverter(this::convert);

    }

    public TextPromptContent getContent() {
        return content;
    }
    
    
    
    

    public String convert(ButtonType t) {

        if (t == ButtonType.OK) {
            return content.getText();
        } else {
            return null;
        }
    }
}
