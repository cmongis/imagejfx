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

import ijfx.ui.arcmenu.ArcMenu;
import ijfx.ui.canvas.FxImageCanvas;
import ijfx.ui.tool.FxTool;
import ijfx.ui.tool.ToolChangeEvent;
import ijfx.ui.tool.DefaultFxToolService;
import ijfx.service.uicontext.UiContextCalculatorService;
import ijfx.service.overlay.OverlayDrawingService;
import ijfx.service.overlay.OverlaySelectionEvent;
import ijfx.service.overlay.OverlaySelectionService;
import ijfx.service.overlay.PixelDrawer;
import ijfx.ui.canvas.utils.ViewPort;
import ijfx.ui.datadisplay.image.overlay.OverlayDrawerService;
import ijfx.ui.datadisplay.image.overlay.OverlayModifier;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.tool.overlay.MoveablePoint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import jfxtras.scene.control.window.CloseIcon;
import jfxtras.scene.control.window.Window;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.display.event.AxisPositionEvent;
import net.imagej.display.event.LUTsChangedEvent;
import net.imagej.event.DataUpdatedEvent;
import net.imagej.event.DatasetUpdatedEvent;
import net.imagej.event.OverlayUpdatedEvent;
import net.imagej.lut.LUTService;
import net.imagej.overlay.Overlay;

import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import org.controlsfx.control.HiddenSidesPane;

import org.scijava.Context;

