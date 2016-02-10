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
package ijfx.ui.project_manager.project;

import java.util.function.Consumer;
import javafx.scene.control.TreeItem;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class TreeItemUtils {
    // go through the whole tree and execute the handler
     public synchronized static <T> void goThrough(TreeItem<? extends T> root, Consumer<TreeItem<? extends T>> handler) {
        handler.accept(root);
        for(TreeItem<? extends T> child : root.getChildren()) {
            goThrough(child, handler);
        };
    }
    
    // go through all the leaves
    public static <T> void goThroughLeaves(TreeItem<? extends T> root, Consumer<TreeItem<? extends T>> handler) {
        goThrough(root,child->{
           if(child.isLeaf()) handler.accept(child);
        });
    }
    
    // recursive property that allows to go through a certain sub level of the tree
    public static void goThroughLevel(TreeItem<TreeItem> root, int level,Handler<TreeItem> handler) {
        if(level == 0) {
            handler.handle(root);
            return;
        }
        else {
            root.getChildren().forEach(child->{
                goThroughLevel(child,level-1,handler);
            });
        }
    }
    
    // count all item on a certain level
    public static int countItemOnLevel(TreeItem<TreeItem> root, int level) {
        SimplerCounter counter = new SimplerCounter();
        
        goThroughLevel(root, level, child->counter.increment());
        return counter.getCount();
    }
    
    public static int getDeepestLevel(TreeItem<TreeItem> root, int level) {
        int count = 0;
        int currentLevel = 0;
        
        while(true) {
            count = countItemOnLevel(root,currentLevel);
            if(count == 0) {
                return currentLevel-1;
            }
            else {
                currentLevel++;
            }
        }
    }
    
    public static int getBrotherLevelMaxChildrenNumber(TreeItem<TreeItem> root, int level) {
        
        MaxHolder maxHolder = new MaxHolder();
        goThroughLevel(root, level, item->{
            maxHolder.setMax(item.getChildren().size());
        });
        return maxHolder.getMax();
    }
    
    
    
 
    @FunctionalInterface
    public interface Handler<T> {
        public void handle(T item);
    }
    
    
    
    public static class MaxHolder {
        public int max = 0;
        
        public int getMax() { return max; };
        public void setMax(int possibleMax) {
            if(possibleMax > max) max = possibleMax;
        }
        
    }
    
    
    
}
