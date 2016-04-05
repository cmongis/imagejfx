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
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import mongis.utils.FXUtilities;
import mongis.utils.ListCellController;

/**
 *
 * @author cyril
 */
public class FolderListCellCtrl extends VBox implements ListCellController<Folder> {

    @FXML
    Label titleLabel;

    @FXML
    Label subtitleLabel;

    Folder currentFolder;

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
            subtitleLabel.setText(String.format("%d images", t.getItemList().size()));
        }
    }

    @Override
    public Folder getItem() {
        return currentFolder;
    }

}
