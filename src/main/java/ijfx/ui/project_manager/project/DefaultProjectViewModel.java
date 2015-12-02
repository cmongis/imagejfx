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
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

/**
 * Implementation of a ProjectViewModel
 * 
 * The view model holds which item of the project tree is currently visualized.
 * 
 * 
 * @author Cyril Quinton
 */
public class DefaultProjectViewModel implements ProjectViewModel {

    
    private final ReadOnlyObjectWrapper<CheckBoxTreeItem> itemProperty;
    private final ReadOnlyIntegerWrapper indexProperty;
    private final ReadOnlyObjectWrapper<ViewMode> viewModeProperty;
    private final ReadOnlyBooleanWrapper singleViewAvailableProperty;
    private final ReadOnlyListWrapper<CheckBoxTreeItem<PlaneDB>> planeListProperty;
    private final ListChangeListener itemChildrenListener;
    private final ReadOnlyBooleanWrapper rightSlideAvailableProperty;
    private final ReadOnlyBooleanWrapper leftSlideAvailableProperty;

    private ViewMode viewModeBU;

    public DefaultProjectViewModel() {
        itemProperty = new ReadOnlyObjectWrapper<>();
        indexProperty = new ReadOnlyIntegerWrapper(0);
        viewModeProperty = new ReadOnlyObjectWrapper<>();
        viewModeProperty.set(ViewMode.GALLERY);
        singleViewAvailableProperty = new ReadOnlyBooleanWrapper(false);
        planeListProperty = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
        planeListProperty.addListener((ObservableValue<? extends ObservableList<CheckBoxTreeItem<PlaneDB>>> observable, ObservableList<CheckBoxTreeItem<PlaneDB>> oldValue, ObservableList<CheckBoxTreeItem<PlaneDB>> newValue) -> {
            handlePlaneListChange();
        });

        rightSlideAvailableProperty = new ReadOnlyBooleanWrapper(isRightSlideAvailable());
        leftSlideAvailableProperty = new ReadOnlyBooleanWrapper(isLeftSlideAvailable());
        itemProperty.addListener(this::handleItemChange);
        itemChildrenListener = (ListChangeListener) (ListChangeListener.Change c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    if (containsPlaneObject(c.getAddedSubList())) {
                        addPlanesToPlaneList(c.getAddedSubList());
                        singleViewAvailableProperty.set(true);

                    }
                }
                if (c.wasRemoved()) {
                    if (containsPlaneObject(c.getRemoved())) {
                        removePlanesFromPlaneList(c.getRemoved());
                        evaluateSingleAvailable();
                    }
                }
            }
        };
        singleViewAvailableProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            setViewMode(ViewMode.GALLERY);
        });
    }

    @Override

    public void setCurrent(int index
    ) {
        int size = planeListProperty.getSize();
        if (size > 0) {
            int correctedIndex = size == 0 ? -1 : index % (size);
            indexProperty.set(correctedIndex);
            updateSlideProperties();
        }

    }

    @Override
    public void setCurrent(CheckBoxTreeItem item) {
        if (item != null && itemProperty.get() != item) {
            itemProperty.set(item);
            setCurrent(0);
        }
    }

    @Override
    public void setViewMode(ViewMode viewMode
    ) {
        if (viewMode != ViewMode.SINGLE_STACK || singleViewAvailableProperty.get()) {
            viewModeProperty.set(viewMode);
        }
    }

    @Override
    public ReadOnlyObjectProperty<ViewMode> viewModeProperty() {
        return viewModeProperty.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyObjectProperty<CheckBoxTreeItem> nodeProperty() {
        return itemProperty.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyIntegerProperty indexProperty() {
        return indexProperty.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyBooleanProperty singleViewAvailableProperty() {
        return singleViewAvailableProperty.getReadOnlyProperty();
    }

    private void evaluateSingleAvailable() {
        if (itemProperty.get() != null) {
            if (containsPlaneObject(itemProperty.get().getChildren())) {
                singleViewAvailableProperty.set(true);
                return;
            }
        }
        singleViewAvailableProperty.set(false);
    }

    private void handleItemChange(ObservableValue<? extends CheckBoxTreeItem> observable, CheckBoxTreeItem oldValue, CheckBoxTreeItem newValue) {
        if (oldValue != null) {
            oldValue.getChildren().removeListener(itemChildrenListener);
        }
        newValue.getChildren().addListener(itemChildrenListener);
        evaluateSingleAvailable();
        createPlaneList();

    }

    private boolean containsPlaneObject(List children) {
        for (Object child : children) {
            if (isPlaneDBItem(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlaneDBItem(Object o) {
        if (o instanceof CheckBoxTreeItem) {
            TreeItem item = (TreeItem) o;
            if (item.getValue() instanceof PlaneDB) {
                return true;
            }
        }
        return false;
    }

    private final void createPlaneList() {
        planeListProperty.clear();
        if (itemProperty.get() != null) {
            addPlanesToPlaneList(itemProperty.get().getChildren());

        }
    }

    private void addPlanesToPlaneList(List content) {
        List<CheckBoxTreeItem<PlaneDB>> addList = new ArrayList<>();
        for (Object child : content) {
            if (isPlaneDBItem(child)) {
                addList.add((CheckBoxTreeItem<PlaneDB>) child);
            }
        }
        planeListProperty.addAll(addList);
    }

    private void removePlanesFromPlaneList(List content) {
        List<CheckBoxTreeItem<PlaneDB>> rmList = new ArrayList<>();
        for (Object child : content) {
            if (isPlaneDBItem(child)) {
                rmList.add((CheckBoxTreeItem<PlaneDB>) child);
            }
        }
        planeListProperty.removeAll(rmList);
    }

    private PlaneDB getPlane(Object o) {
        return (PlaneDB) ((TreeItem) o).getValue();
    }

    @Override
    public ReadOnlyListProperty<CheckBoxTreeItem<PlaneDB>> planeListProperty() {
        return planeListProperty.getReadOnlyProperty();
    }

    private boolean isLeftSlideAvailable() {
        return indexProperty.get() > 0;
    }

    private boolean isRightSlideAvailable() {
        return indexProperty.get() < planeListProperty.getSize() - 1;
    }

    private void updateSlideProperties() {
        leftSlideAvailableProperty.set(isLeftSlideAvailable());
        rightSlideAvailableProperty.set(isRightSlideAvailable());
    }

    private void handlePlaneListChange() {
        //re-calculate the current index after the list change. 
        setCurrent(indexProperty.get());
    }

    @Override
    public ReadOnlyBooleanProperty rightSlideAvailable() {
        return rightSlideAvailableProperty.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyBooleanProperty leftSlideAvailable() {
        return leftSlideAvailableProperty.getReadOnlyProperty();
    }

    
    
    public TreeItem getPreviousTreeItem(TreeItem treeItem) {
        List<TreeItem> planes = buildPlaneOrder();
        int indexOfPlane = planes.indexOf(treeItem);
        
        indexOfPlane--;
        
        if(indexOfPlane < 1) {
            return planes.get(planes.size()-1);
        }
        if(indexOfPlane == planes.size()) {
            return planes.get(0);
        }
        
        return planes.get(indexOfPlane);
        
    }
    
    
    
    public TreeItem getNextTreeItem(TreeItem treeItem) {
        List<TreeItem> planes = buildPlaneOrder();
        int indexOfPlane = planes.indexOf(treeItem);
        
        indexOfPlane++;
        
        if(indexOfPlane < 1) {
            return planes.get(planes.size()-1);
        }
        if(indexOfPlane == planes.size()) {
            return planes.get(0);
        }
        
        return planes.get(indexOfPlane);
        
    }
    
    
    @Override
    public void nextImage() {
       setCurrent((CheckBoxTreeItem)getNextTreeItem(nodeProperty().get()));
    }
    
    @Override
    public void previousImage() {
      setCurrent((CheckBoxTreeItem)getPreviousTreeItem(nodeProperty().get()));
    }
   
    
    public List<TreeItem> buildPlaneOrder() {
            TreeItem root = getRoot(nodeProperty().get());
            
            List<TreeItem> planeTreeItems = new ArrayList<>();
            
            goThrough(root, planeTreeItems);
            
            return planeTreeItems;
            
    }
    
    
    
    public TreeItem getRoot(TreeItem item) {
        TreeItem root = item.getParent();
        
        if(root == null) return item;
        
        while(root != null) {
            if(root.getParent() == null) {
                return root;
            }
            else {
                root = root.getParent();
            }
            
        }
 
        return item;
    }
    
    public void goThrough(TreeItem<TreeItem> item,List<TreeItem> list) {
       for(TreeItem<TreeItem> i : item.getChildren()) {
           if(i.isLeaf() && ProjectViewModelService.containsPlane(i)) {
               list.add(i);
           }
           else {
               goThrough(i, list);
           }
       }
    }
    
}
