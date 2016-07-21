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
package ijfx.ui.datadisplay.image;

import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.input.BatchInputBuilder;
import ijfx.service.log.DefaultLoggingService;
import ijfx.service.overlay.OverlayDrawingService;
import ijfx.ui.canvas.FxImageCanvas;
import ijfx.ui.tool.FxTool;
import ijfx.ui.tool.ToolChangeEvent;
import ijfx.ui.tool.DefaultFxToolService;
import ijfx.service.uicontext.UiContextCalculatorService;
import ijfx.service.overlay.OverlaySelectedEvent;
import ijfx.service.overlay.OverlaySelectionEvent;
import ijfx.service.overlay.OverlaySelectionService;
import ijfx.ui.arcmenu.PopArcMenu;
import ijfx.ui.canvas.utils.ViewPort;
import ijfx.ui.datadisplay.image.overlay.OverlayDrawer;
import ijfx.ui.datadisplay.image.overlay.OverlayDisplayService;
import ijfx.ui.datadisplay.image.overlay.OverlayModifier;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.tool.overlay.MoveablePoint;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
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
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import jfxtras.scene.control.window.CloseIcon;
import jfxtras.scene.control.window.Window;
import mongis.utils.CallbackTask;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.display.event.AxisPositionEvent;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.display.event.LUTsChangedEvent;
import net.imagej.event.DatasetUpdatedEvent;
import net.imagej.event.OverlayCreatedEvent;
import net.imagej.event.OverlayDeletedEvent;
import net.imagej.event.OverlayUpdatedEvent;
import net.imagej.lut.LUTService;
import net.imagej.overlay.Overlay;

import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import org.controlsfx.control.HiddenSidesPane;

import org.scijava.Context;

