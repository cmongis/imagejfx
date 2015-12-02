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
package ijfx.core.project.hierarchy;

import ijfx.core.listenableSystem.Listening;
import ijfx.core.project.imageDBService.Selectable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

/**
 *
 * @author Cyril Quinton
 */
public interface MetaDataHierarchy extends Listening{

    CheckBoxTreeItem getRoot();
    
    
    
    ReadOnlyBooleanProperty updatingProperty();
    
    ReadOnlyBooleanProperty updatedProperty();
    
    ReadOnlyDoubleProperty progressProperty();

    public static List<TreeItem> getLeaves(TreeItem item) {
        List<TreeItem> leaves = new ArrayList<>();
        getLeavesRec(item, leaves);
        return leaves;
    }

    public static void getLeavesRec(TreeItem item, List<TreeItem> leaves) {
        for (Object child : item.getChildren()) {
            if (child instanceof TreeItem) {
                getLeavesRec((TreeItem) child, leaves);
            }
        }
        if (item.getChildren().size() == 0) {
            leaves.add(item);
        }
    }

    public static void BFS(TreeItem root, Predicate<TreeItem> p, Consumer<TreeItem> c, BreakCondition<TreeItem> b) {
        //init the list
        List<TreeItem> visitList = new ArrayList();
        for (Object child : root.getChildren()) {
            if (child instanceof TreeItem) {
                visitList.add((TreeItem) child);

            }
        }
        while (!visitList.isEmpty()) {
            TreeItem item = visitList.remove(0);
            if (p.test(item)) {
                c.accept(item);
            }
            if (b.needToBreak(item)) {
                break;
            }
            for (Object child : item.getChildren()) {
                if (child instanceof TreeItem) {
                    visitList.add((TreeItem) child);

                }
            }
        }
    }
}
