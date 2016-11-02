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
package ijfx.ui.tool;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.ui.canvas.FxImageCanvas;
import ijfx.ui.canvas.utils.CanvasCamera;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;

import net.imagej.display.OverlayService;
import net.imagej.event.OverlayCreatedEvent;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PointOverlay;
import org.scijava.Context;
import org.scijava.display.DisplayService;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = FxTool.class)
public class PointTool implements FxTool {

    @Parameter
    DisplayService imageDisplayService;

    @Parameter
    FxToolService toolService;

    @Parameter
    Context context;

    @Parameter
    EventService eventService;

    @Parameter
    OverlayService overlayService;

    private ToggleButton button;

    EventHandler<MouseEvent> onMouseClick;

    public PointTool() {
        onMouseClick = this::onClick;
    }

    public Node getNode() {
        if (button == null) {
            button = new ToggleButton(getClass().getSimpleName().replace("Tool", ""), new FontAwesomeIconView(FontAwesomeIcon.CROSSHAIRS));
            //button.getStyleClass().add(TOOL_BUTTON_CSS_CLASS);
            button.setOnAction(e -> toolService.setCurrentTool(getClass()));
        }
        return button;

    }

    @Override
    public void update(FxTool currentTool) {
        
    }

    @Override
    public void subscribe(FxImageCanvas canvas) {

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, onMouseClick);

    }

    @Override
    public void unsubscribe(FxImageCanvas canvas) {
        canvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, onMouseClick);
    }

    public void onClick(MouseEvent event) {

        CanvasCamera camera = getCanvasFromEvent(event).getCamera();
        Point2D onScreen = new Point2D(event.getX(), event.getY());
        Point2D onImage = camera.getPositionOnImage(onScreen);

        PointOverlay pointOverlay = new PointOverlay(context);
        double[] point = new double[]{onImage.getX(), onImage.getY()};
        pointOverlay.getPoints().add(point);
        pointOverlay.setFillColor(new ColorRGB(0, 255, 0));
        pointOverlay.setAlpha(255);
        pointOverlay.setLineWidth(1.0);
        List<Overlay> overlays = new ArrayList<>();
        overlays.add(pointOverlay);

        overlayService.addOverlays(getCanvasFromEvent(event).getImageDisplay(), overlays);

        eventService.publishLater(new OverlayCreatedEvent(pointOverlay));

    }

    protected FxImageCanvas getCanvasFromEvent(MouseEvent event) {
        return (FxImageCanvas) event.getTarget();
    }

    
    @org.scijava.event.EventHandler
    public void onToolChanged(ToolChangeEvent event) {
        if(event.getTool() != this) button.setSelected(false);
        
    }
    
}
