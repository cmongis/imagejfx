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

import ijfx.bridge.FxPromptDialog;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.module.FxFormDialog;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import mongis.utils.FXUtilities;
import net.imglib2.display.ColorTable;

/**
 *
 * @author Tuan anh TRINH
 */
public class LUTCreatorDialog extends Dialog<ColorTable> {

    LUTCreator lUTCreator;
    public LUTCreatorDialog() {
        super();
        lUTCreator = new LUTCreator();
        this.getDialogPane().setContent(lUTCreator);
        this.getDialogPane().getButtonTypes().add(ButtonType.OK);
        this.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
       
    }

}
