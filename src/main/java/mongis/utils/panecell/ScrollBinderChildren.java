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

import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import mongis.utils.panecell.ScrollBinder;

/**
 * In case there are many Parent in the pane
 *
 * @author Tuan anh TRINH
 */
public class ScrollBinderChildren extends ScrollBinder {

    public ScrollBinderChildren(ScrollPane pane) {
        super(pane);
    }

    @Override
    public void onScroll(Observable observable, Number oldValue, Number newValue) {

        double minX = scrollPane.getHvalue() * scrollPane.getContent().getBoundsInLocal().getWidth();
        double minY = scrollPane.getVvalue() * scrollPane.getContent().getBoundsInLocal().getHeight();
        Bounds scrollWindow = new BoundingBox(minX, minY, scrollPane.getWidth(), scrollPane.getHeight());

        Pane pane = (Pane) scrollPane.getContent();
        ObservableList<Parent> parentNode = (ObservableList<Parent>) (ObservableList<?>) pane.getChildrenUnmodifiable();

        parentNode.stream().forEach(parent -> {
            parent.getChildrenUnmodifiable().forEach(child -> {
                Bounds boundsInParent = child.getBoundsInParent();

                // if the child is inside the scroll window then we add it
                // to the list of visible nodes (if not added already);
                if (scrollWindow.intersects(boundsInParent)) {
                    if (!visiblesNode.contains(child)) {

                        visiblesNode.add(child);
                    }

                    // if it doesn't belong to the visible node window,
                    // then it's remove from the visible node list.
                } else if (visiblesNode.contains(child)) {

                    visiblesNode.remove(child);
                }
            });
        });
    }

}
