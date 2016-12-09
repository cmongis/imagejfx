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
import ijfx.core.Handles;
import ijfx.service.ui.CommandRunner;
import ijfx.ui.arcmenu.PopArcMenu;
import ijfx.ui.canvas.FxImageCanvas;
import ijfx.ui.canvas.utils.ViewPort;
import ijfx.ui.datadisplay.DisplayPanePlugin;
import ijfx.ui.datadisplay.image.overlay.DefaultOverlayViewConfiguration;
import ijfx.ui.datadisplay.image.overlay.OverlayDisplayService;
import ijfx.ui.datadisplay.image.overlay.OverlayDrawer;
import ijfx.ui.datadisplay.image.overlay.OverlayModifier;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.tool.FxTool;
import ijfx.ui.tool.FxToolService;
import ijfx.ui.tool.ToolChangeEvent;
import ijfx.ui.tool.overlay.MoveablePoint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import mongis.utils.transition.TransitionBinding;
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
import net.imglib2.RandomAccess;
import net.imglib2.display.screenimage.awt.ARGBScreenImage;
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
import org.scijava.plugin.Plugin;

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
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = DisplayPanePlugin.class)
@Handles(type = ImageDisplay.class)
public class ImageDisplayPane extends AnchorPane implements DisplayPanePlugin<ImageDisplay> {

    @FXML
    private StackPane stackPane;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Label infoLabel;

    @FXML
    private Label pixelValueLabel;

    @FXML
    private HBox buttonHBox;
    
    @FXML
    private VBox sliderVBox;
    
    @FXML
    private BorderPane bottomPane;
    
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

    @Parameter
    private Context context;

    Logger logger = ImageJFX.getLogger();

    private FxTool currentTool;

    private javafx.event.EventHandler<MouseEvent> myHandler;

    private PopArcMenu arcMenu;

    private final FxImageCanvas canvas;

    private final ExecutorService refreshExecutor = Executors.newFixedThreadPool(2);

    private Dataset dataset;

    private ImageDisplay imageDisplay;

    final HashMap<Overlay, OverlayModifier> modifierMap = new HashMap();

    private final Map<Class<?>, OverlayDrawer> drawerMap = new HashMap<>();

    private final Property<FxTool> currentToolProperty = new SimpleObjectProperty();

    private final SciJavaEventBus bus = new SciJavaEventBus();

    private final DoubleProperty pixelValueProperty = new SimpleDoubleProperty();

