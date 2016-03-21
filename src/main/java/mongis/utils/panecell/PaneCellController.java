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
package mongis.utils.panecell;

import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import mercury.core.MercuryTimer;

/**
 *
 * @author cyril
 */
public class PaneCellController<T> {

    ObservableList<T> items = FXCollections.observableArrayList();

    LinkedList<PaneCell<T>> itemControllerList = new LinkedList<PaneCell<T>>();
    LinkedList<PaneCell<T>> cachedControllerList = new LinkedList<>();

    Callable<PaneCell<T>> cellFactory;

    Logger logger = ImageJFX.getLogger();

    Pane pane;

   
    
    public PaneCellController(Pane pane) {
        setPane(pane);
        
        
        
    }

    public void setPane(Pane pane) {
        this.pane = pane;
    }

    public void setCellFactory(Callable<PaneCell<T>> cellFactory) {
        this.cellFactory = cellFactory;
    }

    public synchronized void update(List<T> items) {

        try {

            int layoutObjectCount = itemControllerList.size();
            int itemCount = items.size();

            MercuryTimer timer = new MercuryTimer("Browser view");

            logger.info(String.format("%d items to show against %s controllers ", itemCount, itemControllerList.size()));

            // if there is more controllers than items
            if (itemCount < layoutObjectCount) {

                // we create an list to contain all controllers that have to be removed
                List<PaneCell<T>> toRemove = new ArrayList<>(layoutObjectCount - itemCount); // creating the missing controller the new objects

                // putting the excess of controllers in a blacklis
                for (int i = itemCount; i < layoutObjectCount; i++) {

                    // System.out.println("Removing :" +i);
                    toRemove.add(itemControllerList.get(i));

                }

                // adding these controllers to the cache
                cachedControllerList.addAll(toRemove);
                

                logger.info(String.format("Deleting %d controllers", toRemove.size()));

                // removing from the list
                itemControllerList.removeAll(toRemove);
               
                pane.getChildren().removeAll(getContent(toRemove));
                
                cachedControllerList.stream().forEach(item->item.setItem(null));
                
                logger.info(String.format("%d controllers left.", itemControllerList.size()));
            }

            timer.elapsed("controller deleting");

            // if there is more items than controllers
            if (layoutObjectCount < itemCount) {

                //creating a list that should contain the controllers to add
                List<PaneCell<T>> toAdd = new ArrayList<>(itemCount - layoutObjectCount);

                // adding extra controllers
                for (int i = layoutObjectCount; i < itemCount; i++) {

                    PaneCell itemController;

                    // if there is no more controllers in the cache
                    if (cachedControllerList.size() == 0) {
                        
                        fillCache(itemCount - i);
                    }
                    // we pop the first
                    itemController = cachedControllerList.pop();

                    itemController.setItem(items.get(i));

                    //adding the controller to the add list
                    toAdd.add(itemController);
                }
                timer.elapsed("controller fetching");
                // adding all the controllers
                itemControllerList.addAll(toAdd);
                pane.getChildren().addAll(getContent(toAdd));
                timer.elapsed("controller adding");
            }

            // updating the controllers with the new data
            for (int i = 0; i < itemCount; i++) {

                if (i >= itemControllerList.size()) {
                    break;
                }
                itemControllerList.get(i).setItem(items.get(i));
            }

            timer.elapsed("controller update");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Couln't update the controllers", e);
        }
    }

    public Collection<Node> getContent(Collection<PaneCell<T>> cellList) {
        return cellList.stream().map(PaneCell::getContent).collect(Collectors.toList());
    }

    private void fillCache(int number) {

        cachedControllerList.addAll(IntStream.range(0, number).parallel().mapToObj(n -> createPaneCell()).collect(Collectors.toList()));

    }

    private PaneCell<T> createPaneCell() {
        try {
            return cellFactory.call();
        } catch (Exception ex) {
            Logger.getLogger(PaneCellController.class.getName()).log(Level.SEVERE, "Error when creating cell", ex);
        }
        return null;
    }

    /*
    private class PaneCellSelectionModel<T> extends MultipleSelectionModel<T> {

        ObservableList<Integer> selectedIndices = FXCollections.observableArrayList();
        
        ObservableList<T> selectedItems = FXCollections.observableArrayList();
        
        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return selectedIndices;
        }

        @Override
        public ObservableList<T> getSelectedItems() {
            return selectedItems;
        }

        @Override
        public void selectIndices(int index, int... indices) {
            
            //int[] all = new int[1+indices.length];
            int[] all = ArrayUtils.addAll(indices,index);
            
            
            List<T> items = IntStream.of(all)
                    .mapToObj(i->itemControllerList.get(i).getItem())
                    .
        }

        @Override
        public void selectAll() {
        }

        @Override
        public void selectFirst() {
        }

        @Override
        public void selectLast() {
        }

        @Override
        public void clearAndSelect(int index) {
        }

        @Override
        public void select(int index) {
        }

        @Override
        public void select(T obj) {
        }

        @Override
        public void clearSelection(int index) {
        }

        @Override
        public void clearSelection() {
        }

        @Override
        public boolean isSelected(int index) {
        }

        @Override
        public boolean isEmpty() {
        }

        @Override
        public void selectPrevious() {
        }

        @Override
        public void selectNext() {
        }
        
    }*/
    
}
