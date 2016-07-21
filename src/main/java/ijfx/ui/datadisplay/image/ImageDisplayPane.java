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
package ijfx.ui.datadisplay.image;

import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.service.log.DefaultLoggingService;
import ijfx.service.overlay.OverlaySelectedEvent;
import ijfx.service.overlay.OverlaySelectionEvent;
import ijfx.service.overlay.OverlaySelectionService;
import ijfx.ui.arcmenu.PopArcMenu;
import ijfx.ui.canvas.FxImageCanvas;
import ijfx.ui.canvas.utils.ViewPort;
import ijfx.ui.datadisplay.image.overlay.OverlayDisplayService;
import ijfx.ui.datadisplay.image.overlay.OverlayDrawer;
import ijfx.ui.datadisplay.image.overlay.OverlayModifier;
import ijfx.ui.tool.FxTool;
import ijfx.ui.tool.FxToolService;
import ijfx.ui.tool.ToolChangeEvent;
import ijfx.ui.tool.overlay.MoveablePoint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import net.imagej.Dataset;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.event.OverlayCreatedEvent;
import net.imagej.event.OverlayDeletedEvent;
import net.imagej.event.OverlayUpdatedEvent;
import net.imagej.overlay.Overlay;
import org.scijava.Context;
import org.scijava.display.DisplayService;
import org.scijava.event.EventHandler;
import org.scijava.event.SciJavaEvent;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.plugin.Parameter;

/**
 * This render an ImageDisplay as a JavaFX object.
 * 
 * e.g. :
 * 
 * 
 * Dataset dataset = ...
 * 
 * DefaultImageDispay imageDisplay = new DefaultImageDisplay();
 * imageDisplay.display(dataset);
 * ImageDisplayPane pane = new ImageDisplayPane(context);
 * pane.show(imageDisplay);
 * 
 * 
 * 
 * @author cyril
 */
public class ImageDisplayPane extends AnchorPane {

    @FXML
    StackPane stackPane;

    @FXML
    AnchorPane anchorPane;

    @FXML
    Label infoLabel;

    @Parameter
    private OverlayDisplayService overlayDisplayService;

    @Parameter
    private DefaultLoggingService logService;

    @Parameter
    private OverlaySelectionService overlaySelectionService;

    @Parameter
    private OverlayService overlayService;

    @Parameter
    private ImageDisplayService imageDisplayService;

    @Parameter
    private DisplayService displayService;

    @Parameter
    private TimerService timerService;

    @Parameter
    private FxToolService toolService;

    private FxTool currentTool;

    private PopArcMenu arcMenu;

    private FxImageCanvas canvas;

    private Dataset dataset;

    private ImageDisplay imageDisplay;

    private final ExecutorService refreshQueue = Executors.newFixedThreadPool(1);

    private final HashMap<Overlay, Node> shapeMap = new HashMap<>();

    private final HashMap<Overlay, OverlayDrawer> drawerMap = new HashMap();
    private final HashMap<Overlay, OverlayModifier> modifierMap = new HashMap();

    private final Property<FxTool> currentToolProperty = new SimpleObjectProperty();

    private final ImageWindowEventBus bus = new ImageWindowEventBus();

    private final StringProperty titleProperty = new SimpleStringProperty();

    public ImageDisplayPane(Context context) throws IOException {

        FXUtilities.injectFXML(this);

        context.inject(this);

        canvas = new FxImageCanvas();

        getChildren().add(canvas);

        AnchorPane.setTopAnchor(canvas, 0d);
        AnchorPane.setLeftAnchor(canvas, 0d);
        AnchorPane.setRightAnchor(canvas, 0d);
        AnchorPane.setBottomAnchor(canvas, 30d);

        // Adding canvas related events
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onCanvasClick);

        canvas.cursorProperty().bind(Bindings.createObjectBinding(this::getToolDefaultCursor, currentToolProperty));

        canvas.getCamera().addListener(this::onViewPortChange);

