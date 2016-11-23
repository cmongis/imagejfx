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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import static mongis.utils.panecell.ScrollWindowEvent.SCROLL_WINDOW_ENTERED;
import static mongis.utils.panecell.ScrollWindowEvent.SCROLL_WINDOW_EXITED;

/**
 * This class takes a Parent and a @Scrollpane and takes care of notifying the
 * children when they are shown.
 *
 * @author Cyril MONGIS, 2016
 */
public class ScrollBinder {

    //public static EventType SCROLL_WINDOW_EXITED = new EventType(EventType.ROOT,"SCROLL_WINDOW_EXITED");
    protected ObservableList<Node> visiblesNode = FXCollections.observableArrayList();

    protected final ScrollPane scrollPane;

    public ScrollBinder(ScrollPane pane) {
        scrollPane = pane;

        // binding the scroll pane
        scrollPane.hvalueProperty().addListener(this::onScroll);
        scrollPane.vvalueProperty().addListener(this::onScroll);

        //
        visiblesNode.addListener(this::onListChange);

        
        update();
    }

    public void update() {
        onScroll(null, null, scrollPane.hminProperty().doubleValue());
    }
    
    private void onListChange(ListChangeListener.Change<? extends Node> change) {
        while (change.next()) {
            for (Node added : change.getAddedSubList()) {
                added.fireEvent(new ScrollWindowEvent(SCROLL_WINDOW_ENTERED));
            }
            for (Node removed : change.getRemoved()) {
                removed.fireEvent(new ScrollWindowEvent(SCROLL_WINDOW_EXITED));
            }
        }

    }

    
    
    public void onScroll(Observable observable, Number oldValue, Number newValue) {

        double minX = scrollPane.getHvalue() * scrollPane.getContent().getBoundsInLocal().getWidth();
        double minY = scrollPane.getVvalue() * scrollPane.getContent().getBoundsInLocal().getHeight();
        Bounds scrollWindow = new BoundingBox(minX, minY+10, scrollPane.getWidth(), scrollPane.getHeight());

        Parent parentNode = (Parent) scrollPane.getContent();
        
        List<Node> toAdd = new ArrayList<>();
        List<Node> toRemove = new ArrayList<>();
        
        parentNode.getChildrenUnmodifiable().forEach(child -> {
            Bounds boundsInParent = child.getBoundsInParent();

            // if the child is inside the scroll window then we add it
            // to the list of visible nodes (if not added already);
            if (scrollWindow.intersects(boundsInParent)) {
                if (!visiblesNode.contains(child)) {

                    toAdd.add(child);
                }

                // if it doesn't belong to the visible node window,
                // then it's remove from the visible node list.
            } else if (visiblesNode.contains(child)) {

                toRemove.remove(child);
            }
        });
        
        visiblesNode.addAll(toAdd);
        visiblesNode.removeAll(toRemove);
        
    }
}
