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
import ijfx.plugins.commands.AutoContrast;
import ijfx.service.ImagePlaneService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.service.overlay.OverlaySelectedEvent;
import ijfx.service.overlay.OverlaySelectionEvent;
import ijfx.service.overlay.OverlaySelectionService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.arcmenu.PopArcMenu;
import ijfx.ui.canvas.FxImageCanvas;
import ijfx.ui.canvas.utils.ViewPort;
import ijfx.ui.datadisplay.image.overlay.DefaultOverlayViewConfiguration;
import ijfx.ui.datadisplay.image.overlay.OverlayDisplayService;
import ijfx.ui.datadisplay.image.overlay.OverlayDrawer;
import ijfx.ui.datadisplay.image.overlay.OverlayModifier;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.tool.FxTool;
import ijfx.ui.tool.FxToolService;
import ijfx.ui.tool.overlay.MoveablePoint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import net.imagej.Dataset;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.display.OverlayView;
import net.imagej.display.event.AxisPositionEvent;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.display.event.LUTsChangedEvent;
import net.imagej.event.DatasetUpdatedEvent;
import net.imagej.event.OverlayCreatedEvent;
import net.imagej.event.OverlayDeletedEvent;
import net.imagej.event.OverlayUpdatedEvent;
import net.imagej.overlay.Overlay;
import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import org.apache.commons.lang.ArrayUtils;
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

    ExecutorService refreshExecutor = Executors.newFixedThreadPool(2);

    private Dataset dataset;

    private ImageDisplay imageDisplay;

    //private final ExecutorService refreshQueue = Executors.newFixedThreadPool(1);

    private final HashMap<Overlay, OverlayModifier> modifierMap = new HashMap();

    private final Map<Class<?>, OverlayDrawer> drawerMap = new HashMap<>();

    private final Property<FxTool> currentToolProperty = new SimpleObjectProperty();

    private final SciJavaEventBus bus = new SciJavaEventBus();

    @Parameter
    LoadingScreenService loadingScreenService;
    
     private Class<?>[] displayEvents = new Class<?>[]{
        DisplayUpdatedEvent.class,
        DatasetUpdatedEvent.class,
        LUTsChangedEvent.class,
        AxisPositionEvent.class,
        DataViewUpdatedEvent.class,
        OverlayCreatedEvent.class,
        OverlayDeletedEvent.class,
        OverlayUpdatedEvent.class
    };
     
     private Class<?>[] overlayEvents = new Class<?>[]{
        OverlayUpdatedEvent.class,
         OverlayCreatedEvent.class,
         OverlaySelectionEvent.class,
         OverlaySelectedEvent.class
    };
     
    private boolean doesDisplayRequireRefresh(SciJavaEvent event) {
        //System.out.println("Event class : " + event.getClass());
        return ArrayUtils.contains(displayEvents, event.getClass());

    }

    public boolean isOverlayEvent(SciJavaEvent event) {
        if(event instanceof DataViewUpdatedEvent) {
            DataViewUpdatedEvent devent = (DataViewUpdatedEvent)event;
            return devent.getView() instanceof OverlayView;
        }
        return ArrayUtils.contains(overlayEvents, event.getClass());
    }
    
    
    private final StringProperty titleProperty = new SimpleStringProperty();

    private int refreshDelay = 1000/24; // in milliseconds

    private AxisConfiguration axisConfig;

    public ImageDisplayPane(Context context) throws IOException {

        FXUtilities.injectFXML(this);

        context.inject(this);

        canvas = new FxImageCanvas();
        
        stackPane.getChildren().add(canvas);
       
        canvas.setAfterDrawing(this::redrawOverlays);
        // Adding canvas related events
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onCanvasClick);

        canvas.cursorProperty().bind(Bindings.createObjectBinding(this::getToolDefaultCursor, currentToolProperty));

        canvas.getCamera().addListener(this::onViewPortChange);


        canvas.widthProperty().bind(stackPane.widthProperty());
        canvas.heightProperty().bind(stackPane.heightProperty());
        
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {

            if (e.getButton() == MouseButton.PRIMARY && this.getImageDisplay() != null) {
                displayService.setActiveDisplay(this.getImageDisplay());
            }
        });
    }

    private void onWidthChanged(Observable obs, Number oldValue, Number newValue) {       
        canvas.setWidth(newValue.doubleValue());
        if(oldValue.doubleValue() == 0.0) canvas.scaleToFit();
  
    }

    private void onHeightChanged(Observable obs, Number oldValue, Number newValue) {       
        canvas.setHeight(newValue.doubleValue());
    }

    public void display(ImageDisplay display) {
        imageDisplay = (ImageDisplay) display;

        build();
        setCurrentTool(toolService.getCurrentTool());
        initEventBuffering();
        canvas.setImageDisplay(imageDisplay);
        
         new CallbackTask()
                        .setName("Enhancing contrast...")
                        .run(() -> AutoContrast.run(statsService, imageDisplay, getDataset(), true))
                        .submit(loadingScreenService)
                        .setInitialProgress(0.8)
                        .start();
        
        
        
        
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
    
    private double getImageWidth() {
        return imageDisplay.dimension(0);
    }
    
    private double getImageHeight() {
        return imageDisplay.dimension(1);
    }
    
    
    public WritableImage getWrittableImage() {
        
        
        if(wi != null && (wi.getWidth() != getImageWidth() || wi.getHeight() != getImageHeight())) {
            wi = null;
            
        }
        
        if (wi == null) {
            wi = new WritableImage((int)getImageWidth(),(int)getImageHeight());
            canvas.setImage(wi);
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

        
        
        
        
        Task task = new CallbackTask<Void, Void>()
                .run(this::transformImage)
                .then(this::updateImageAndOverlays);
           
        refreshExecutor.submit(task);

    }

    private void transformImage() {

        useIjfxRender();

    }

    private <T extends RealType<T>> void useIjfxRender() {

        
        Timer t = timerService.getTimer(this.getClass());
        t.start();
        t.elapsed("since last time");

        WritableImage image = getWrittableImage();

        
       

        DatasetView view = imageDisplayService.getActiveDatasetView(imageDisplay);
        
        ARGBScreenImage screenImage = view.getScreenImage();
        
        t.elapsed("getting screen image");
        
        ArrayCursor<ARGBType> cursor = screenImage.cursor();
        
        long[] position = new long[2];
        PixelWriter writer = image.getPixelWriter();
        int[] pixels = screenImage.getData();
        int width = new Double(image.getWidth()).intValue();
        int height = new Double(image.getHeight()).intValue();
        //writer.setPixels(0, 0, image.getWidth(), image.getHeight(), PixelFormat.getIntArgbInstance(),pixels, 0, image.getWidth());
        writer.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        
        /*
        cursor.reset();
        
        while(cursor.hasNext()) {
            cursor.fwd();
            cursor.localize(position);
            writer.setArgb((int)position[0],(int)position[1],cursor.get().get());
        }*/
        t.elapsed("pixel transformation");

    }

    synchronized private void useImageJRender() {

        Timer t = timerService.getTimer(this.getClass());
        t.start();

        logService.info("Refreshing source image " + imageDisplay.getName());
        t.elapsed("in between");
        //Retrieving buffered image from the dataset view
        
        DatasetView datasetView = getDatasetview();
        
        ARGBScreenImage screenImage = datasetView.getScreenImage();
        
        BufferedImage bf = screenImage.image();

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
    private synchronized void repaint() {
        
        
        
        Timer t = timerService.getTimer(this.getClass());
        canvas.repaint();
        t.elapsed("canvas repainting");
    }
    private void updateImageAndOverlays(Void v) {

        
        
        
        // Timer t = timerService.getTimer(this.getClass());
        repaint();
        
        if(imageDisplay.getName().equals(titleProperty.getName()) == false) {
            titleProperty.setValue(imageDisplay.getName());
        }
        
        
        
        // t.elapsed("canvas.repaint");
        updateInfoLabel();
        
        if (getAxisConfiguration().equals(imageDisplay) == false) {
            axisConfig = new AxisConfiguration(imageDisplay);
            build();
        }
        
        
    }

    public void redrawOverlays(FxImageCanvas canvas) {

        
        Timer t1 = timerService.getTimer(this.getClass());
        
        
        t1.start();
       
        List<OverlayView> overlays = imageDisplay
                .stream()
                .parallel()
                .filter(view->view instanceof OverlayView)
                .map(view->(OverlayView)view)
                .collect(Collectors.toList());
        
        canvas.getGraphicsContext2D().setFill(Color.RED);
        canvas.getGraphicsContext2D().setStroke(Color.RED);
        logger.info("Redrawing overlays");
        
        for (OverlayView view : overlays) {
            Overlay overlay = view.getData();
             OverlayDrawer drawer = getDrawer(overlay);   
            if (drawer.isOverlayOnViewPort(overlay, canvas.getCamera())) {
                drawer.update(new DefaultOverlayViewConfiguration(view, overlay), canvas.getCamera(), canvas);
            }
        }
        
        t1.elapsed("Drawing all overlay");

    }

    private void deleteOverlay(Overlay overlay) {

        if (getModifier(overlay) != null) {
            anchorPane.getChildren().remove(getModifier(overlay).getModifiers(canvas.getCamera(), overlay));
        }
        modifierMap.remove(overlay);
    }

    private void deleteOverlays(List<Overlay> overlay) {
        overlay.forEach(this::deleteOverlay);
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
    }

    /**
     *
     * JavaFX Events
     *
     */
    protected void initEventBuffering() {

        // stream intercepting all event causing a refresh of the display
        bus.getStream(SciJavaEvent.class)
                .filter(this::doesDisplayRequireRefresh)
                .buffer(refreshDelay, TimeUnit.MILLISECONDS)
                .filter(list -> !list.isEmpty())
                .subscribe(list -> {
                    logService.info(String.format("%s events that require image refresh", list.size()));
                    list.forEach(event -> System.out.println(event.getClass().getSimpleName()));
                    refreshSourceImage();
                    
                    
                });
       
        /**
         * bus.getStream(OverlayCreatedEvent.class)
         * .map(event->event.getObject())
         * //.filter(overlay->overlayService.getOverlays(getImageDisplay()).contains(overlay))
                .subscribe(this::updateOverlay);
         */
        
        bus.getStream(DataViewUpdatedEvent.class)
                .map(dataviewEvent -> dataviewEvent.getView())
                .filter(dataview -> dataview instanceof OverlayView)
                .cast(OverlayView.class)
                .buffer(refreshDelay, TimeUnit.MILLISECONDS)
                .filter(list -> !list.isEmpty())
                .map(list -> new HashSet<OverlayView>(list))
                .subscribe(this::treatOverlayViewUpdatedEvent);

        // stream intercepting all the event causing a overlay to be deleted
        bus.getStream(OverlayDeletedEvent.class)
                .map(event -> event.getObject())
                .buffer(refreshDelay, TimeUnit.MILLISECONDS)
                .filter(list -> !list.isEmpty())
                .subscribe(this::deleteOverlays);
    }
    
    protected void treatOverlayViewUpdatedEvent(Set<OverlayView> set) {
        Stream<OverlayView> selected = set.stream().filter(view->view.isSelected());
                   
                    if(selected.count() == 1) {
                        
                        OverlayView view = selected.findFirst().get();
                        setEdited(view.getData());
                    }
                    else {
                        setEdited(null);
                    }
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
       repaint();
        
    }

    public void onViewPortChange(ViewPort viewport) {
       
        repaint();
        
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
        //getOverlays().forEach(this::updateOverlay);
        repaint();
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
            
            bus.channel(event);

        }
    }

    @EventHandler
    void onOverlayModified(OverlayUpdatedEvent event) {
        logService.info("Overlay modified");
        if (getOverlays().contains(event.getObject())) {
            Platform.runLater(canvas::repaint);
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
        bus.channel(event);
        setEdited(null);

    }
    
    @EventHandler
    public void onDatasetUpdated(DatasetUpdatedEvent event) {
        bus.channel(event);
    }
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
        //logger.info("Searching a drawer for "+overlay.getClass().getSimpleName());
        if (drawerMap.get(overlay.getClass()) == null) {
            OverlayDrawer drawer = overlayDisplayService.createDrawer(overlay.getClass());
            if (drawer == null) {
                logger.warning("No overlay compatible for " + overlay.getClass().getSimpleName());
                return null;
            } else {
                drawerMap.put(overlay.getClass(), drawer);
                return drawer;
            }
        } else {
            return drawerMap.get(overlay.getClass());
        }
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

        boolean wasOverlaySelected;
        

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
        repaint();
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
            axisConfig = new AxisConfiguration(imageDisplay);
        }
        return axisConfig;
    }

    private class AxisConfiguration {

        private CalibratedAxis[] axes;

        public AxisConfiguration(ImageDisplay dataset) {

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
            if (object instanceof ImageDisplay) {
                return equals(new AxisConfiguration((ImageDisplay) object));
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
