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

import java.util.List;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;

/**
 *
 * @author Tuan anh TRINH
 */
public class LutViewChanger extends LUTView {

    private List<Color> listColors;

    public LutViewChanger(String name, ColorTable table, List<Color> colors) {
        super(name, table);
        listColors = colors;

    }

    public List<Color> getObservableListColors() {
        return listColors;
    }
//    public LutViewChanger(List<Color> colors, byte[]  
//        ... values) {
//        super(values);
//    }
//    

    public void setElements(List<Color> colors) {
        listColors.clear();
        listColors.addAll(colors);
    }
}
