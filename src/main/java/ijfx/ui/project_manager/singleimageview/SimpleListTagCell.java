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
package ijfx.ui.project_manager.singleimageview;


import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.ui.project_manager.other.EditHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class SimpleListTagCell extends HBox{
    
    Label label = new Label();
    FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.REMOVE);
    Button button = new Button();
    private final EditHandler editHandler;

    public SimpleListTagCell(EditHandler handler,String text) {
        this.editHandler = handler;
        setText(text);
        
        button.setGraphic(icon);
        getChildren().addAll(button);
        //button.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onDeleteButtonClicked);
        //button.setGraphicTextGap(10);
        button.setOnAction(this::onDeleteButtonClicked);
        
    }
    
    public void setText(String text) {
        button.setText(text);
    }
    
    public String getText() {
        return button.getText();
    }
    
    public void onDeleteButtonClicked(ActionEvent event) {
        editHandler.remove(getText());
    }
    
    public void onDeleteButtonClicked(MouseEvent event) {
        editHandler.remove(getText());
    }
    
    
    
    
   
    
}
