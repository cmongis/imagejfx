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
package ijfx.ui.project_manager.other;

import javafx.scene.control.CheckBoxTreeItem;

/**
 *
 * @author Cyril Quinton
 */
public class DefaultSelectableNodeController implements SelectableNodeController {

    private final CheckBoxTreeItem item;

    public DefaultSelectableNodeController(CheckBoxTreeItem node) {
        this.item = node;
    }

    @Override
    public CheckBoxTreeItem getNodeReference() {
        return item;
    }

    @Override
    public String toString() {
        return item.getValue().toString();
    }

}