    private final Property<DatasetView> viewProperty = new SimpleObjectProperty();
    
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
        if (event instanceof DataViewUpdatedEvent) {
            DataViewUpdatedEvent devent = (DataViewUpdatedEvent) event;
            return devent.getView() instanceof OverlayView;
        }
        return ArrayUtils.contains(overlayEvents, event.getClass());
    }

    private final StringProperty titleProperty = new SimpleStringProperty();

    private int refreshDelay = 1000 / 24; // in milliseconds

    private AxisConfiguration axisConfig;

    public ImageDisplayPane() throws IOException {

        FXUtilities.injectFXML(this);

        canvas = new FxImageCanvas();
            
        
        getStyleClass().add("image-display-pane");
        stackPane.getChildren().add(canvas);

        canvas.setAfterDrawing(this::redrawOverlays);
        // Adding canvas related events
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onCanvasClick);

        canvas.cursorProperty().bind(Bindings.createObjectBinding(this::getToolDefaultCursor, currentToolProperty));

        canvas.getCamera().addListener(this::onViewPortChange);

        canvas.widthProperty().bind(stackPane.widthProperty());
        canvas.heightProperty().bind(stackPane.heightProperty());

        canvas.addEventHandler(KeyEvent.KEY_TYPED, this::onKeyPressed);

        canvas.positionOnImageProperty().addListener(this::onPositionOnImageChanged);

        // setting the pixel value label to a binding retrieving the value of the current pixel and displaying it
        pixelValueLabel.textProperty().bind(Bindings.createStringBinding(this::getPixelValueLabelText, pixelValueProperty));
        
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty().add(-5));
        
        setClip(clip);
        
        new TransitionBinding<Number>()
                .bindOnFalse(sliderVBox.heightProperty())
                .setOnTrue(0.0)
                .bind(bottomPane.hoverProperty(), bottomPane.translateYProperty());
                
       
        
    }

    public ImageDisplayPane(Context context) throws IOException {
        this();
        context.inject(this);
    }

    public void display(ImageDisplay display) {
        imageDisplay = (ImageDisplay) display;
        canvas.setImageDisplay(imageDisplay);
        build();
        setCurrentTool(toolService.getCurrentTool());
        initEventBuffering();

        new CommandRunner(context)
                .set("imageDisplay", imageDisplay)
                .set("dataset", imageDisplayService.getActiveDataset(display))
                .set("channelDependant",true)
                .runAsync(AutoContrast.class, null, true);

        viewProperty.setValue(imageDisplayService.getActiveDatasetView(display));
        
        
      
        
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
        if (getDataset().numDimensions() >= 2) {
            return getDataset().dimension(0);
        } else {
            return 0;
        }
    }

    private double getImageHeight() {
        if (getDataset().numDimensions() >= 2) {
            return getDataset().dimension(1);
        } else {
            return 0;
        }
    }

    public WritableImage getWrittableImage() {

        if (wi != null && (wi.getWidth() != getImageWidth() || wi.getHeight() != getImageHeight())) {
            wi = null;

        }

        if (wi == null) {
            wi = new WritableImage((int) getImageWidth(), (int) getImageHeight());
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

        final Timer t = timerService.getTimer(this.getClass());
        t.start();
        t.elapsed("since last time");

        final WritableImage image = getWrittableImage();

        final DatasetView view = imageDisplayService.getActiveDatasetView(imageDisplay);
        if (view == null) {
            return;
        }
        final ARGBScreenImage screenImage = view.getScreenImage();

        t.elapsed("getting screen image");

        final PixelWriter writer = image.getPixelWriter();
        final int[] pixels = screenImage.getData();
        final int width = new Double(image.getWidth()).intValue();
        final int height = new Double(image.getHeight()).intValue();

        writer.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);

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

        
        
    }
    private void checkAxis() {

        if(imageDisplay.size() == 0) return;
        
        if (imageDisplay.getName().equals(titleProperty.getName()) == false) {
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
                .filter(view -> view instanceof OverlayView)
                .map(view -> (OverlayView) view)
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

        
        logger.info("rebuilding");
        sliderVBox.getChildren().clear();
        
        
        
        
        for(int i = 2; i!=imageDisplay.numDimensions();i++) {
            
            AxisSlider slider = new AxisSlider(imageDisplay, i);
            
            sliderVBox.getChildren().add(slider);
            
        }
        
        createColorButtons();
        
        
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
        if (getDatasetview() == null) {
            arcMenu = null;
            return;
        }
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
        // stream intercepting all the overlay updates 
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
        Stream<OverlayView> selected = set.stream().filter(view -> view.isSelected());

        if (selected.count() == 1) {

            OverlayView view = selected.findFirst().get();
            setEdited(view.getData());
        } else {
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

    protected void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            deleteOverlay(overlaySelectionService.getSelectedOverlay(imageDisplay));
        }
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
        
        
        Platform.runLater(this::checkAxis);
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

    @EventHandler
    protected void onToolChangedEvent(ToolChangeEvent event) {
        setCurrentTool(event.getTool());
    }

    @Override
    public Pane getPane() {
        return this;
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
        long[] position = new long[imageDisplay.numDimensions()];

        getDataset().axes(axes);
        try {
            imageDisplay.localize(position);
            String positionStr
                    = IntStream.range(2, position.length)
                            .mapToObj(e -> {
                                long p = position[e];
                                long max = imageDisplay.dimension(e) - 1;
                                String axe = axes[e].type().toString();
                                return String.format("%s : %d / %d", axe, p, max);

                            })
                            .collect(Collectors.joining("   -  "));
            infoLabel.setText(String.format("%s - %d x %d - %s", imageType, width, height, positionStr));
        } catch (Exception e) {
            logger.warning("Error when updating label");
        }

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

        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        Point2D positionOnImage = canvas.getPositionOnImage(event.getX(), event.getY());
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


    public AxisConfiguration getAxisConfiguration() {
        if (axisConfig == null) {
            axisConfig = new AxisConfiguration(imageDisplay);
        }
        return axisConfig;
    }

    public void dispose() {

    }

    private void onPositionOnImageChanged(Observable obs, Point2D oldValue, Point2D position) {

        if (position == null) {
            return;
        }

        // get the pixel value async and set the property
        new CallbackTask<Point2D, Double>()
                .setInput(position)
                .run(this::getPixelValue)
                .then(pixelValueProperty::setValue)
                .start();

    }
    

    private Double getPixelValue(Point2D point) {

        final long x = Math.round(point.getX());
        final long y = Math.round(point.getY());

        DatasetView datasetView = imageDisplayService.getActiveDatasetView(imageDisplay);

        RandomAccess<? extends RealType<?>> randomAccess = datasetView.xyPlane().randomAccess();
        long[] position = new long[imageDisplay.numDimensions()];
        viewProperty.getValue().localize(position);
        position[0] = x;
        position[1] = y;
        if(x < 0 || y < 0 || x > imageDisplay.max(0) || y > imageDisplay.max(1))return 0.0;
        randomAccess.setPosition(position);
        double realDouble = randomAccess.get().getRealDouble();
        return realDouble;
    }
    
    
    private final static String PIXEL_INTEGER_FORMAT = "%.0f x %.0f = %.0f";
    
    private final static String PIXEL_FLOAT_FORMAT = "%.0f x %.0f = %.4f";
    
    private String getPixelValueLabelText() {
        double value = pixelValueProperty.doubleValue();
        Point2D position = canvas.getCursorPositionOnImage();
        String format;
        if(new Double(value).doubleValue() == new Long(Math.round(value)).doubleValue()) {
            format =  PIXEL_INTEGER_FORMAT;
        }
        else {
            format = PIXEL_INTEGER_FORMAT;
        }
        return String.format(format,position.getX(),position.getY(),value);
    }
    
    private Double getSliderTranslateY() {
        return sliderVBox.getHeight();
    }
    
    
    private void createColorButtons() {
        
        
        buttonHBox.getChildren().clear();
        
        if(getDatasetview().getChannelCount() > 1) {
            
            for(int i = 1; i!= getDatasetview().getChannelCount()+1;i++) {
                TableColorButton button = new TableColorButton();
                context.inject(button); 
                final int channel = i-1;
                button.datasetViewProperty().bind(viewProperty);
                button.channelProperty().set(channel);
               
                
               
                
                
                buttonHBox.getChildren().add(button);
                
                
           }
                
        }
        
        
        
        
    }
    
    
    
    
}
