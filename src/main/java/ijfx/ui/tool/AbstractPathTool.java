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
package ijfx.ui.tool;

import ijfx.ui.canvas.FxImageCanvas;
import ijfx.ui.main.ImageJFX;
import ijfx.service.display.DisplayRangeService;
import ijfx.service.overlay.OverlaySelectionService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import net.imagej.DatasetService;
import net.imagej.axis.AxisType;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.lut.LUTService;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PolygonOverlay;
import net.imglib2.RealPoint;
import org.scijava.Context;
import org.scijava.display.DisplayService;

import org.scijava.plugin.Parameter;
import org.scijava.util.ColorRGB;

/**
 * Abstract class that eases creation of path based tools.
 * 
 * The listeners are setup so the tool listen
 * when the user pressed the mouse to draw.
 * The interaction of the user is saved in the currentPath (@FxPath)
 * variable.
 * 
 * Abstract methods are called before, during and after drawing and it comes
 * to the daughter class to draw or modify the Canvas.
 * 
 * Check @Hand and @FreeHand for examples of implementations.
 * 
 * 
 * @author Cyril MONGIS, 2015
 */
public abstract class AbstractPathTool implements FxTool {

    private FxPath currentPath;
    private FxImageCanvas canvas;
    private ToggleButton button;
   

    @Parameter
    protected DefaultFxToolService toolService;

    @Parameter
    private DisplayService displayService;
    @Parameter
    private ImageDisplayService imageDisplayService;

    @Parameter
    private DatasetService datasetService;
    
    @Parameter
    private OverlayService overlayService;

    @Parameter
    private OverlaySelectionService overlaySelectionService;
    
    @Parameter
    LUTService lutService;

    @Parameter
    DisplayRangeService displayRangeService;

    @Parameter
    Context context;
    
    private EventHandler<MouseEvent> onMouseDragged = this::onMouseDragged;
    private EventHandler<MouseEvent> onMouseReleased = this::onMouseReleased;
    private EventHandler<MouseEvent> onMouseClicked = this::onClick;
    private EventHandler<MouseEvent> onMouseMoved = this::onMouseMoved;

    
    
    public FxImageCanvas getCanvas() {
        return canvas;
    }

    public void onMousePressed(DragEvent event) {
        currentPath = new FxPath();
        elongPath(event);
        beforeDrawing(currentPath);
    }
    public void onMouseReleased(MouseEvent event) {
        if(currentPath != null && currentPath.size() > 0)
        afterDrawing(currentPath);

        currentPath = null;
    }
    public void onMouseDragged(MouseEvent event) {
        elongPath(event);
        event.consume();
        duringDrawing(currentPath);
    }
    
    public void onMouseClicked(MouseEvent event) {
        
    }
    
    public void onMouseMoved(MouseEvent event) {
        
    }

    public void addOverlays(final Overlay... overlays) {
        ImageDisplay display = displayService.getActiveDisplay(ImageDisplay.class);
        final ArrayList<Overlay> list = new ArrayList<>();
        Collections.addAll(list, overlays);

        for (Overlay overlay : overlays) {
            final int dimCount = getActiveImageDisplay().numDimensions();
            final AxisType[] axes = new AxisType[dimCount];

            for (int i = 0; i != dimCount; i++) {
                final long pos = getActiveImageDisplay().getLongPosition(i);
                overlay.setAxis(getActiveImageDisplay().axis(i), i);

            }

        }
        ImageJFX.getThreadQueue().execute(() -> {

            double rangeMin = displayRangeService.getCurrentViewMinimum();
            double rangeMax = displayRangeService.getCurrentViewMaximum();

            overlayService.addOverlays(display, list);
            if(list.size() == 1) {
                overlaySelectionService.setOverlaySelection(display, list.get(0), true);
            }
            displayRangeService.updateCurrentDisplayRange(rangeMin, rangeMax);
        });
    }

    
    public void addOverlay(FxPath path) {
        
        List<Point2D> pathOnImage = path.getPathOnImage();
        
         PolygonOverlay overlay = new PolygonOverlay(context);
        
        overlay.setName("new overlay");
        overlay.setFillColor(new ColorRGB(244,14,14));
        overlay.setLineColor(new ColorRGB(0,0,244));
        
        ImageDisplay display = displayService.getActiveDisplay(ImageDisplay.class);
        
        
        long[] position =  new long[display.numDimensions()];
        
        display.localize(position);
                 for(int i = 2; i!= display.numDimensions();i++) {
                     overlay.setAxis(imageDisplayService.getActiveDataset().axis(i), i);
                     
                 }


        
        for(int i = 0;i!=pathOnImage.size();i++) {
            Point2D p = pathOnImage.get(i);
            
            double[] vertex = new double[display.numDimensions()];
            
            vertex[0] = p.getX();
            vertex[1] = p.getY();
            
            for(int j= 2;j!=position.length;j++) {
                vertex[j] = position[j];
            }
            
            Arrays.toString(vertex);
            
            overlay.getRegionOfInterest().addVertex(i, new RealPoint(vertex));
        }
        
        addOverlays(overlay);
    }
  

