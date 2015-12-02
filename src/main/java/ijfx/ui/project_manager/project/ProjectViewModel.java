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

import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.project_manager.BrowsingModel;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.CheckBoxTreeItem;

/**
 *
 * @author Cyril Quinton
 */
public interface ProjectViewModel extends BrowsingModel {

    void setCurrent(int index);

    void setCurrent(CheckBoxTreeItem item);

    ReadOnlyObjectProperty<CheckBoxTreeItem> nodeProperty();

    ReadOnlyIntegerProperty indexProperty();

    ReadOnlyListProperty<CheckBoxTreeItem<PlaneDB>> planeListProperty();

    ReadOnlyBooleanProperty rightSlideAvailable();

    ReadOnlyBooleanProperty leftSlideAvailable();

    public void previousImage();
    public void nextImage();
    
    public default List<CheckBoxTreeItem> getCheckedChildren() {
        
        ArrayList<CheckBoxTreeItem> list = new ArrayList<>();
        
        nodeProperty().getValue().getChildren().forEach(child->{
            if(child instanceof CheckBoxTreeItem) {
                CheckBoxTreeItem childItem = (CheckBoxTreeItem) child;
                
                if(childItem.isSelected()) {
                    list.add(childItem);
                }
            }
        });
        
        return list;
    }
    
    
}
