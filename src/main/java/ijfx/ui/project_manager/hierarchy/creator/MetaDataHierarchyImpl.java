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
package ijfx.ui.project_manager.hierarchy.creator;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.metadata.MetaData;
import ijfx.core.project.Project;
import ijfx.core.project.hierarchy.MetaDataHierarchy;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.project_manager.project.ProjectViewModel;
import ijfx.ui.project_manager.project.DefaultProjectViewModelService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import org.scijava.Context;

/**
 *
 * @author Cyril Quinton
 */
public class MetaDataHierarchyImpl implements MetaDataHierarchy {

    public enum ItemType {

        METADATA, IMAGE, SELECTION
    };
    public enum ImageGroup { ALL_IMAGE,SELECTED};
    public static int NB_PLANE_THREAD = 10;
    private final CheckBoxTreeItem rootItem;
    private final HashMap<PlaneDB, TreeItem> planeNodeMap;
    private final HashMap<PlaneDB, ImageListeners> imageListenersMap;
    private final ReadOnlyListProperty<PlaneDB> planes;
    private final ReadOnlyListProperty<String> hierarchy;
    private final ReadOnlyBooleanWrapper updatingProperty;
    private final ReadOnlyBooleanWrapper updatedProperty;

    private final ReadOnlyDoubleWrapper progressProperty;
    private final Project project;
    private Future futureTask;

    // private final ThreadService threadService;
    public MetaDataHierarchyImpl(Project project, ReadOnlyListProperty<String> hierarchy, ReadOnlyListProperty<PlaneDB> planes) {
        this.project = project;
        this.planes = planes;
        this.hierarchy = hierarchy;
        this.rootItem = new CheckBoxTreeItem();
        // threadService = contextService.getContext().getService(ThreadService.class);
        
        hierarchy.addListener(this::handleHierarchyChange);
        planes.addListener(this::handlePlaneListChange);
        planeNodeMap = new HashMap<>();
        imageListenersMap = new HashMap<>();
        updatingProperty = new ReadOnlyBooleanWrapper(false);
        updatedProperty = new ReadOnlyBooleanWrapper(false);
        progressProperty = new ReadOnlyDoubleWrapper(0);
        createTree();
    }

    @Override
    public CheckBoxTreeItem getRoot() {
        return rootItem;
    }

    private void createTree() {
        updatedProperty.set(false);
        Task<Void> runnable = new Task<Void>() {

            @Override
            public Void call() {

                rootItem.getChildren().clear();
                planeNodeMap.clear();
                int total = imageListenersMap.keySet().size() + planes.size();
                int progress = 0;
                for (PlaneDB image : imageListenersMap.keySet()) {
                    imageListenersMap.get(image).stopListening();
                    progress ++;
                    updateProgress(progress, total);
                }
                for (PlaneDB image : planes) {
                    addImage(image);
                    progress ++;
                    updateProgress(progress, total);
                }
                return null;
            }
            
        };

        runTask(runnable);

    }