import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayActivatedEvent;
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

    private DatasetView datasetView;

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
    private LogService logService;

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
    private OverlayDrawingService overlayDrawer;

    @Parameter
    private PluginService pluginService;

    @Parameter
    private OverlayDrawerService overlayDrawerService;

    FxTool currentTool;

    /*
            JavaFX Nodes
     */
    HiddenSidesPane hiddenSidePane = new HiddenSidesPane();
    BorderPane borderPane = new BorderPane();
    FxImageCanvas canvas = new FxImageCanvas();
    AnchorPane anchorPane = new AnchorPane();

    HBox hbox = new HBox();

    HBox bottomHBox = new HBox();

    Label infoLabel = new Label("Some text as example I guess");

    ArcMenu arcMenu;

    static String TITLE_CLASS_NAME = "image-window-titlebar";

    static String WINDOW_CLASS_NAME = "image-window";

    static String INFO_LABEL_CLASS_NAME = "info-label";

    private Color selectedOverlayColor = Color.BLUE;
    private Color overlayColor = Color.YELLOW;

    public ImageWindow() {
        super();
        logger.info("Creating window.");
        setContentPane(borderPane);

        getStyleClass().add(WINDOW_CLASS_NAME);
        setTitleBarStyleClass(TITLE_CLASS_NAME);

        hiddenSidePane.setContent(anchorPane);
        borderPane.setCenter(hiddenSidePane);

        anchorPane.getChildren().add(canvas);
        //hbox.getChildren().add(canvas);

        // Bind canvas size to stack pane size.
        canvas.widthProperty().bind(anchorPane.widthProperty());
        canvas.heightProperty().bind(anchorPane.heightProperty());

        // Adding clicking listening
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onCanvasClick);

        // setting the window movable
        setMovable(true);

        // make sure that the window is put in front in the model when an event happens
        // on the canvas
        hbox.onMousePressedProperty().addListener(event -> putInFront());

        // make sure to repaint the canvas when resizing the window
        widthProperty().addListener((obj, old, nw) -> canvas.repaint());

        // I forgot why but it must be important
        anchorPane.setMinSize(0, 0);

        // close icon
        CloseIcon closeIcon = new CloseIcon(this);

        getRightIcons().add(closeIcon);

        closeIcon.onActionProperty().addListener(event -> {
            toolService.getCurrentTool().unsubscribe(canvas);
            imageDisplay.close();

        });

        setPrefSize(300, 300);

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
            arcMenu.detachFrom(anchorPane);
            arcMenu = null;
        }
        arcMenu = new ArcMenu();

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
        arcMenu.attachedTo(anchorPane);

        refreshSourceImage();
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

    WritableImage wi;

    public WritableImage getWrittableImage() {
        if (wi == null) {
            wi = new WritableImage(getDatasetview().getPreferredWidth(), getDatasetview().getPreferredHeight());
            canvas.setImage(wi);

        }
        return wi;
    }

    ARGBScreenImage currentScreenImage;

    public DisplayService getDisplayService() {
        return displayService;
    }

    public ImageDisplayService getImageDisplayService() {
        return imageDisplayService;
    }

    public ImageDisplay getImageDisplay() {
        return imageDisplay;
    }

    public synchronized void refreshSourceImage() {

        if (checkServices()) {

            logger.info("Refreshing source image");

            //Retrieving buffered image from the dataset view
            BufferedImage bf = getDatasetview().getScreenImage().image();

            // getting a writableImage (previously created or not)
            WritableImage writableImage = getWrittableImage();

            // if the size of the images are different, the buffer is recreated;
            if (writableImage.getWidth() != canvas.getCamera().getImageSpace().getWidth() || writableImage.getHeight() != canvas.getCamera().getImageSpace().getHeight()) {

                logger.info(String.format("Image size has changed :  %.0f x %.0f", writableImage.getWidth(), writableImage.getHeight()));

                // force buffer recreation
                wi = null;
                writableImage = getWrittableImage();
            }

            SwingFXUtils.toFXImage(bf, writableImage);

            canvas.repaint();

            updateInfoLabel();

            updateOverlays();

        }
    }

    public void updateOverlays() {
        getOverlays().forEach(this::updateOverlay);
    }

    public void updateOverlay(Overlay overlay) {

        Platform.runLater(() -> {
            try {
                Node node = overlayDrawerService.getDrawer(overlay).update(overlay, canvas.getCamera());
                node.setMouseTransparent(true);

                if (anchorPane.getChildren().contains(node) == false) {

                    anchorPane.getChildren().add(node);
                }

                if (overlaySelectionService.getSelectedOverlays(imageDisplay).contains(overlay)) {
                    Shape shape = (Shape) node;
                    shape.setFill(shape.getStroke());
                }

            } catch (NullPointerException e) {
                logger.log(Level.WARNING, "Couldn't draw overlay. Probably because of a lack of Drawer", e);
            }
        });
    }

    // add the MoveablePoints of a overlay in order to edit it
    private void setEdited(Overlay overlay) {

        // delete all the moveable points
        Node[] nodes = anchorPane.getChildren().stream().filter(node -> MoveablePoint.class.isAssignableFrom(node.getClass())).toArray(size -> new Node[size]);
        anchorPane.getChildren().removeAll(nodes);

        // if an overlay has been selected
        if (overlay != null) {
            // it get the modifier which will returns the set of moveable points

            OverlayModifier modifier = overlayDrawerService.getModifier(overlay);
            if (modifier == null) {
                logger.warning("No modifier was found for this overlay " + overlay.getClass().getSimpleName());
                return;
            }
            List<MoveablePoint> modifiers = modifier.getModifiers(canvas.getCamera(), overlay);

            modifiers.forEach(m -> m.positionOnImageProperty().addListener(this::onMoveablePointMoved));
            anchorPane.getChildren().addAll(modifiers);

        }

    }

    public void onViewPortChange(ViewPort viewport) {
        updateOverlays();
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
        System.out.println();
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

    private interface ColoredPixelDrawer extends PixelDrawer {

        public void setColor(Color color);

        public Color getColor();
    }

    private class WritableImagePixelDrawer implements ColoredPixelDrawer {

        Color color;
        WritableImage image;

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public WritableImage getImage() {
            return image;
        }

        public void setImage(WritableImage image) {
            this.image = image;
        }

        public WritableImagePixelDrawer(WritableImage image, Color color) {
            setColor(color);
            setImage(image);
        }

        @Override
        public void drawPixel(long x, long y) {
            image.getPixelWriter().setColor((int) x, (int) y, color);

        }

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
        };

        if (!wasOverlaySelected) {
            setEdited(null);
        }
        contextCalculationService.determineContext(imageDisplay);
        refreshSourceImage();
        updateInfoLabel();

    }

    @EventHandler
    protected void onEvent(final AxisPositionEvent event) {

        if (imageDisplay != event.getDisplay()) {
            logger.info("Ignoring event");
            return;
        }
        logger.info("Changing a axis");
        imageDisplay.update();
        refreshSourceImage();
        //refreshSourceImage();
    }

    @EventHandler
    protected void onEvent(LUTsChangedEvent event) {

        if (event.getView() != getDatasetview()) {
            return;
        }
        logger.info("LUTsChangedEvent");
        //  System.out.println(event.getView());
        // System.out.println(getDatasetview());
        //imageDisplay.update();
        imageDisplayService.getActiveDataset(imageDisplay).update();
        Platform.runLater(() -> refreshSourceImage());

    }

    @EventHandler
    protected void onEvent(DatasetUpdatedEvent event) {
        //imageDisplay.update();

        logger.info("DatasetChangedEvent : " + event.getObject());
        refreshSourceImage();
    }

    @EventHandler
    protected void onEvent(DisplayUpdatedEvent event) throws InterruptedException {
        // imageDisplay.update();

        if (event.getDisplay() != imageDisplay) {
            return;
        }
        logger.info("DisplayUpdatedEvent");

        refreshSourceImage();
    }

    @EventHandler
    protected void onEvent(DataUpdatedEvent event) {
        logger.info("DataUpdatedEvent");
        //imageDisplay.update();

        refreshSourceImage();

    }

    @EventHandler
    protected void onEvent(ToolChangeEvent event) {
        setCurrentTool(event.getTool());
    }

    @EventHandler
    protected void onOverlaySelectionChanged(OverlaySelectionEvent event) {
        System.out.println("selection changed");
        System.out.println(event.getOverlay());

        Platform.runLater(() -> setEdited(event.getOverlay()));

        refreshSourceImage();
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

    @EventHandler
    void onOverlayModified(OverlayUpdatedEvent event) {
        updateOverlay(event.getObject());
    }
    
    

    protected boolean isWindowConcernedByModule(Module module) {
        return module.getInputs().values().stream().filter(obj -> (obj == imageDisplay || obj == getDataset())).count() > 0;
    }

    protected void onMoveablePointMoved(Observable obs, Point2D oldValue, Point2D newValue) {
        updateOverlays();
    }

    /*
    @EventHandler
    protected void onEvent(ModuleStartedEvent event) {
        if(isWindowConcernedByModule(event.getModule())) {
            Platform.runLater(()->LoadingScreen.getInstance().showOn(anchorPane));
        }
    }
    
    @EventHandler void onEvent(ModuleFinishedEvent event) {
        if(isWindowConcernedByModule(event.getModule()) ) {
            Platform.runLater(()->LoadingScreen.getInstance().hideFrom(anchorPane));
        }
    }
    
      @EventHandler void onEvent(ModuleCanceledEvent event) {
        if(isWindowConcernedByModule(event.getModule()) ) {
           Platform.runLater(()->LoadingScreen.getInstance().hideFrom(anchorPane));
        }
    }*/
}