import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayActivatedEvent;
import org.scijava.display.event.DisplayDeletedEvent;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.event.SciJavaEvent;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.thread.ThreadService;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class ImageWindow extends Window {

    Logger logger = ImageJFX.getLogger();

    private Dataset dataset;


    @Parameter
    private DisplayService displayService;

    @Parameter
    private ImageDisplayService imageDisplayService;

    @Parameter
    private DatasetService datasetService;

    private ImageDisplay imageDisplay;

    @Parameter
    private LUTService lutService;

    private Slider slider = new Slider();

    @Parameter
    private EventService eventService;

    @Parameter
    private DefaultLoggingService logService;

    @Parameter
    private ThreadService threadService;

    @Parameter
    private DefaultFxToolService toolService;

    @Parameter
    private Context context;

    @Parameter
    private UiContextCalculatorService contextCalculationService;

    @Parameter
    private OverlaySelectionService overlaySelectionService;

    @Parameter
    private OverlayService overlayService;

    @Parameter
    private OverlayDisplayService overlayDrawer;

    @Parameter
    private PluginService pluginService;

    @Parameter
    private OverlayDisplayService overlayDisplayService;
    
    @Parameter
    private OverlayDrawingService overlayDrawingService;

    @Parameter
    private TimerService timerService;

    FxTool currentTool;

    /*
            JavaFX Nodes
     */
    private HiddenSidesPane hiddenSidePane = new HiddenSidesPane();
    private BorderPane borderPane = new BorderPane();
    private FxImageCanvas canvas = new FxImageCanvas();
    private AnchorPane anchorPane = new AnchorPane();
    private StackPane stackPane = new StackPane();
    private HBox hbox = new HBox();

    private HBox bottomHBox = new HBox();

    private Label infoLabel = new Label("Some text as example I guess");

    private PopArcMenu arcMenu;

    static String TITLE_CLASS_NAME = "ijfx-window-titlebar";

    static String WINDOW_CLASS_NAME = "ijfx-window";

    static String INFO_LABEL_CLASS_NAME = "info-label";

    private Color selectedOverlayColor = Color.BLUE;
    private Color overlayColor = Color.RED;

    private final ExecutorService refreshQueue = Executors.newFixedThreadPool(1);

    private final HashMap<Overlay, Node> shapeMap = new HashMap<>();

    private final HashMap<Overlay, OverlayDrawer> drawerMap = new HashMap();
    private final HashMap<Overlay, OverlayModifier> modifierMap = new HashMap();

    private final Property<FxTool> currentToolProperty = new SimpleObjectProperty();

    private final ImageWindowEventBus bus = new ImageWindowEventBus();

    public ImageWindow() {
        super();
        logger.info("Creating window.");
        setContentPane(borderPane);

        getStyleClass().add(WINDOW_CLASS_NAME);
        setTitleBarStyleClass(TITLE_CLASS_NAME);

        hiddenSidePane.setContent(anchorPane);
        borderPane.setCenter(hiddenSidePane);

        anchorPane.getChildren().add(stackPane);
        anchorPane.getChildren().add(canvas);

        //hbox.getChildren().add(canvas);
        stackPane.prefWidthProperty().bind(anchorPane.widthProperty());
        stackPane.prefHeightProperty().bind(anchorPane.heightProperty());

        // Bind canvas size to stack pane size.
        canvas.widthProperty().bind(anchorPane.widthProperty());
        canvas.heightProperty().bind(anchorPane.heightProperty());

        // Adding clicking listening
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onCanvasClick);

        canvas.cursorProperty().bind(Bindings.createObjectBinding(this::getToolDefaultCursor, currentToolProperty));

        // setting the window movable
        setMovable(true);

        // make sure that the window is put in front in the model when an event happens
        // on the canvas
        hbox.onMousePressedProperty().addListener(event -> putInFront());

        // make sure to repaint the canvas when resizing the window
        widthProperty().addListener(this::onWindowSizeChanged);

        // I forgot why but it must be important
        anchorPane.setMinSize(0, 0);

        // close icon
        CloseIcon closeIcon = new CloseIcon(this);

        getRightIcons().add(closeIcon);

        setOnCloseAction(this::onWindowClosed);

        setPrefSize(400, 400);

        //putting an unused hbox...
        borderPane.setBottom(bottomHBox);

        // adding a hidden side pane for info
        //hiddenSidePane.setContent(infoLabel);
        hiddenSidePane.setTop(infoLabel);
        infoLabel.getStyleClass().add(INFO_LABEL_CLASS_NAME);

        canvas.getCamera().addListener(this::onViewPortChange);

    }

    public ImageWindow(Display<?> display) {

        this();

        display.getContext().inject(this);

        imageDisplay = (ImageDisplay) display;

        build();

        setCurrentTool(toolService.getCurrentTool());

        for (EventType<? extends MouseEvent> t : new EventType[]{MouseEvent.MOUSE_CLICKED, MouseEvent.DRAG_DETECTED, MouseEvent.MOUSE_PRESSED}) {
            addEventHandler(t, this::putInFront);
            getContentPane().addEventHandler(t, this::putInFront);
        }

        focusedProperty().addListener(this::onFocus);

        translateXProperty().addListener(event -> putInFront());

        initEventBuffering();

    }

    public void onWindowSizeChanged(Observable obs) {
        canvas.repaint();
        updateOverlays(getOverlays());
    }

    public void onFocus(Observable focusProperty, Boolean oldValue, Boolean newValue) {
        if (newValue) {
            putInFront();
        }
    }

    public void putInFront(MouseEvent event) {
        putInFront();
    }

    public void putInFront() {
        //  System.out.println("putting display to front");
        logger.info("Putting in front " + imageDisplay);
        displayService.setActiveDisplay(imageDisplay);
        setMoveToFront(true);
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
        if (checkServices()) {

            double min = getDatasetview().getChannelMin(0);
            double max = getDatasetview().getChannelMax(0);

            logger.info(String.format("Adding slider %.1f %.1f ", min, max));
            for (int i = 0; i != imageDisplay.numDimensions(); i++) {

                if (imageDisplay.axis(i).type().isXY()) {
                    continue;
                }

                Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);

                arcMenu.addAll(new AxisArcItem(imageDisplay, i));
                //hbox.getChildren().add(new AxisSlider(imageDisplay, i));
            }
        }
         
        arcMenu.build();

        refreshSourceImage();
    }

    protected void initEventBuffering() {

        // stream intercepting all event causing a refresh of the display
        bus.getStream(SciJavaEvent.class)
                .filter(bus::doesDisplayRequireRefresh)
                .buffer(1000 / 15, TimeUnit.MILLISECONDS)
                .filter(list -> !list.isEmpty())
                .subscribe(list -> {
                    
                    logger.info(String.format("%s events that require image refresh", list.size()));
                    list.forEach(event->System.out.println(event.getClass().getSimpleName()));
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
        logger.info("Mapping event to overlay");
        if (event instanceof OverlayCreatedEvent) {
            return ((OverlayCreatedEvent) event).getObject();
        }
        if (event instanceof OverlayUpdatedEvent) {
            return ((OverlayUpdatedEvent) event).getObject();
        }
        if (event instanceof OverlaySelectionEvent) {
            return ((OverlaySelectionEvent) event).getOverlay();
        }
        if(event instanceof OverlaySelectedEvent) {
            return ((OverlaySelectedEvent) event).getOverlay();
        }
        return null;
    }

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
            logger.info(String.format("Adding axis %s (%.1f - %.1f) with initial value : %.3f", axis.type(), getMin(), getMax(), getValue()));

            //setMinorTickCount((int)Math.round(getMax()-getMin()));
            valueProperty().addListener((event, oldValue, newValue) -> {

                if (display.getLongPosition(axisId) == newValue.longValue()) {
                    return;
                }

                display.setPosition(newValue.longValue(), axis.type());
                logger.info(String.format("Changing %s to %.3f", axis.type().getLabel(), newValue.doubleValue()));
            });
        }

    }

    /*
     Dataset and image display
     */
    private boolean checkServices() {
        if (imageDisplayService == null) {
            logger.warning("ImageDisplayService is null !");
            return false;

        }
        if (imageDisplay == null) {
            logger.warning("ImageDisplay is null !");
            return false;
        }

        return true;
    }

    public DatasetView getDatasetview() {
        checkServices();
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

        if (checkServices()) {

            refreshQueue.execute(new CallbackTask<Void, Void>()
                    .run(this::transformImage)
                    .then(this::updateImageAndOverlays)
            );
        }
    }

    private void transformImage() {

        Timer t = timerService.getTimer(this.getClass());
        t.start();
       
        logger.info("Refreshing source image " + imageDisplay.getName());

        
      
        
        //Retrieving buffered image from the dataset view
        BufferedImage bf = getDatasetview().getScreenImage().image();
        
        t.elapsed("getScreenImage");

        // getting a writableImage (previously created or not)
        WritableImage writableImage = getWrittableImage();

        // if the size of the images are different, the buffer is recreated;
        if (bf.getWidth() != canvas.getCamera().getImageSpace().getWidth() || bf.getHeight() != canvas.getCamera().getImageSpace().getHeight()) {

            logger.info(String.format("Image size has changed :  %.0f x %.0f", writableImage.getWidth(), writableImage.getHeight()));

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

        logger.info(String.format("Received %d, treating %d overlays", overlays.size(), toTreat.size()));

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
            Shape shape = (Shape) node;
            if (overlaySelectionService.isSelected(imageDisplay,overlay)) {
                shape.setFill(shape.getStroke());
            }
            else {
                shape.setFill(Color.TRANSPARENT);
            }
            t.elapsed("updateOverlay");

        } catch (NullPointerException e) {
            logger.log(Level.WARNING, "Couldn't draw overlay. Probably because of a lack of Drawer", e);
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
            logger.info(String.format("Deleting %d overlays",overlay.size()));
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
                logger.warning("No modifier was found for this overlay " + overlay.getClass().getSimpleName());
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

    public void onViewPortChange(ViewPort viewport) {
        updateOverlays(getOverlays());
        canvas.repaint();

    }

    public void updateInfoLabel() {

        String imageType = getDataset().getTypeLabelShort();

        long width = getDataset().dimension(0);
        long height = getDataset().dimension(1);

        infoLabel.setText(String.format("%s - %d x %d", imageType, width, height));

    }

    private boolean isOnOverlay(double x, double y, Overlay overlay) {

        double x1 = overlay.getRegionOfInterest().realMin(0);
        double y1 = overlay.getRegionOfInterest().realMin(1);
        double x2 = overlay.getRegionOfInterest().realMax(0);
        double y2 = overlay.getRegionOfInterest().realMax(1);

        //System.out.println(String.format("(%.0f,%.0f), (%.0f,%.0f)", x1, y1, x2, y2));
        Rectangle2D r = new Rectangle2D(x1, y1, x2 - x1, y2 - y1);
        //System.out.println(String.format("contains  (%.0f,%.0f) ? : %b",x,y,r.contains(x,y)));
        return (r.contains(x, y));

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

    public Color getSelectedOverlayColor() {
        return selectedOverlayColor;
    }

    public void setSelectedOverlayColor(Color selectedOverlayColor) {
        this.selectedOverlayColor = selectedOverlayColor;
    }

    public Color getOverlayColor() {
        return overlayColor;
    }

    public void setOverlayColor(Color overlayColor) {
        this.overlayColor = overlayColor;
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
        logger.info(String.format("This image contains %s overlays", overlayService.getOverlays(imageDisplay).size()));

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
        logger.info(String.format("%d overlay touched", touchedOverlay.size()));
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

    @EventHandler
    protected void onAxisPositionChanged(final AxisPositionEvent event) {

        if (imageDisplay != event.getDisplay()) {
            logger.info("Ignoring event");
            return;
        }
        logger.info("Changing a axis");
        //bus.channel(event);
        //imageDisplay.update();
        //refreshSourceImage();
        //refreshSourceImage();
    }

    @EventHandler
    protected void onLUTChanged(LUTsChangedEvent event) {

        if (event.getView() != getDatasetview()) {
            return;
        }
        logger.info("LUTsChangedEvent");
        //imageDisplay.update();
        //refreshSourceImage();
        //getOverlays().stream().map(o -> new OverlayUpdatedEvent(o)).forEach(bus::channel);
          imageDisplayService.getActiveDataset(imageDisplay).update();
        //  System.out.println(event.getView());
        // System.out.println(getDatasetview());
        //imageDisplay.update();
        // last one : imageDisplayService.getActiveDataset(imageDisplay).update();
        //refreshSourceImage();
    }

    @EventHandler
    public void onDatasetViewUpdated(DataViewUpdatedEvent event) {
        if(event.getView() == getDatasetview()) {
            bus.channel(event);
        }
    }
    
    @EventHandler
    protected void onDatasetUpdated(DatasetUpdatedEvent event) {
        //imageDisplay.update();
        if (event.getObject() != imageDisplayService.getActiveDataset(imageDisplay)) {
            return;
        }
        logger.info("DatasetChangedEvent : " + event.getObject());
        //imageDisplay.update();
        
        bus.channel(event);
        updateOverlays(getOverlays());
        //refreshSourceImage();
    }

    @EventHandler
    protected void onDisplayUpdated(DisplayUpdatedEvent event) throws InterruptedException {
        // imageDisplay.update();

        if (event.getDisplay() != imageDisplay) {
            return;
        }
        logger.info("DisplayUpdatedEvent");
        bus.channel(event);
        //displayUpdateRequest.onNext(Boolean.TRUE);
        //displayUpdateRequestBuffer.queue(this::refreshSourceImage);
        //refreshSourceImage();

    }

    protected void onWindowClosed(ActionEvent event) {
        System.out.println("Closing dataset");
        if (toolService.getCurrentTool() != null) {
            toolService.getCurrentTool().unsubscribe(canvas);
        }

        datasetService.getDatasets().remove(datasetService.getDatasets(imageDisplay));
        //mageDisplayService.getActiveDataset(imageDisplay).
        imageDisplay.close();

        eventService.publishLater(new DisplayDeletedEvent(imageDisplay));
    }

    @EventHandler
    protected void onToolChangedEvent(ToolChangeEvent event) {
        setCurrentTool(event.getTool());
    }

    @EventHandler
    protected void onOverlaySelectionChanged(OverlaySelectionEvent event) {

        if (event.getDisplay() != imageDisplay) {
            return;
        }

        logger.info("Channeling event");
        bus.channel(event);

        Platform.runLater(() -> setEdited(event.getOverlay()));

    }
    
    @EventHandler
    protected void onOverlaySelectedEvent(OverlaySelectedEvent event) {
        logger.info("Overlay selected event");
        bus.channel(event);
    }

    @EventHandler
    protected void onOverlayCreated(OverlayCreatedEvent event) {
        logger.info("Overlay created");
        if (getOverlays().contains(event.getObject())) {
            //Platform.runLater(() -> updateOverlay(event.getObject()));
            bus.channel(event);

        }
    }

    @EventHandler
    void onOverlayModified(OverlayUpdatedEvent event) {
        logger.info("Overlay modified");
        if (getOverlays().contains(event.getObject())) {
            bus.channel(event);
        }
    }
    
    @EventHandler
    void onDataViewUpdated(DataViewUpdatedEvent event) {
        logger.info("DataView updated");
        if(imageDisplay.contains(event.getView())) {
            bus.channel(event);
        }
    }

    @EventHandler
    protected void onOverlayDeleted(OverlayDeletedEvent event) {
        logger.info("Overlay deleted");
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
        logger.info(event.getClass().getSimpleName());
    }
    

    protected OverlayDrawer getDrawer(Overlay overlay) {
        return drawerMap.computeIfAbsent(overlay, overlayDisplayService::createDrawer);
    }

    protected OverlayModifier getModifier(Overlay overlay) {
        return modifierMap.computeIfAbsent(overlay, overlayDisplayService::createModifier);
    }

    @EventHandler
    protected void onActiveDisplayChanged(DisplayActivatedEvent event) {
        if (event.getDisplay() == imageDisplay) {
            System.out.println("I'm activaed !" + imageDisplay.getName());
            setFocused(true);
            setCurrentTool(toolService.getCurrentTool());
        } else {
            setFocused(false);
            setCurrentTool(null);
        }
    }

    protected boolean isWindowConcernedByModule(Module module) {
        return module.getInputs().values().stream().filter(obj -> (obj == imageDisplay || obj == getDataset())).count() > 0;
    }

    protected void onMoveablePointMoved(Observable obs, Point2D oldValue, Point2D newValue) {
        getOverlays().forEach(this::updateOverlay);
    }

    protected Cursor getToolDefaultCursor() {

        if (currentTool == null) {
            return Cursor.DEFAULT;
        } else {
            return currentTool.getDefaultCursor();
        }
    }

}