    @Override
    public ReadOnlyBooleanProperty updatingProperty() {
        return updatingProperty.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyDoubleProperty progressProperty() {
        return progressProperty.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyBooleanProperty updatedProperty() {
        return updatedProperty.getReadOnlyProperty();
    }

    /*
     //return the first treeItem that contains the object. 
     private TreeItem getTreeItem(TreeItem root, Object object) {
     List<TreeItem> foundList = new ArrayList<>();
     Predicate<TreeItem> p = (TreeItem t) -> t.getValue() == object;
     Consumer<TreeItem> c = foundList::add;
     BreakCondition<TreeItem> b = p::test;
     MetaDataHierarchy.BFS(root, p, c, b);
     if (foundList.isEmpty()) {
     return null;
     }
     return foundList.get(0);
     }
     */
    // the node should be the parent of a destruct child
    private void clearEmptyFolderRec(TreeItem item) {
        if (item.getValue() instanceof MetaData && item.getChildren().isEmpty()) {
            TreeItem parent = item.getParent();
            removeItemFromTree(item);
            /*if (!resp) {
             ImageJFX.getLogger();
             }*/
            if (parent != null) {
                clearEmptyFolderRec(parent);
            }
        }
    }

    private void removeItemFromTree(TreeItem item) {
        if (item != null && item.getParent() != null) {

            item.getParent().getChildren().remove(item);

        }

    }

    //must be called only if the image has to move position
    private void updateImagePosition(PlaneDB image) {
        removeImageItem(planeNodeMap, image);
        placeImageInFolder(image, rootItem, planeNodeMap);
    }

    private void placeImageInFolder(PlaneDB image, TreeItem root, HashMap<PlaneDB, TreeItem> map) {
        TreeItem item = root;
        int i = 0;
        while (i < hierarchy.size()) {
            boolean goNext = false;
            for (Object childObject : item.getChildren()) {
                if (childObject instanceof TreeItem) {
                    TreeItem child = (TreeItem) childObject;
                    // if the metadata contained in the TreeItem is also contained in image metaDataset
                    if (child.getValue().equals(image.metaDataSetProperty().get(hierarchy.get(i)))) {
                        item = child;
                        goNext = true;
                        break;
                    }
                }
            }
            if (goNext) {
                i++;
            } else {
                break;
            }
        }
        if (i == hierarchy.size()) { // a suitable node exists for this image
            addImageItem(map, image, item);
        } else {
            createMetaDataItem(i, item, image);
        }
    }

    private void createMetaDataItem(int hierarchyIndex, TreeItem root, PlaneDB image) {
        TreeItem currentNode = root;
        while (hierarchyIndex < hierarchy.size() && imageContainsKey(image, hierarchy.get(hierarchyIndex))) {
            String currentKey = hierarchy.get(hierarchyIndex);
            TreeItem metaItem = new CheckBoxTreeItem(image.metaDataSetProperty().get(currentKey));
            addGraphicToItem(metaItem, ItemType.METADATA);
            currentNode.getChildren().add(metaItem);
            currentNode = metaItem;
            hierarchyIndex++;

        }
        addImageItem(planeNodeMap, image, currentNode);
    }

    private void addImageItem(HashMap<PlaneDB, TreeItem> map, PlaneDB image, TreeItem parent) {
        if (map.get(image) == null) {
            CheckBoxTreeItem imageItem = new CheckBoxTreeItem(image);
            addGraphicToItem(imageItem, ItemType.IMAGE);
            map.put(image, imageItem);
            parent.getChildren().add(imageItem);
            imageItem.selectedProperty().bind(image.selectedProperty());
        }
    }

    private void removeImageItem(HashMap<PlaneDB, TreeItem> map, PlaneDB image) {
        TreeItem item = map.get(image);
        if (item != null) {
            TreeItem parent = item.getParent();
            removeItemFromTree(item);
            if (parent != null) {
                clearEmptyFolderRec(parent);
            }
            map.remove(image);
        }
    }

    private boolean imageContainsKey(PlaneDB image, String key) {
        return image.getMetaDataSetProperty(PlaneDB.MODIFIED_METADATASET).containsKey(key);
    }

    private void addImage(PlaneDB newPlane) {
        ImageListeners il = new ImageListeners(newPlane);
        MapChangeListener<String, MetaData> listener = (MapChangeListener.Change<? extends String, ? extends MetaData> change) -> {
            handleMetaDataSetChange(change, newPlane);
        };
        il.setMetaDataListener(listener);
        imageListenersMap.put(newPlane, il);
        updateImagePosition(newPlane);

    }

    private void removeImage(PlaneDB removedPlane) {
        removeImageItem(planeNodeMap, removedPlane);
        imageListenersMap.get(removedPlane).stopListening();
    }

    @Override
    public void stopListening() {

    }

    private void handleMetaDataSetChange(MapChangeListener.Change<? extends String, ? extends MetaData> change, PlaneDB plane) {
        String changedKey = change.getKey();
        if (hierarchy.contains(changedKey)) {
            updateImagePosition(plane);
        }

    }

    private void handleHierarchyChange(ObservableValue<? extends ObservableList<String>> observable, ObservableList<String> oldValue, ObservableList<String> newValue) {
        createTree();
    }

    private void handlePlaneListChange(ListChangeListener.Change<? extends PlaneDB> c) {

        List<PlaneDB> addList = new ArrayList<>();
        List<PlaneDB> rmList = new ArrayList<>();
        while (c.next()) {
            if (c.wasAdded()) {
                for (PlaneDB added : c.getAddedSubList()) {
                    addList.add(added);
                }
            } else if (c.wasRemoved()) {
                for (PlaneDB removed : c.getRemoved()) {
                    rmList.add(removed);
                }
            }
        }
        Task<Void> taskAdd;
        Task<Void> taskRm;
        if (!addList.isEmpty()) {
            Consumer<PlaneDB> consumer = (PlaneDB t) -> {
                addImage(t);
            };
            taskAdd = createTask(addList, (PlaneDB t) -> addImage(t));
            if (addList.size() > NB_PLANE_THREAD) {
                updatedProperty.set(false);
                runTask(taskAdd);

            } else {
                taskAdd.run();
            }
        }
        if (!rmList.isEmpty()) {
            taskRm = createTask(rmList, (PlaneDB t) -> removeImage(t));
            if (rmList.size() > NB_PLANE_THREAD) {
                updatedProperty.set(false);
                runTask(taskRm);
            } else {
                taskRm.run();
            }
        }

    }

    private Task<Void> createTask(List<PlaneDB> planeList, Consumer<PlaneDB> c) {
        Task<Void> task = new Task<Void>() {

            @Override
            public Void call() throws Exception {
                int size = planeList.size();
                for (int i = 0; i < size; i++) {
                    PlaneDB plane = planeList.get(i);
                    c.accept(plane);
                    updateProgress(i, size);
                }
                return null;
            }
        };
        return task;
    }

    public class ImageListeners {

        private ChangeListener<Boolean> selectionListener;
        private MapChangeListener metaDataListener;
        private final PlaneDB image;

        private ImageListeners(PlaneDB image) {
            this.image = image;
        }

        private void setMetaDataListener(MapChangeListener listener) {
            stopListeningMetaData();
            this.metaDataListener = listener;
            image.metaDataSetProperty().addListener(listener);
        }

        private void stopListeningMetaData() {
            if (metaDataListener != null) {
                image.metaDataSetProperty().removeListener(metaDataListener);
            }
        }

        private void stopListening() {
            stopListeningMetaData();
        }
    }

    private void addGraphicToItem(TreeItem item, ItemType itemType) {
        FontAwesomeIconView iconView = new FontAwesomeIconView();
        switch (itemType) {
            case SELECTION:
                iconView.setGlyphName("CHECK");
                break;
            case METADATA:
                iconView.setGlyphName("FOLDER");
                break;
            case IMAGE:
                iconView.setGlyphName("IMAGE");

        }
        item.setGraphic(iconView);
    }

    private void runTask(Task task) {
         
        task.setOnRunning(new EventHandler() {

            @Override
            public void handle(Event event) {
               progressProperty.unbind();
                progressProperty.bind(task.progressProperty());
                updatingProperty.unbind();
                updatingProperty.bind(task.runningProperty());
            }
        });
        task.setOnSucceeded(new EventHandler() {

            @Override
            public void handle(Event event) {
                updatedProperty.set(true);
            }
        });

        //wait for the previous task to be done before running the new one. 
        ExecutorService executor = Executors.newFixedThreadPool(1);
        futureTask = executor.submit(task);
        //System.out.println(future.get());
                /* Thread th = new Thread(task);
         th.run();
         */
    }

}
