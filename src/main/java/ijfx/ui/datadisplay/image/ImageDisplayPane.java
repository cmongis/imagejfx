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

import ijfx.core.stats.IjfxStatisticService;
import ijfx.service.ImagePlaneService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.service.overlay.OverlaySelectedEvent;
import ijfx.service.overlay.OverlaySelectionEvent;
import ijfx.service.overlay.OverlaySelectionService;
import ijfx.ui.arcmenu.PopArcMenu;
import ijfx.ui.canvas.FxImageCanvas;
import ijfx.ui.canvas.utils.ViewPort;
import ijfx.ui.datadisplay.image.overlay.OverlayDisplayService;
import ijfx.ui.datadisplay.image.overlay.OverlayDrawer;
import ijfx.ui.datadisplay.image.overlay.OverlayModifier;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.tool.FxTool;
import ijfx.ui.tool.FxToolService;
import ijfx.ui.tool.overlay.MoveablePoint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.display.event.LUTsChangedEvent;
import net.imagej.event.OverlayCreatedEvent;
import net.imagej.event.OverlayDeletedEvent;
import net.imagej.event.OverlayUpdatedEvent;
import net.imagej.overlay.Overlay;
import net.imglib2.RandomAccess;
import net.imglib2.converter.RealLUTConverter;
import net.imglib2.display.ColorTable;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.event.SciJavaEvent;
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
 * imageDisplay.display(dataset); ImageDisplayPane pane = new
 * ImageDisplayPane(context); pane.show(imageDisplay);
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
    private Logger logService = ImageJFX.getLogger();

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

    @Parameter
    private ImagePlaneService imagePlaneService;

    @Parameter
    private EventService eventService;

    @Parameter
    private IjfxStatisticService statsService;

    Logger logger = ImageJFX.getLogger();

    private FxTool currentTool;

    private javafx.event.EventHandler<MouseEvent> myHandler;

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

    private int refreshDelay = 100; // in milliseconds

    private AxisConfiguration axisConfig;

    public ImageDisplayPane(Context context) throws IOException {

        FXUtilities.injectFXML(this);

        context.inject(this);

        canvas = new FxImageCanvas();

        stackPane.getChildren().add(canvas);

        //AnchorPane.setTopAnchor(canvas, 0d);
        //AnchorPane.setLeftAnchor(canvas, 0d);
        //AnchorPane.setRightAnchor(canvas, 0d);
        //AnchorPane.setBottomAnchor(canvas, 30d);
        // Adding canvas related events
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onCanvasClick);

        canvas.cursorProperty().bind(Bindings.createObjectBinding(this::getToolDefaultCursor, currentToolProperty));

        canvas.getCamera().addListener(this::onViewPortChange);

        stackPane.widthProperty().addListener(this::onWidthChanged);
        stackPane.heightProperty().addListener(this::onHeightChanged);
        //canvas.heightProperty().bind(stackPane.heightProperty());
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {

            if (e.getButton() == MouseButton.PRIMARY && this.getImageDisplay() != null) {
                displayService.setActiveDisplay(this.getImageDisplay());
            }
        });
    }

    private void onWidthChanged(Observable obs, Number oldValue, Number newValue) {
        canvas.setWidth(newValue.doubleValue());
        updateOverlays(getOverlays());
    }

    private void onHeightChanged(Observable obs, Number oldValue, Number newValue) {
        canvas.setHeight(newValue.doubleValue());
        updateOverlays(getOverlays());
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
            if (canvas.getImage() != wi) {
                canvas.setImage(wi);
            }
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

        //refreshQueue.execute(
        new CallbackTask<Void, Void>()
                .run(this::transformImage)
                .then(this::updateImageAndOverlays)
                .start();
        //);

    }

    private void transformImage() {

        useImageJRender();

    }

    private <T extends RealType<T>> void useIjfxRender() {

        Timer t = timerService.getTimer(this.getClass());
        t.start();
        t.elapsed("since last time");

        WritableImage image = getWrittableImage();

        t.elapsed("getWrittableImage");
        long[] position = new long[imageDisplay.numDimensions()];

        imageDisplay.localize(position);
        RandomAccess<T> ra;
        if (position.length == 2) {
            ra = (RandomAccess<T>) getDataset().randomAccess();
        } else {
            ra = (RandomAccess<T>) imagePlaneService.planeView(getDataset(), position).randomAccess();
        }

        int channel = imageDisplay.getIntPosition(Axes.CHANNEL);
        ColorTable colorTable = dataset.getColorTable(channel);

        DatasetView view = imageDisplayService.getActiveDatasetView(imageDisplay);

        double min = view.getChannelMin(channel);
        double max = view.getChannelMax(channel);

        int width = (int) dataset.dimension(0);
        int height = (int) dataset.dimension(1);

        RealLUTConverter<T> converter = new RealLUTConverter<T>(min, max, colorTable);
        ARGBType argb = new ARGBType();
        // RandomAccess<T> ra = dataset.randomAccess();

        for (int x = 0; x != width; x++) {
            for (int y = 0; y != height; y++) {
                ra.setPosition(x, 0);
                ra.setPosition(y, 1);
                converter.convert(ra.get(), argb);
                image.getPixelWriter().setArgb(x, y, argb.get());
            }
        }

        t.elapsed("pixel transformation");

    }

    synchronized private void useImageJRender() {

        Timer t = timerService.getTimer(this.getClass());
        t.start();

        logService.info("Refreshing source image " + imageDisplay.getName());
        t.elapsed("in between");
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

        // Timer t = timerService.getTimer(this.getClass());
        canvas.repaint();
        // t.elapsed("canvas.repaint");
        updateInfoLabel();

        if (getAxisConfiguration().equals(getDataset()) == false) {
            axisConfig = new AxisConfiguration(getDataset());
            build();
        }
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

            // if the overlay is null we cancel
            if (overlay == null) {
                return;
            }

            // now we have to draw the overlay. Each overlay
            // has it's own drawer
            OverlayDrawer drawer = getDrawer(overlay);

            // if no drawer was found we cancel
            if (drawer == null) {
                logService.warning(String.format("No drawer found for %s", overlay.getClass().getSimpleName()));
                return;
            };

            // otherwise, if a drawer is found, we ask it to generate a shape
            Node node = drawer.update(overlay, canvas.getCamera());

            // if the shape is not on the camera, we just remove it and skip it
            if (drawer.isOverlayOnViewPort(overlay, canvas.getCamera()) == false) {

                anchorPane.getChildren().remove(node);
                return;

            } // otherwise, the node is configured
            else {

                node.setMouseTransparent(true);
                node.setUserData(overlay);
                // if the node wasn't on the anchor pane, it's added
                if (anchorPane.getChildren().contains(node) == false) {
                    anchorPane.getChildren().add(node);
                }

                // testing if it's a shpe class node.
                if (Shape.class.isAssignableFrom(node.getClass())) {
                    Shape shape = (Shape) node;
                    if (overlaySelectionService.isSelected(imageDisplay, overlay)) {
                        shape.setFill(shape.getStroke());
                    } else {
                        shape.setFill(Color.TRANSPARENT);
                    }
                }
            }
            t.elapsed("updateOverlay");

        } catch (NullPointerException e) {
            logService.log(Level.WARNING, "Couldn't draw overlay. Probably because of a lack of Drawer", e);
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
                logService.warning("No modifier was found for this overlay " + overlay.getClass().getSimpleName());
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
 

        if (arcMenu != null) {
            anchorPane.removeEventHandler(MouseEvent.MOUSE_PRESSED, myHandler);
            arcMenu = null;

        }
        arcMenu = new PopArcMenu();
        myHandler = (MouseEvent event) -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                arcMenu.show(event);
            }
        };
        anchorPane.addEventFilter(MouseEvent.MOUSE_CLICKED, myHandler);

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
                .buffer(refreshDelay, TimeUnit.MILLISECONDS)
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
        if (getOverlays().contains(event.getOverlay())) {
            bus.channel(event);
        }
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
        try {

            if (imageDisplay.contains(event.getView())) {
                bus.channel(event);
            }
        } catch (Exception e) {
        }
    }

    @EventHandler
    void onDisplayUpdated(DisplayUpdatedEvent event) {
        logService.info("Display updated event");
        if (event.getDisplay() == imageDisplay) {
            bus.channel(event);
        }
    }

    @EventHandler
    public void onDatasetViewUpdated(DataViewUpdatedEvent event) {
        if (event.getView() == getDatasetview()) {
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

    }

    @EventHandler
    protected void onLUTsChangedEvent(LUTsChangedEvent event) {
        logService.info("LUT changed");
        if (event.getView() == getDatasetview()) {
            logService.info("Updating dataset");
            ImageJFX.getThreadPool().execute(getDataset()::update);
            // getDatasetview().getProjector().map();
        }
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
        CalibratedAxis[] axes = new CalibratedAxis[imageDisplay.numDimensions()];
        int[] position = new int[imageDisplay.numDimensions()];
        imageDisplay.axes(axes);
        imageDisplay.localize(position);
        StringBuilder stringBuilder = new StringBuilder();
        IntStream.range(2, position.length)
                .forEach(e -> {
                    stringBuilder.append(axes[e].type().toString());
                    stringBuilder.append(": ");
                    stringBuilder.append(position[e]);
                    stringBuilder.append("");

                });
        infoLabel.setText(String.format("%s - %d x %d - %s", imageType, width, height, stringBuilder));

    }

    private boolean isOnOverlay(double x, double y, Overlay overlay) {

        OverlayDrawer drawer = getDrawer(overlay);

        if (drawer == null) {
            return false;
        } else {
            return drawer.isOnOverlay(overlay, canvas.getCamera(), x, y);
        }
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
        List<Overlay> overlays = null;
        try {
            overlays = overlayService.getOverlays(imageDisplay);

        } catch (Exception e) {
        }
        if (overlays == null) {
            return new ArrayList<Overlay>();
        }
        return overlays;
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

    public FxImageCanvas getCanvas() {
        return canvas;
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

    public AxisConfiguration getAxisConfiguration() {
        if (axisConfig == null) {
            axisConfig = new AxisConfiguration(getDataset());
        }
        return axisConfig;
    }

    private class AxisConfiguration {

        private CalibratedAxis[] axes;

        public AxisConfiguration(Dataset dataset) {

            axes = new CalibratedAxis[dataset.numDimensions()];
            dataset.axes(axes);

        }

        public int numAxis() {
            return axes.length;
        }

        public CalibratedAxis[] axes() {
            return axes;
        }

        @Override
        public boolean equals(Object object) {
            if (object == null) {
                return false;
            }
            if (object instanceof Dataset) {
                return equals(new AxisConfiguration((Dataset) object));
            }
            if (object instanceof AxisConfiguration == false) {
                return false;
            }
            AxisConfiguration other = (AxisConfiguration) object;
            if (other.numAxis() != numAxis()) {
                return false;
            }
            return Arrays.equals(axes, other.axes());

        }

    }

    public void dispose() {

    }

}
