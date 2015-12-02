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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javafx.scene.control.TreeItem;

/**
 *
 * @author Cyril Quinton
 */
public interface NodeController {

    public TreeItem getNodeReference();

    public static TreeItem findItem(TreeItem item, Predicate<NodeController> p) {
        List<TreeItem> queue = new ArrayList<>();
        for (Object child : item.getChildren()) {
            if (child instanceof TreeItem) {
                TreeItem treeItem = (TreeItem) child;
                queue.add(treeItem);
                if (testItem(treeItem, p)) {
                    return treeItem;
                }
            }
        }
        while (!queue.isEmpty()) {
            findItem(queue.remove(0), p);

        }
        return null;
    }

    public static boolean testItem(TreeItem treeItem, Predicate<NodeController> p) {
        Object value = treeItem.getValue();
        if (value instanceof NodeController) {
            NodeController nodeController = (NodeController) value;
            return p.test(nodeController);
        }
        return false;
    }
}
