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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import mercury.core.MercuryTimer;
import mongis.utils.AsyncCallback;
import mongis.utils.properties.ServiceProperty;

/**
 * The PaneController takes care of filling a Pane with PaneCells. Like a
 * ListView, PaneCells are just container that update themselves when changes of
 * the model occurs. The PaneController cache used PaneCell and create new when
 * necessary. When the list of items change, the PaneCellController update the
 * children nodes of the associated Pane.
 *
 * @author Cyril MONGIS
 */
public class PaneCellController<T extends Object> {

    private List<T> currentItems;
    private LinkedList<PaneCell<T>> itemControllerList = new LinkedList<PaneCell<T>>();
    private LinkedList<PaneCell<T>> cachedControllerList = new LinkedList<>();

    private Callable<PaneCell<T>> cellFactory;

    private Logger logger = ImageJFX.getLogger();

    private Pane pane;

    private ObservableSet<T> selectedItems = FXCollections.observableSet();

    public PaneCellController(Pane pane) {
        setPane(pane);
    }

    /**
     * Set the pane that should be updated by the controller
     *
     * @param pane
     */
    public void setPane(Pane pane) {
        this.pane = pane;
    }

    public void setCellFactory(Callable<PaneCell<T>> cellFactory) {
        this.cellFactory = cellFactory;
    }

    /**
     * Give it a list of items coming from the model and the controller will
     * update the pane. If necessary, new PanelCell will be created. Unnecessary
     * PaneCells will be cached.
     *
     * @param items List of items coming from the model
     */
    public synchronized void update(List<T> items) {

        new AsyncCallback<Integer, List<PaneCell<T>>>()
                .setInput(items.size())
                .run(this::retrieve)
                .then(controllers -> {
                    MercuryTimer timer = new MercuryTimer("Browser view");
                    timer.start();
                    pane.getChildren().clear();
                    pane.getChildren().addAll(getContent(controllers));
                    timer.elapsed("Adding all the controllers");
                    for (int i = 0; i != items.size(); i++) {
                        controllers.get(i).setItem(items.get(i));
                    }
                    timer.elapsed("Updating all the controllers");

                })
                .start();

    }

    public synchronized void update2DList(List<List<T>> items, int size) {
        new AsyncCallback<Integer, List<PaneCell<T>>>()
                .setInput(size+1)
                .run(this::retrieve)
                .then(controllers -> {
                    MercuryTimer timer = new MercuryTimer("Browser view");
                    timer.start();
                    pane.getChildren().clear();
                       int cpt = 0;
                    for (int i = 0; i < items.size(); i++) {
                        for (int j = 0; j < items.get(i).size(); j++) {
                            cpt++;
                            ((GridPane) pane).add(controllers.get(cpt).getContent(), i, j);
                            controllers.get(cpt).setItem(items.get(i).get(j));
                                
                        
                        }
                    }
                    timer.elapsed("Updating2D all the controllers");
//                    for (int i = 0; i < items.size(); i++) {
//                        for (int j = 0; j < items.get(i).size(); j++) {
//                            controllers.get(j*items.size() +i).setItem(items.get(i).get(j));
//                        }
//                    }
                })
                .start();

    }

    private List<PaneCell<T>> retrieve(Integer number) {

        MercuryTimer timer = new MercuryTimer("Browser view");
        int cacheSize = cachedControllerList.size();
        int missingControllers = number - cacheSize;
        timer.start();
        if (missingControllers > 0) {
            fillCache(missingControllers);
        }
        timer.elapsed("Time to fetch controllers");
        return cachedControllerList.subList(0, number);
    }

    /*
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

                cachedControllerList.stream().forEach(item -> item.setItem(null));

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
            
            
            currentItems = items;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Couln't update the controllers", e);
        }
    }*/
    // get the list of cells
    protected Collection<Node> getContent(Collection<PaneCell<T>> cellList) {
        return cellList.stream().map(PaneCell::getContent).collect(Collectors.toList());
    }

    // fills the cache by creating a certain number of cells
    private void fillCache(int number) {

        cachedControllerList.addAll(IntStream.range(0, number + 1).parallel().mapToObj(n -> createPaneCell()).collect(Collectors.toList()));

    }

    PseudoClass SELECTED_PSEUDO_CLASS = new PseudoClass() {
        private final static String SELECTED = "selected";

        @Override
        public String getPseudoClassName() {
            return SELECTED;
        }
    };

    // creates a pane cell
    private PaneCell<T> createPaneCell() {
        try {

            PaneCell cell = cellFactory.call();

            return cell;

        } catch (Exception ex) {
            Logger.getLogger(PaneCellController.class.getName()).log(Level.SEVERE, "Error when creating cell", ex);
        }
        return null;
    }

    private class CellClickHandler implements EventHandler<MouseEvent> {

        PaneCell<T> cell;
        private final static long DOUBLE_CLICK_INTERVAL = 1000;
        long lastClick;

        @Override
        public void handle(MouseEvent event) {

            long now = System.currentTimeMillis();

            if (now - lastClick <= DOUBLE_CLICK_INTERVAL) {

            } else {
                setSelected(cell.getItem(), Boolean.TRUE);
            }

        }

    }

    public Boolean isSelected(T item) {
        return selectedItems.contains(item);
    }

    public void setSelected(T item, Boolean selection) {
        if (selection) {
            selectedItems.add(item);
        } else {
            selectedItems.remove(item);
        }
        Platform.runLater(this::updateSelection);
    }

    public void select(List<T> items) {
        selectedItems.addAll(items);
        Platform.runLater(this::updateSelection);
    }

    public void unselected(List<T> items) {
        Platform.runLater(this::updateSelection);
    }

    public Property<Boolean> getSelectedProperty(T item) {
        return new ServiceProperty<>(item, this::setSelected, this::isSelected);
    }

    public void updateSelection() {
        itemControllerList.forEach(this::updateSelection);
    }

    public void updateSelection(PaneCell<T> cell) {
        cell.getContent().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, isSelected(cell.getItem()));
    }

    public List<T> getItems() {
        return currentItems;
    }

    public List<PaneCell> getCells() {
        return pane
                .getChildren()
                .stream()
                .map(child -> (PaneCell) child)
                .collect(Collectors.toList());
    }
}
