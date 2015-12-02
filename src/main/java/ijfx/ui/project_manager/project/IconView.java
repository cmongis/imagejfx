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

import ijfx.core.metadata.MetaData;
import ijfx.core.project.ProjectManagerService;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import mercury.core.MercuryTimer;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class IconView extends StackPane {

    Logger logger = ImageJFX.getLogger();

    @Parameter
    ProjectViewModelService viewModelService;

    @Parameter
    ProjectManagerService projectService;

    @Parameter
    Context context;

    ProjectViewModel projectViewModel;

    
    @FXML
    FlowPane flowPane;

   
    LinkedList<IconItem> cachedControllerList = new LinkedList<>();

    public IconView(Context context) {
        super();

        context.inject(this);

        try {
            FXUtilities.injectFXML(this);


        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);

            getChildren().add(new Label("Error when initializing"));

        }

        init();

    }

    ObservableList<IconItem> itemControllerList = FXCollections.observableArrayList();

    public void init() {

        flowPane.prefWrapLengthProperty().bind(widthProperty());

        setViewModel(viewModelService.getViewModel(projectService.currentProjectProperty().get()));

        bindList(flowPane.getChildren(), itemControllerList);

        onTreeItemChanged(null, null, projectViewModel.nodeProperty().get());

    }

    public <T> void bindList(final ObservableList<Node> list, final ObservableList<IconItem> subList) {

        subList.addListener((Change<? extends IconItem> c) -> {


            while (c.next()) {



                list.removeAll(c.getRemoved());
                list.addAll(c.getAddedSubList());
                
                /*
                if (c.wasPermutated()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        //permutate
                    }
                } else if (c.wasUpdated()) {
                    //update item
                } else {

                    //for (IconItem remitem : c.getRemoved()) {
                    //if (list.contains(c.getRemoved())) {
                    //    list.remove(remitem);
                    //}
                    //}
                    //for (IconItem additem : c.getAddedSubList()) {
                    //  list.add(additem);
                    //}
                }*/

            }
        });

    }

    public void update(List<TreeItem> items) {

        int layoutObjectCount = itemControllerList.size();
        int itemCount = items.size();

        
        MercuryTimer timer = new MercuryTimer("Browser view");
        
        // if there is more controllers than items
        if (itemCount < layoutObjectCount) {

            // we create an list to contain all controllers that have to be removed
            List<IconItem> toRemove = new ArrayList<>(layoutObjectCount - itemCount); // creating the missing controller the new objects

            // putting the excess of controllers in a blacklis
            for (int i = itemCount; i < layoutObjectCount; i++) {

                // System.out.println("Removing :" +i);
                toRemove.add(itemControllerList.get(i));

            }

            // adding these controllers to the cache
            cachedControllerList.addAll(toRemove);

            // removing from the list
            itemControllerList.removeAll(toRemove);
        }
        
        timer.elapsed("controller deleting");
        
        // if there is more items than controllers
        if (layoutObjectCount < itemCount) {

            //creating a list that should contain the controllers to add
            List<IconItem> toAdd = new ArrayList<>(itemCount - layoutObjectCount);

            // adding extra controllers
            for (int i = layoutObjectCount; i < itemCount; i++) {

                IconItem treeItemController;

                // if there is controllers in the cache
                if (cachedControllerList.size() > 0) {
                    
                    // we pop the first
                    treeItemController = cachedControllerList.pop();

                } // otherwise, we create a new one
                else {
                    treeItemController = new IconItem(items.get(i));
                    context.inject(treeItemController);
                }

                //adding the controller to the add list
                toAdd.add(treeItemController);
            }
            timer.elapsed("controller fetching");
            // adding all the controllers
            itemControllerList.addAll(toAdd);
            timer.elapsed("controller adding");
        }
           
        
        
           
        // updating the controllers with the new data
        for (int i = 0; i < layoutObjectCount; i++) {
            
            if(i >= itemControllerList.size()) break;
            itemControllerList.get(i).setItem(items.get(i));

        }
        
        timer.elapsed("controller update");

    }

    public void setViewModel(ProjectViewModel projectViewModel) {
        this.projectViewModel = projectViewModel;

        projectViewModel.nodeProperty().addListener(this::onTreeItemChanged);

    }

    public void onTreeItemChanged(Observable obs, CheckBoxTreeItem oldValue, CheckBoxTreeItem newValue) {

        ItemWrapper item = new ItemWrapper(newValue);



        //List<TreeItem> items = new ArrayList<TreeItem>(newValue.getChildren().size());

        //if (newValue.isLeaf() == false) {
            //GenericMetaData metadata = item.getValue();
            update(newValue.getChildren());

            /*
             newValue.getChildren().forEach(child -> {
             // System.out.println("adding item "+child.toString());
             itemListView.getItems().add(child);
             });*/
            //System.out.println(itemListView.getItems().size());
        //}
        //else {
            
        //}
        
      
    }

    private class ItemWrapper {

        TreeItem item;

        public ItemWrapper(TreeItem item) {
            this.item = item;
        }

        public boolean isFolder() {
            return MetaData.class.isAssignableFrom(item.getValue().getClass());
        }

        public boolean isPlane() {
            return !isFolder();
        }

        public <T> T getValue() {
            return (T) item.getValue();
        }

    }

   

}
