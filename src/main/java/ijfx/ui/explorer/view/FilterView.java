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
package ijfx.ui.explorer.view;

import ijfx.ui.explorer.Explorable;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author Tuan anh TRINH
 */
public abstract class FilterView extends AnchorPane {

    protected List<ComboBox<String>> comboBoxList;

    public FilterView() {
        super();
        comboBoxList = new ArrayList<>();

    }

    public ArrayList<String> getMetaDataKey(List<? extends Explorable> items) {
        ArrayList<String> keyList = new ArrayList<String>();
        items.forEach(plane -> {
            plane.getMetaDataSet().keySet().forEach(key -> {

                if (!keyList.contains(key)) {
                    keyList.add(key);
                }
            });
        });
        return keyList;
    }
    
    public abstract void initComboBox();
}