    public FxPath getCurrentPath() {
        if (currentPath == null) {
            currentPath = new FxPath();
        }
        return currentPath;
    }

    protected void drawPath(FxPath fxPath) {
         List<Point2D> points = fxPath.getPathOnScreen();

        double[] xList = FxPath.xList(points);
        double[] yList = FxPath.yList(points);

        getCanvas().repaint();
        getCanvas().getGraphicsContext2D().setStroke(Color.YELLOW);
        getCanvas().getGraphicsContext2D().setLineWidth(1.0);
        getCanvas().getGraphicsContext2D().strokePolygon(xList, yList, xList.length);
    }
    
    
    public void elongPath(MouseEvent event) {

        //System.out.println(getCurrentPath().getPathOnScreen().size());
        Point2D cursorPosition = new Point2D(event.getX(), event.getY());

        Point2D onImage = canvas.getPositionOnImage(cursorPosition);
        getCurrentPath().newPoint(cursorPosition, onImage);

    }
    
    public void elongPath(MouseEvent event, FxPath path) {
        
          Point2D cursorPosition = new Point2D(event.getX(), event.getY());

        Point2D onImage = canvas.getPositionOnImage(cursorPosition);
        path.newPoint(cursorPosition, onImage);
    }

    public void elongPath(DragEvent event) {
        Point2D cursorPosition = new Point2D(event.getX(), event.getY());
        Point2D onImage = canvas.getPositionOnImage(cursorPosition);
        getCurrentPath().newPoint(cursorPosition, onImage);

    }

    @Override
    public void subscribe(FxImageCanvas canvas) {
        this.canvas = canvas;
        //canvas.addEventHandler(DragEvent.DRAG_ENTERED, this::onMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleased);
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, onMouseClicked);
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED,onMouseMoved);
        onActivated();
        //canvas.addEventHandler(MouseDragEvent.MOUSE_DRAGGED,event->event.consume());
    }

    @Override
    public void unsubscribe(FxImageCanvas canvas) {
     
        //canvas.removeEventHandler(DragEvent.DRAG_ENTERED, this::onMousePressed);
        canvas.removeEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDragged);
        canvas.removeEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleased);
        canvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, onMouseClicked);
        canvas.removeEventHandler(MouseEvent.MOUSE_MOVED,onMouseMoved);
    }

    public Node getNode() {
        if (button == null) {
            button = new ToggleButton(getClass().getSimpleName().replace("Tool", ""), getIcon());
            //button.getStyleClass().add(TOOL_BUTTON_CSS_CLASS);
            button.setOnAction(this::onButtonClick);
        }
        return button;
    }

    public void update(FxTool tool) {
        button.setSelected(tool == this);
    }

    @org.scijava.event.EventHandler
    public void handleEvent(ToolChangeEvent event) {
        update(event.getTool());
    }

    private void onButtonClick(ActionEvent event) {
        toolService.setCurrentTool(this);
    }
    
    public void onActivated() {
        
    }

    public abstract void beforeDrawing(FxPath path);

    public abstract void duringDrawing(FxPath fxPath);

    public abstract void afterDrawing(FxPath path);

    public abstract void onClick(MouseEvent event);

    public abstract Node getIcon();

    public Rectangle2D getRectangle(Point2D p1, Point2D p2) {

        double x;
        double y;

        double w;
        double h;

        if (p1.getX() < p2.getX()) {
            x = p1.getX();
        } else {
            x = p2.getX();
        }

        if (p1.getY() < p2.getY()) {
            y = p1.getY();
        } else {
            y = p2.getY();
        }

        w = Math.abs(p1.getX() - p2.getX());
        h = Math.abs(p1.getY() - p2.getY());

        return new Rectangle2D(x, y, w, h);

    }

    protected ImageDisplay getActiveImageDisplay() {
        return displayService.getActiveDisplay(ImageDisplay.class);
    }

    protected DisplayService displayService() {
        return displayService;
    }

    protected ImageDisplayService imageDisplayService() {
        return imageDisplayService;
    }
    
    
    
}