        canvas.widthProperty().bind(stackPane.widthProperty());
        canvas.heightProperty().bind(stackPane.heightProperty());

    }

    public void display(ImageDisplay display) {
        imageDisplay = (ImageDisplay) display;
        build();
        setCurrentTool(toolService.getCurrentTool());
        initEventBuffering();
        canvas.setImageDisplay(imageDisplay);
    }

    public DatasetView getDatasetview() {
        return imageDisplayService.getActiveDatasetView(imageDisplay);
    }

    public Dataset getDataset() {

        if (dataset == null) {
            dataset = imageDisplayService.getActiveDataset(imageDisplay);
        }

        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setTitle(String title) {
        titleProperty.setValue(title);
    }

    public StringProperty titleProperty() {
        return titleProperty;
    }

    private WritableImage wi;

    public WritableImage getWrittableImage() {
        if (wi == null) {
            wi = new WritableImage(getDatasetview().getPreferredWidth(), getDatasetview().getPreferredHeight());
            canvas.setImage(wi);
        }
        return wi;
    }

    public DisplayService getDisplayService() {
        return displayService;
    }

    public ImageDisplayService getImageDisplayService() {
        return imageDisplayService;
    }

    public ImageDisplay getImageDisplay() {
        return imageDisplay;
    }

    public void refreshSourceImage() {

        refreshQueue.execute(new CallbackTask<Void, Void>()
                .run(this::transformImage)
                .then(this::updateImageAndOverlays)
        );

    }

    private void transformImage() {

        Timer t = timerService.getTimer(this.getClass());
        t.start();

        logService.info("Refreshing source image " + imageDisplay.getName());

        //Retrieving buffered image from the dataset view
        BufferedImage bf = getDatasetview().getScreenImage().image();

        t.elapsed("getScreenImage");

        // getting a writableImage (previously created or not)
        WritableImage writableImage = getWrittableImage();

        // if the size of the images are different, the buffer is recreated;
        if (bf.getWidth() != canvas.getCamera().getImageSpace().getWidth() || bf.getHeight() != canvas.getCamera().getImageSpace().getHeight()) {

            logService.info(String.format("Image size has changed :  %.0f x %.0f", writableImage.getWidth(), writableImage.getHeight()));

            // force buffer recreation
            wi = null;
            writableImage = getWrittableImage();

        }
        t.elapsed("getWrittableImage");
        SwingFXUtils.toFXImage(bf, writableImage);
        t.elapsed("write to buffer");
    }

    private void updateImageAndOverlays(Void v) {

        Timer t = timerService.getTimer(this.getClass());

        canvas.repaint();
        t.elapsed("canvas.repaint");
        updateInfoLabel();
        t.elapsed("updateInfoLabel");
        // updateOverlays();
        t.elapsed("updateOverlays");

    }

    private void updateOverlays(List<Overlay> overlays) {

        Collection<Overlay> toTreat = new HashSet<>(overlays);

        logService.info(String.format("Received %d, treating %d overlays", overlays.size(), toTreat.size()));

        Platform.runLater(() -> {
            toTreat.forEach(this::updateOverlay);
        });
    }

    protected void updateOverlay(Overlay overlay) {
        try {

            Timer t = timerService.getTimer(this.getClass());
            t.start();
            if (overlay == null) {
                return;
            }
            OverlayDrawer drawer = getDrawer(overlay);
            if (drawer == null) {
                logService.warn("No drawer found for %s", overlay.getClass().getSimpleName());
            };
            Node node = drawer.update(overlay, canvas.getCamera());
            node.setMouseTransparent(true);
            node.setUserData(overlay);

            if (anchorPane.getChildren().contains(node) == false) {
                anchorPane.getChildren().add(node);
            }

            if (Shape.class.isAssignableFrom(node.getClass())) {
                Shape shape = (Shape) node;
                if (overlaySelectionService.isSelected(imageDisplay, overlay)) {
                    shape.setFill(shape.getStroke());
                } else {
                    shape.setFill(Color.TRANSPARENT);
                }
            }
            t.elapsed("updateOverlay");

        } catch (NullPointerException e) {
            logService.warn("Couldn't draw overlay. Probably because of a lack of Drawer", e);
        }
    }

    private Node deleteOverlay(Overlay overlay) {
        Node toRemove = getDrawer(overlay).update(overlay, canvas.getCamera());

        if (getModifier(overlay) != null) {
            anchorPane.getChildren().remove(getModifier(overlay).getModifiers(canvas.getCamera(), overlay));
        }
        drawerMap.remove(overlay);
        modifierMap.remove(overlay);
        return toRemove;
    }

    private void deleteOverlays(List<Overlay> overlay) {
        Platform.runLater(() -> {
            logService.info(String.format("Deleting %d overlays", overlay.size()));
            anchorPane
                    .getChildren()
                    .removeAll(
                            overlay
                            .stream()
                            .map(this::deleteOverlay)
                            .collect(Collectors.toList())
                    );
        });
    }

    // add the MoveablePoints of a overlay in order to edit it
    private void setEdited(Overlay overlay) {

        if (overlay == null) {
            Platform.runLater(this::deleteAllModifiers); //deleteAllModifiers();
            return;
        }

        if (getOverlays().contains(overlay) == false) {
            return;
        }

        // delete all the moveable points
        deleteAllModifiers();

        // if an overlay has been selected
        if (overlay != null) {
            // it get the modifier which will returns the set of moveable points

            OverlayModifier modifier = getModifier(overlay);
            if (modifier == null) {
                logService.warn("No modifier was found for this overlay " + overlay.getClass().getSimpleName());
                return;
            }
            List<MoveablePoint> modifiers = modifier.getModifiers(canvas.getCamera(), overlay);

            modifiers.forEach(m -> m.positionOnImageProperty().addListener(this::onMoveablePointMoved));
            anchorPane.getChildren().addAll(modifiers);
        }

    }

    private void deleteAllModifiers() {
        Node[] nodes = anchorPane.getChildren().stream().filter(node -> MoveablePoint.class.isAssignableFrom(node.getClass())).toArray(size -> new Node[size]);
        anchorPane.getChildren().removeAll(nodes);
    }

    /*
    
     Arc Menu building
    
     */
    public void build() {
        setTitle(imageDisplay.getName());
        //   System.out.println(getDatasetview().getPlanePosition().numDimensions());

        logService.setLevel(LogService.INFO);

        if (arcMenu != null) {
            anchorPane.removeEventHandler(MouseEvent.MOUSE_CLICKED, arcMenu::show);
            arcMenu = null;

        }
        arcMenu = new PopArcMenu();
        anchorPane.addEventFilter(MouseEvent.MOUSE_CLICKED, arcMenu::show);

        double min = getDatasetview().getChannelMin(0);
        double max = getDatasetview().getChannelMax(0);

        logService.info(String.format("Adding slider %.1f %.1f ", min, max));
        for (int i = 0; i != imageDisplay.numDimensions(); i++) {

            if (imageDisplay.axis(i).type().isXY()) {
                continue;
            }

            Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);

            arcMenu.addAll(new AxisArcItem(imageDisplay, i));
            //hbox.getChildren().add(new AxisSlider(imageDisplay, i));
        }

        arcMenu.build();

        refreshSourceImage();
    }

    /**
     *
     * JavaFX Events
     *
     */
    protected void initEventBuffering() {

        // stream intercepting all event causing a refresh of the display
        bus.getStream(SciJavaEvent.class)
                .filter(bus::doesDisplayRequireRefresh)
                .buffer(1000 / 15, TimeUnit.MILLISECONDS)
                .filter(list -> !list.isEmpty())
                .subscribe(list -> {

                    logService.info(String.format("%s events that require image refresh", list.size()));
                    list.forEach(event -> System.out.println(event.getClass().getSimpleName()));
                    refreshSourceImage();
                });

        // stream intercepting all event causing a redrawing of the overlay
        bus.getStream(SciJavaEvent.class)
                .filter(bus::isOverlayEvent) // filter all the events that should update an overlay
                .buffer(1000 / 24, TimeUnit.MILLISECONDS) // we buffer them to avoid any overlap
                .filter(list -> !list.isEmpty()) // we check for event that are not empty event
                .map(list -> list.stream().map(this::extractOverlayFromEvent).collect(Collectors.toList())) // the list is map to overlay
                .subscribe(this::updateOverlays);

        // stream intercepting all the event causing a overlay to be deleted
        bus.getStream(OverlayDeletedEvent.class)
                .map(event -> event.getObject())
                .buffer(1000 / 24, TimeUnit.MILLISECONDS)
                .filter(list -> !list.isEmpty())
                .subscribe(this::deleteOverlays);

    }

    protected Overlay extractOverlayFromEvent(SciJavaEvent event) {
        logService.info("Mapping event to overlay");
        if (event instanceof OverlayCreatedEvent) {
            return ((OverlayCreatedEvent) event).getObject();
        }
        if (event instanceof OverlayUpdatedEvent) {
            return ((OverlayUpdatedEvent) event).getObject();
        }
        if (event instanceof OverlaySelectionEvent) {
            return ((OverlaySelectionEvent) event).getOverlay();
        }
        if (event instanceof OverlaySelectedEvent) {
            return ((OverlaySelectedEvent) event).getOverlay();
        }
        return null;
    }

    public void onWindowSizeChanged(Observable obs) {
        canvas.repaint();
        updateOverlays(getOverlays());
    }

    public void onViewPortChange(ViewPort viewport) {
        updateOverlays(getOverlays());
        canvas.repaint();

    }

    protected Cursor getToolDefaultCursor() {

        if (currentTool == null) {
            return Cursor.DEFAULT;
        } else {
            return currentTool.getDefaultCursor();
        }
    }

    protected boolean isWindowConcernedByModule(Module module) {
        return module.getInputs().values().stream().filter(obj -> (obj == imageDisplay || obj == getDataset())).count() > 0;
    }

    protected void onMoveablePointMoved(Observable obs, Point2D oldValue, Point2D newValue) {
        getOverlays().forEach(this::updateOverlay);
    }

    @EventHandler
    protected void onOverlaySelectionChanged(OverlaySelectionEvent event) {

        if (event.getDisplay() != imageDisplay) {
            return;
        }

        logService.info("Channeling event");
        bus.channel(event);

        Platform.runLater(() -> setEdited(event.getOverlay()));

    }

    @EventHandler
    protected void onOverlaySelectedEvent(OverlaySelectedEvent event) {
        logService.info("Overlay selected event");
        bus.channel(event);
    }

    @EventHandler
    protected void onOverlayCreated(OverlayCreatedEvent event) {
        logService.info("Overlay created");
        if (getOverlays().contains(event.getObject())) {
            //Platform.runLater(() -> updateOverlay(event.getObject()));
            bus.channel(event);

        }
    }

    @EventHandler
    void onOverlayModified(OverlayUpdatedEvent event) {
        logService.info("Overlay modified");
        if (getOverlays().contains(event.getObject())) {
            bus.channel(event);
        }
    }

    @EventHandler
    void onDataViewUpdated(DataViewUpdatedEvent event) {
        logService.info("DataView updated");
        if (imageDisplay.contains(event.getView())) {
            bus.channel(event);
        }
    }

    @EventHandler
    protected void onOverlayDeleted(OverlayDeletedEvent event) {
        logService.info("Overlay deleted");
        if (drawerMap.keySet().contains(event.getObject()) == false) {
            return;
        }

        bus.channel(event);
        setEdited(null);

        /*
        // removing the associated node
        anchorPane.getChildren()
                .stream()
                .filter(node->node.getUserData() == event.getObject())
                .forEach(node->Platform.runLater(()->anchorPane.getChildren().remove(node)));
         */
        //updateOverlays();
        //Platform.runLater(() -> deleteOverlay(event.getObject()));
    }

    @EventHandler
    protected void onAnyEvent(SciJavaEvent event) {
        logService.info(event.getClass().getSimpleName());
    }

    protected OverlayDrawer getDrawer(Overlay overlay) {
        return drawerMap.computeIfAbsent(overlay, overlayDisplayService::createDrawer);
    }

    protected OverlayModifier getModifier(Overlay overlay) {
        return modifierMap.computeIfAbsent(overlay, overlayDisplayService::createModifier);
    }

    public void updateInfoLabel() {

        String imageType = getDataset().getTypeLabelShort();

        long width = getDataset().dimension(0);
        long height = getDataset().dimension(1);

        infoLabel.setText(String.format("%s - %d x %d", imageType, width, height));

    }

    private boolean isOnOverlay(double x, double y, Overlay overlay) {
        
        
         
        OverlayDrawer drawer = getDrawer(overlay);
        
        if(drawer == null) return false;
        else {
            return drawer.isOnOverlay(overlay, canvas.getCamera(), x, y);
        }
        /*
        return drawer.isOnOverlay(overlay,canvas.getCamera(),x,y);
        
        double x1 = overlay.getRegionOfInterest().realMin(0);
        double y1 = overlay.getRegionOfInterest().realMin(1);
        double x2 = overlay.getRegionOfInterest().realMax(0);
        double y2 = overlay.getRegionOfInterest().realMax(1);
        
        //System.out.println(String.format("(%.0f,%.0f), (%.0f,%.0f)", x1, y1, x2, y2));
        Rectangle2D r = new Rectangle2D(x1, y1, x2 - x1, y2 - y1);
        //System.out.println(String.format("contains  (%.0f,%.0f) ? : %b",x,y,r.contains(x,y)));
        return (r.contains(x, y));
        */
    }

    /*
     Tool handling
   
     */
    public FxTool getCurrentTool() {
        if (currentTool == null) {
            setCurrentTool(toolService.getCurrentTool());
        }
        return currentTool;
    }

    public void setCurrentTool(FxTool tool) {
        Platform.runLater(() -> currentToolProperty.setValue(tool));
        if (tool == currentTool) {
            return;
        }

        if (currentTool != null) {
            currentTool.unsubscribe(canvas);
        }
        if (tool != null) {
            tool.subscribe(canvas);
        }
        currentTool = tool;

    }

    /*
     Overlay drawing
     */
    public List<Overlay> getOverlays() {

        List<Overlay> overlays = overlayService.getOverlays(imageDisplay);
        if (overlays == null) {
            return new ArrayList<Overlay>();
        }
        return overlayService.getOverlays(imageDisplay);
    }

    public Overlay getSelectedOverlay() {
        return overlayService.getActiveOverlay(imageDisplay);
    }

    /*
     Event handling
     */
    private void onCanvasClick(MouseEvent event) {

        System.out.println("canvas click");
        Point2D positionOnImage = canvas.getPositionOnImage(event.getX(), event.getY());
        System.out.println(positionOnImage);
        logService.info(String.format("This image contains %s overlays", overlayService.getOverlays(imageDisplay).size()));

        boolean wasOverlaySelected = false;
        /*
        for (Overlay o : overlayService.getOverlays(imageDisplay)) //.parallelStream().forEach(o -> 
        {
            if (isOnOverlay(positionOnImage.getX(), positionOnImage.getY(), o)) {
                wasOverlaySelected = true;
                logger.info("Selecting overlay " + o.toString());
                overlaySelectionService.setOverlaySelection(imageDisplay, o, true);
                event.consume();
            } else {
                overlaySelectionService.setOverlaySelection(imageDisplay, o, false);
            }
        };*/

        List<Overlay> touchedOverlay = overlayService.getOverlays(imageDisplay).stream()
                .filter(o -> isOnOverlay(positionOnImage.getX(), positionOnImage.getY(), o))
                .collect(Collectors.toList());

        touchedOverlay.forEach(o -> {
            System.out.println("Overlay : " + o);
        });

        wasOverlaySelected = touchedOverlay.size() > 0;
        logService.info(String.format("%d overlay touched", touchedOverlay.size()));
        if (touchedOverlay.size() == 0) {
            overlaySelectionService.unselectedAll(imageDisplay);
        } else if (touchedOverlay.size() == 1) {
            overlaySelectionService.selectOnlyOneOverlay(imageDisplay, touchedOverlay.get(0));
        } else {

        }

        if (!wasOverlaySelected) {
            setEdited(null);
        }
        //contextCalculationService.determineContext(imageDisplay, true);
        //refreshSourceImage();
        canvas.repaint();
        updateInfoLabel();

    }

    /*
      
    Helper Classes
    
     */
    public class AxisSlider extends Slider {

        CalibratedAxis axis;
        ImageDisplay display;
        int axisId;

        public AxisSlider(ImageDisplay display, int id) {
            this.axis = display.axis(id);
            this.display = display;

            setMin(display.min(id));
            setMax(display.max(id));
            setValue(display.getLongPosition(id));
            setMajorTickUnit(1.0);
            setMinorTickCount(0);
            setSnapToTicks(true);
            setBlockIncrement(1.0f);
            setShowTickMarks(true);
            logService.info(String.format("Adding axis %s (%.1f - %.1f) with initial value : %.3f", axis.type(), getMin(), getMax(), getValue()));

            //setMinorTickCount((int)Math.round(getMax()-getMin()));
            valueProperty().addListener((event, oldValue, newValue) -> {

                if (display.getLongPosition(axisId) == newValue.longValue()) {
                    return;
                }

                display.setPosition(newValue.longValue(), axis.type());
                logService.info(String.format("Changing %s to %.3f", axis.type().getLabel(), newValue.doubleValue()));
            });
        }

    }

}
