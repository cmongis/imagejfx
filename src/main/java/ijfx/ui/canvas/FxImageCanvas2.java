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
package ijfx.ui.canvas;

import ijfx.ui.tool.overlay.RectangleRepresentation;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import net.imagej.overlay.RectangleOverlay;

/**
 *
 * @author cyril
 */
public class FxImageCanvas2 extends ScrollPane {

    StackPane pane = new StackPane();
    AnchorPane anchorPane = new AnchorPane();

    Image image = new Image("file:/Users/cyril/Pictures/HD-Wallpapers1_QlwttNW.jpeg");
    ImageView view = new ImageView(image);

    DoubleProperty zoomProperty = new SimpleDoubleProperty(1.0);
    DoubleBinding invertZoomProperty = Bindings.createDoubleBinding(() -> 1.0 / zoomProperty.getValue(), zoomProperty);

    public FxImageCanvas2() {
        setContent(anchorPane);
        anchorPane.setPrefSize(600, 500);
        //anchorPane.getChildren().add(pane);

        RectangleOverlay overlay = new RectangleOverlay();
        overlay.setOrigin(20, 0);
        overlay.setOrigin(50, 1);
        overlay.setExtent(300, 0);
        overlay.setExtent(70, 1);

        RectangleRepresentation r = new RectangleRepresentation();
        r.zoomProperty().bind(zoomProperty);

        anchorPane.getChildren().addAll(r.getRepresentation());
        r.getRepresentation().forEach(node -> {
            node.setTranslateX(200);
            node.setTranslateY(300);
        });
        ;
        r.updateFrom(overlay);
        
        // pane.getChildren().add(view);
        // pane.getChildren().addAll(r.getNode());
        //r.getNode().setLayoutX(20);
        //r.getNode().setLayoutY(30);
        //pane.addEventHandler(MouseEvent.MOUSE_MOVED, this::onMouseWheel);

        // pane.setOnScroll(this::onScreelEvent);
        // pane.scaleXProperty().bind(invertZoomProperty);
        //pane.scaleYProperty().bind(invertZoomProperty);
    }

    public void onScreelEvent(ScrollEvent event) {

        double newZoom = zoomProperty.getValue();

        if (event.getDeltaY() > 0) {
            newZoom = newZoom * 0.95;
        } else {
            newZoom = newZoom * 1.05;
        }

        zoomProperty.setValue(newZoom);
        System.out.println(pane.getLayoutBounds().getWidth());
    }

}
