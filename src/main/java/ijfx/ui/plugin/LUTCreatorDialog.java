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

import ijfx.ui.main.ImageJFX;
import java.util.List;
import java.util.stream.IntStream;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.paint.Color;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;

/**
 *
 * @author Tuan anh TRINH
 */
public class LUTCreatorDialog extends Dialog<LutViewChanger> {

    LUTCreator lUTCreator;

    public LUTCreatorDialog(List<Color> colors) {
        super();

        lUTCreator = new LUTCreator(colors);
        this.setResizable(true);
        this.getDialogPane().setContent(lUTCreator);
        this.getDialogPane().getStylesheets().add(ImageJFX.STYLESHEET_ADDR);
        this.getDialogPane().getButtonTypes().add(ButtonType.OK);
        this.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        this.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add("success");
        this.getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().add("danger");
        this.setResultConverter(this::convert);

    }

    public LutViewChanger convert(ButtonType t) {

        if (t == ButtonType.OK) {
//                        return LUTCreator.colorsToColorTable(lUTCreator.getColors());
            List<Color> sampleColors = lUTCreator.getColors();
            LutViewChanger lutViewChanger = new LutViewChanger("LUT ", LUTCreator.colorsToColorTable(lUTCreator.getGeneratedColors()), sampleColors);
            return lutViewChanger;

        }
        return null;
    }

}
