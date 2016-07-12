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
package ijfx.ui.plugin;

import ijfx.ui.explorer.view.GridIconView;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mongis.utils.FXUtilities;
import net.imglib2.display.ColorTable;

/**
 *
 * @author Tuan anh TRINH
 */
public class LUTCreator extends BorderPane {

    @FXML
    ListView<Rectangle> listView;

    public LUTCreator() {
        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/plugin/LUTCreator.fxml");
            listView.setItems(FXCollections.observableArrayList());
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    listView.getItems().add(new Rectangle(200, 200, Color.AQUA));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GridIconView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    

}
