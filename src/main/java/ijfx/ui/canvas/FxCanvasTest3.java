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

import ijfx.ui.datadisplay.image.overlay.RectangleOverlayHelper;
import ijfx.ui.datadisplay.image.overlay.RectangleOverlayDrawer;
import ijfx.ui.canvas.utils.CanvasCamera;
import ijfx.ui.tool.overlay.MoveablePoint;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import net.imagej.overlay.RectangleOverlay;

/**
 *
 * @author cyril
 */
public class FxCanvasTest3 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        AnchorPane pane = new AnchorPane();
        pane.setPrefSize(400, 400);
        FxImageCanvas canvas = new FxImageCanvas();

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        canvas.setImage(new Image("file:/Users/cyril/Pictures/HD-Wallpapers1_QlwttNW.jpeg"));

        RectangleOverlay rectangleOverlay = new RectangleOverlay();

        rectangleOverlay.setOrigin(800, 0);
        rectangleOverlay.setOrigin(900, 1);
        rectangleOverlay.setExtent(50, 0);
        rectangleOverlay.setExtent(50, 1);

        pane.getChildren().add(canvas);
        

        CanvasCamera canvasCamera = canvas.getCamera();
        
        // helper to update the overlay model
        RectangleOverlayHelper helper = new RectangleOverlayHelper(rectangleOverlay);
        
        // point that represent the overlay
        MoveablePoint p1 = new MoveablePoint(canvas.getCamera());
        MoveablePoint p2 = new MoveablePoint(canvas.getCamera());
        
        // each time the point change the position on the image it update the model
        helper.minEdgeProperty.bind(p1.positionOnImagePropety());
        helper.maxEdgeProperty.bind(p2.positionOnImagePropety());
       
        // setting the position on the screen of the first point
        p1.positionOnScreenProperty().setValue(new Point2D(20,20));
        p2.positionOnScreenProperty().setValue(new Point2D(50,50));
        
        pane.getChildren().addAll(p1,p2);
        
        Scene scene = new Scene(pane);
        canvas.repaint();
        primaryStage.setScene(scene);
        primaryStage.show();

        
        // now let's take care of overlay representation indepentenly of interaction
        RectangleOverlayDrawer drawer = new RectangleOverlayDrawer(this);

        Runnable paintOverlay = () -> {
            Node node = drawer.update(rectangleOverlay, canvasCamera);
            if (pane.getChildren().contains(node) == false) {
                pane.getChildren().add(node);
            }
        };

        ChangeListener<Point2D> pointChangeListener = (obs,oldValue,newValue)->paintOverlay.run();
        
        p1.positionOnScreenProperty().addListener(pointChangeListener);
        p2.positionOnScreenProperty().addListener(pointChangeListener);
        
        canvas.getCamera().addListener(camera -> {
            paintOverlay.run();
        });

        
        
    }

    public static void main(String... args) {
        launch(args);
    }

    public static class CameraBinding {

        public static void bind(final CanvasCamera camera, Property<Point2D> pointOnCamera, Property<Point2D> pointOnImage) {
            pointOnCamera.addListener((obs, oldv, newv) -> {
                pointOnImage.setValue(camera.getPositionOnImage(newv));
            });
        }

    }



}
