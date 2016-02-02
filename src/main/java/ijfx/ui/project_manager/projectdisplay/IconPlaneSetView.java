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
package ijfx.ui.project_manager.projectdisplay;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import mercury.core.MercuryTimer;
import mongis.utils.FXUtilities;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class IconPlaneSetView extends BorderPane implements PlaneSetView{

    FlowPane flowPane = new FlowPane();
    
    TreeItem<PlaneOrMetaData> currentItem;
    
    PlaneSet planeSet;
    
    ObservableList<IconItem> itemControllerList = FXCollections.observableArrayList();
    LinkedList<IconItem> cachedControllerList = new LinkedList<>();
    
    
    @Parameter
    Context context;
    
    public IconPlaneSetView(Context context) {
        super();
        context.inject(this);
        flowPane.prefWidthProperty().bind(widthProperty());
        FXUtilities.bindList(flowPane.getChildren(), itemControllerList);
        setCenter(flowPane);
    }
    
    @Override
   public void setCurrentItem(TreeItem<PlaneOrMetaData> item) {
       
        System.out.println(item);
        if(item == null) return;
        ArrayList<TreeItem<?>> items = new ArrayList<>();
        items.addAll(item.getChildren());
        update(items);
    }

    @Override
    public void setHirarchy(List<String> hierarchy) {
        
    }

    @Override
    public Node getIcon() {     
        return new FontAwesomeIconView(FontAwesomeIcon.TH);
    }
    
    
    
    private void update(List<TreeItem<?>> items) {

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

    @Override
    public TreeItem<PlaneOrMetaData> getCurrentItem() {
        return currentItem;
    }

    @Override
    public void setCurrentPlaneSet(PlaneSet planeSet) {
        this.planeSet = planeSet;
    }

    @Override
    public PlaneSet getCurrentPlaneSet() {
        return planeSet;
    }

    @Override
    public Node getNode() {
        return this;
    }
}
