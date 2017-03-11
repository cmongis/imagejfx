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
package ijfx.ui.main;

import ijfx.ui.context.animated.AnimatedPaneContextualView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.bridge.ImageJContainer;
import ijfx.service.notification.Notification;
import ijfx.service.notification.NotificationEvent;
import ijfx.service.ui.AppService;
import ijfx.service.uicontext.UiContextService;
import ijfx.service.log.DefaultLoggingService;
import ijfx.service.ui.FontEndTaskSubmitted;
import ijfx.service.ui.HintService;
import ijfx.service.ui.JsonPreferenceService;
import ijfx.service.ui.hint.DefaultHint;
import ijfx.service.ui.hint.Hint;
import ijfx.service.ui.hint.HintRequestEvent;
import ijfx.service.uiplugin.UiPluginReloadedEvent;
import java.util.HashMap;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import net.imagej.ImageJ;
import org.controlsfx.control.Notifications;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleListProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import mongis.utils.BindingsUtils;
import ijfx.ui.UiPlugin;
import ijfx.ui.UiConfiguration;
import ijfx.service.uiplugin.UiPluginService;
import ijfx.ui.IjfxCss;
import ijfx.ui.UiContexts;
import ijfx.ui.UiPluginSorter;
import ijfx.ui.activity.Activity;
import ijfx.ui.activity.ActivityChangedEvent;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.context.ContextualWidget;
import ijfx.ui.plugin.DebugEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import javafx.animation.Transition;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import mongis.utils.MemoryUtils;
import ijfx.ui.context.animated.Animations;
import ijfx.ui.correction.FolderSelection;
import ijfx.ui.explorer.ExplorerActivity;
import ijfx.service.ui.RichTextDialog;
import ijfx.service.ui.RichTextDialog.Answer;
import ijfx.service.ui.UIExtraService;
import ijfx.service.usage.Usage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import mongis.utils.AnimationChain;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import mongis.utils.ProgressHandler;
import mongis.utils.TaskList2;
import mongis.utils.TextFileUtils;
import mongis.utils.transition.TransitionBinding;
import net.mongis.usage.UsageLocation;
import net.mongis.usage.UsageType;
import org.scijava.app.StatusService;
import org.scijava.plugin.PluginService;

/**
 * FXML Controller class
 *
 * @author Cyril MONGIS, 2015
 */
public class MainWindowController extends AnchorPane {

    public static int ANIMATION_DURATION = 300;

    private static final int MEMORY_REFRESH_RATE = 1000;
    /**
     * Initializes the controller class.
     */

    @FXML
    private HBox topLeftHBox;

    @FXML
    private HBox topCenterHBox;

    @FXML
    private HBox topRightHBox;

    @FXML
    private VBox leftVBox;

    @FXML
    private VBox rightVBox;

    @FXML
    private HBox bottomLeftHBox;

    @FXML
    private HBox bottomRightHBox;

    @FXML
    private HBox bottomCenterHBox;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private VBox topToolBarVBox;

    //@FXML
    //private StackPane centerStackPane;
    @FXML
    private BorderPane sideMenu;

    @FXML
    private VBox sideMenuMainTopVBox;

    @FXML
    private VBox sideMenuTopVBox;

    @FXML
    private VBox sideMenuBottomVBox;

    @FXML
    private ProgressBar memoryProgressBar;

    @FXML
    private Label memoryLabel;

    //protected final LoadingScreen loadingScreen = LoadingScreen.getInstance();
    protected ImageJ imageJ;

    protected final Logger logger = ImageJFX.getLogger();

    HashMap<String, AnimatedPaneContextualView> uiPluginCtrl = new HashMap<>();

    protected BooleanBinding isSideMenuHidden;

    @Parameter
    private UiPluginService uiPluginService;

    private MainWindowController thisController;

    @Parameter
    private UiContextService uiContextService;

    @Parameter
    private AppService appService;

    @Parameter
    private DefaultLoggingService logErrorService;

    @Parameter
    private HintService hintService;

    @Parameter
    private ActivityService activityService;

    @Parameter
    private JsonPreferenceService jsonPrefSrv;

    @Parameter
    private PluginService pluginSrv;

    @Parameter
    private UIExtraService uiExtraService;

    LoadingPopup loadingPopup = new LoadingPopup(ImageJFX.PRIMARY_STAGE);

    Queue<Hint> hintQueue = new LinkedList<>();

    TaskList2 taskList = new TaskList2();

    boolean isHintDisplaying = false;

    BooleanProperty menuActivated = new SimpleBooleanProperty(false);

    @Parameter
    StatusService statusService;

    private Thread memoryThread = new Thread(() -> {

        while (true) {

            Platform.runLater(() -> updateMemoryUsage());
            try {
                Thread.sleep(MEMORY_REFRESH_RATE);
            } catch (Exception e) {
            }
        }

    });

    public MainWindowController() throws IOException {

        FXUtilities.injectFXML(this, "/ijfx/ui/main/MainWindow.fxml");

        Font.loadFont(FontAwesomeIcon.class.getResource("fontawesome-webfont.ttf").toExternalForm(), 16);

        // binding the sides to the pseudo class empty
        final PseudoClass empty = PseudoClass.getPseudoClass("empty");
        final PseudoClass hidden = PseudoClass.getPseudoClass("hidden");

        BindingsUtils.bindNodeToPseudoClass(empty, leftVBox, new SimpleListProperty<Node>(leftVBox.getChildren()).emptyProperty());
        BindingsUtils.bindNodeToPseudoClass(empty, rightVBox, new SimpleListProperty<Node>(rightVBox.getChildren()).emptyProperty());
        BindingsUtils.bindNodeToPseudoClass(hidden, sideMenu, Bindings.createBooleanBinding(() -> sideMenu.getTranslateX() <= -1.0 * sideMenu.getWidth() + 2, sideMenu.translateXProperty()));

        leftVBox.pseudoClassStateChanged(empty, true);

        Bindings.isEmpty(rightVBox.getChildren()).addListener((obs, oldValue, newValue) -> {
            rightVBox.pseudoClassStateChanged(empty, newValue);
        });

        //loadingScreen.setDefaultPane(mainAnchorPane);
        sideMenu.setTranslateZ(0);
        sideMenu.setTranslateX(-100);
        sideMenu.setPrefWidth(30);
        memoryThread.start();

        /*
            The boot sequence is the following
            1. start imagej
            2. inject the context
            3. register contextuals viewx
            4. load plugins
            5. register plugins to the context wideget
            6. finish the start
         */
        mainBorderPane.setOpacity(1.0);
        mainBorderPane.setCenter(new Label("Loading..."));

        loadingPopup.taskProperty().bind(taskList.foregroundTaskProperty());

        memoryProgressBar.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMemoryProgressBarClicked);

    }

    private Scene myScene;

    public void setScene(Scene scene) {
        myScene = scene;
    }

    public void init() {

        CallbackTask task = new CallbackTask<Void, Boolean>()
                .runLongCallable(this::init)
                .then(this::finishInitialization)
                .error(this::onError);

        Thread t = new Thread(task);
        t.setContextClassLoader(ClassLoader.getSystemClassLoader());
        t.start();

        loadingPopup
                .setCanCancel(false)
                .closeOnFinished()
                .attachTo(this.getScene());

        taskList.submitTask(task);

    }

    public void onError(Throwable t) {
        ImageJFX.getLogger().log(Level.SEVERE, "Error when starting ImageJ", t);

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);

        new Alert(Alert.AlertType.ERROR, sw.getBuffer().toString(), ButtonType.CLOSE).show();
    }

    public Boolean init(ProgressHandler handler) {
        handler.setStatus("Initializing ImageJ....");
        handler.setProgress(1, 3);

        logger.info(String.format("Class loader : %s", Thread.currentThread().getContextClassLoader().getClass().getSimpleName()));
        imageJ = new ImageJ();

        // service.removePlugin(service.getPlugin(Binarize.class));
        handler.setProgress(2, 3);
        handler.setStatus("ImageJ initialized.");
        imageJ.getContext().inject(this);

        removeBlacklisted();

        registerWidgetControllers();
        handler.setProgress(3, 3);
        uiPluginService.loadAll(handler).forEach(this::installPlugin);
        //finishInitialization(this);
        return true;
    }

    private void installPlugin(UiPlugin uiPlugin) {
        UiConfiguration infos = uiPluginService.getInfos(uiPlugin);

        if (infos == null) {
            logger.warning("No informations for " + uiPlugin.getClass().getName());
            return;
        }

        uiContextService.link(infos.id(), infos.context());
        loadWidget(uiPlugin);
    }

    protected void finishInitialization(Object o) {

        logger.info("finishing initialization...");
        // entering the right context
        uiContextService.enter(UiContexts.list(UiContexts.DEBUG), "visualize");

        // showing the intro app
        // updating the context
        uiContextService.update();
        activityService.openByType(ImageJContainer.class);
        // sequence over

        initMenuAction();
        new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(sideMenu.translateXProperty(), 0.0))).play();
        logger.info("Start over");

        new GifRecorder(this).setNotifier(statusService::showStatus);

        if (Usage.factory().hasDecided() == false) {

            // asking for usage things
            Answer answer = uiExtraService
                    .createRichTextDialog()
                    .setDialogTitle("Help us improving ImageJFX")
                    .loadContent(getClass(), "/USAGE_CONDITION.md")
                    .setContentType(RichTextDialog.ContentType.MARKDOWN)
                    .addAnswerButton(RichTextDialog.AnswerType.CANCEL, "I decline")
                    .addAnswerButton(RichTextDialog.AnswerType.VALIDATE, "I accept, I want to help")
                    .showDialog();
            
            Usage.factory().setDecision(answer.isPositive());
            
        }

    }

    public void registerWidgetControllers() {
        registerPaneCtrl(topLeftHBox)
                .setAnimationOnHide(Animations.DISAPPEARS_LEFT)
                .setAnimationOnShow(Animations.APPEARS_LEFT);

        registerPaneCtrl(topRightHBox)
                .setAnimationOnHide(Animations.DISAPPEARS_UP)
                .setAnimationOnShow(Animations.APPEARS_UP);

        registerPaneCtrl(leftVBox)
                .setAnimationOnHide(Animations.DISAPPEARS_LEFT)
                .setAnimationOnShow(Animations.APPEARS_LEFT);

        registerPaneCtrl(rightVBox)
                .setAnimationOnHide(Animations.DISAPPEARS_RIGHT)
                .setAnimationOnShow(Animations.APPEARS_RIGHT);

        registerPaneCtrl(bottomLeftHBox)
                .setAnimationOnHide(Animations.DISAPPEARS_DOWN)
                .setAnimationOnHide(Animations.APPEARS_DOWN);

        registerPaneCtrl(bottomRightHBox)
                .setAnimationOnHide(Animations.DISAPPEARS_DOWN)
                .setAnimationOnShow(Animations.APPEARS_DOWN);

        /*
        registerPaneCtrl(centerStackPane)
                .setAnimationOnHide(Animations.FADEOUT)
                .setAnimationOnShow(Animations.FADEIN);*/
        registerPaneCtrl(topCenterHBox)
                .setAnimationOnHide(Animations.DISAPPEARS_UP)
                .setAnimationOnShow(Animations.APPEARS_UP);

        registerPaneCtrl(bottomCenterHBox)
                .setAnimationOnShow(Animations.APPEARS_DOWN)
                .setAnimationOnHide(Animations.DISAPPEARS_DOWN);

        registerPaneCtrl(topToolBarVBox)
                .setAnimationOnShow(Animations.FADEIN)
                .setAnimationOnHide(Animations.FADEOUT);
    }

    private AnimatedPaneContextualView registerPaneCtrl(Pane node) {

        // The UI plugin service implements the interface used to sort nodes inside the contextual containers
        UiPluginSorter sorter = uiPluginService;

        AnimatedPaneContextualView ctrl = new AnimatedPaneContextualView(sorter, node)
                .setOnUiPluginDisplayed(this::onUiPluginDisplaed);

        uiContextService.addContextualView(ctrl);
        uiPluginCtrl.put(node.getId(), ctrl);
        return ctrl;

    }

    protected void updateMemoryUsage() {

        if (memoryLabel == null) {
            return;
        }

        int free = (int) MemoryUtils.getAvailableMemory();
        int max = (int) MemoryUtils.getTotalMemory();
        int used = (max - free);

        double progress = 1.0 * (used) / max;

        // System.out.println(progress);
        memoryProgressBar.setProgress(progress);
        memoryLabel.setText(String.format("%d / %d MB", used, max));

    }

    protected void loadWidget(UiPlugin uiPlugin) {

        // getting the localization from the Localization Plugin (which gets
        // it from the annotation
        String localization
                = uiPluginService.getLocalization(uiPlugin);

        if (uiPluginCtrl.containsKey(localization) == false) {
            String message = String.format(
                    "The localization '%s' is not registered", localization
            );
            logger.warning(message);
            imageJ.ui().showDialog(message);
            return;
        }

        // register the node the controller
        uiPluginCtrl.get(localization).registerNode(uiPlugin.getUiElement());
    }

    @EventHandler
    public void handleEvent(UiPluginReloadedEvent event) {
        Platform.runLater(() -> loadWidget(event.getWidget()));
    }

    @EventHandler
    public void handleEvent(DebugEvent event) {

        Platform.runLater(() -> showHelpSequence(new DefaultHint().setTarget("#debug-button").setText("This is a very long text which doesn't really matter !")));
    }

    @EventHandler
    public void handleEvent(NotificationEvent event) {

        showNotification(event.getNotification());
    }

    @EventHandler
    public synchronized void onHintRequested(HintRequestEvent event) {

        for (Hint hint : event.getHintList()) {
            queueHint(hint);
        }

        //hintQueue.addAll(event.getHintList());
        Platform.runLater(this::nextHint);
    }

    private synchronized void queueHint(Hint hint) {
        logger.info(String.format("Queuing hint %s (%d already in the queue)", hint.getId(), hintQueue.size()));
        if (hintQueue.stream().filter(hint2 -> hint.getId().equals(hint2.getId())).count() == 0) {
            hintQueue.add(hint);
        }

    }

    @EventHandler
    public void onActivityChanged(ActivityChangedEvent event) {
        System.out.println("?");
        logger.info("Activity changed : " + event.toString());
        Task updateTask = event.getActivity().updateOnShow();
        Runnable runnable = updateTask;
        if (runnable == null) {
            runnable = () -> {
            };

        }
        Platform.runLater(() -> mainBorderPane.setCenter(event.getActivity().getContent()));
        new AnimationChain()
                .animate(mainBorderPane.getCenter(), Animations.FADEOUT)
                .then(runnable)
                .thenInFXThread(() -> mainBorderPane.setCenter(event.getActivity().getContent()))
                .animate(event.getActivity().getContent(), Animations.FADEIN)
                .execute();

    }

    @EventHandler
    public void onFrontEndTaskSubmitted(FontEndTaskSubmitted event) {

        if (event.getObject() != null) {
            Platform.runLater(() -> {
                System.out.println("front end task submitted");
                if (event.getObject() == null) {
                    return;
                }
                taskList.submitTask(event.getObject());
            });
        }
    }

    private void showNotification(Notification notification) {

        Platform.runLater(() -> {
            Notifications ns = Notifications
                    .create()
                    .title(notification.getTitle())
                    .text(notification.getText());

            ns.position(Pos.TOP_RIGHT);
            ns.hideAfter(Duration.seconds(120));

            ns.showInformation();

        });
    }

    public void onUiPluginDisplaed(ContextualWidget<Node> uiPlugin) {

        hintService.displayHints(uiPlugin.getObject().getClass(), false);

    }

    private void onMemoryProgressBarClicked(MouseEvent event) {
        System.gc();
    }

    public synchronized void nextHint() {

        if (isHintDisplaying) {
            return;
        }
        if (hintQueue.size() > 0) {
            showHelpSequence(hintQueue.peek());
        }

    }

    public synchronized void showHelpSequence(Hint hint) {

        if (hint == null) {

            return;
        }

        Node node = mainAnchorPane.getScene().lookup(hint.getTarget());

        try {
            isHintDisplaying = true;
            double hintWidth = 200;
            double hintMargin = 20;
            double rectanglePadding = 5;

            final double sceneHeight = getScene().getHeight();
            final double sceneWidth = getScene().getWidth();
            final double finalX;
            final double finalY;

            Button gotItButton = new Button("Got it !");
            // rectangle representing the highlighted node
            Rectangle rectangle;
            if (node != null) {
                Bounds nodeBounds = node.getLocalToSceneTransform().transform(node.getLayoutBounds());
                rectangle = new Rectangle(nodeBounds.getMinX() - rectanglePadding, nodeBounds.getMinY() - rectanglePadding, nodeBounds.getWidth() + rectanglePadding * 2, nodeBounds.getHeight() + rectanglePadding * 2);

            } else {
                rectangle = new Rectangle(getWidth() / 2 + 100, 150, 0, 0);
            }

            Rectangle bigOne = new Rectangle(0, 0, sceneWidth, sceneHeight);

            Shape highligther = Path.subtract(bigOne, rectangle);
            highligther.setFill(Paint.valueOf("black"));
            highligther.setOpacity(0.7);

            mainAnchorPane.getChildren().add(highligther);
            Label label = new Label(hint.getText());
            label.setPadding(new Insets(hintMargin));
            label.getStyleClass().add("help-label");
            label.setMaxWidth(hintWidth);
            label.setWrapText(true);
            label.setPrefWidth(hintWidth);

            Callable<Double> nextToNodeX = () -> {

                Bounds rectangleBounds;
                rectangleBounds = rectangle.getBoundsInLocal();

                Callable<Double> toTheLeft = () -> rectangleBounds.getMaxX() + hintMargin + highligther.getTranslateX();
                Callable<Double> toTheRight = () -> rectangleBounds.getMinX() - hintWidth - hintMargin + highligther.getTranslateX();

                Callable<Boolean> putToRight = () -> rectangleBounds.getMinX() - label.getWidth() - hintMargin < 0;

                return putToRight.call() ? toTheLeft.call() : toTheRight.call();

                /// return null;
            };

            Callable<Double> nextToNodeY = () -> {
                Bounds rectangleBounds;
                rectangleBounds = rectangle.getBoundsInLocal();

                Callable<Double> onTop = () -> rectangleBounds.getMaxY() - label.getHeight() - gotItButton.getHeight() - (hintMargin / 2);
                Callable<Double> under = () -> rectangleBounds.getMinY();

                Callable<Boolean> putOnTop = () -> rectangleBounds.getMinY() + label.getHeight() + (hintMargin) + gotItButton.getHeight() > sceneHeight;

                return putOnTop.call() ? onTop.call() : under.call();
            };

            Callable<Double> saveLevelX = () -> {
                Bounds rectangleBounds;
                rectangleBounds = rectangle.getBoundsInLocal();

                return rectangleBounds.getMinX();
            };

            Bounds rectangleBounds;
            rectangleBounds = rectangle.getBoundsInLocal();

            // callable determining the final position of the box
            // generics:
            //Callable<Double> verticalCenter = () -> 1d * (sceneWidth +  label.getWidth()) / 2;
            //Callable<Double> horizontalCenter = () -> 1d * (sceneHeight + label.getHeight()+hintMargin+gotItButton.getHeight())/2;
            Callable<Double> toTheLeft = () -> rectangleBounds.getMaxX() + hintMargin + highligther.getTranslateX();
            Callable<Double> toTheRight = () -> rectangleBounds.getMinX() - hintWidth - hintMargin + highligther.getTranslateX();

            Callable<Double> onTop = () -> rectangleBounds.getMaxY() - label.getHeight() - gotItButton.getHeight() - (hintMargin / 2);
            Callable<Double> under = () -> rectangleBounds.getMinY();

            // deciding the last position
            Callable<Boolean> putToRight = () -> rectangleBounds.getMinX() - label.getWidth() - hintMargin < 0;
            Callable<Boolean> putOnTop = () -> rectangleBounds.getMinY() + label.getHeight() + (hintMargin) + gotItButton.getHeight() > sceneHeight;

            Callable<Double> xPosition = nextToNodeX;
            Callable<Double> yPosition = nextToNodeY;

            if (rectangle.getBoundsInLocal().getWidth() > sceneWidth * 0.8) {
                xPosition = saveLevelX;
                yPosition = () -> rectangleBounds.getMaxY() + hintMargin;
            }

            // correcting
            // if (rectangleBounds.getWidth() > sceneWidth * 0.9) {
            //   xPosition = horizontalCenter;
            //  yPosition = under;
            //}
            //if(rectangle.getWidth() == 0) xPosition = verticalCenter;
            //if(rectangle.getHeight() == 0) yPosition = horizontalCenter;
            label.translateXProperty().bind(Bindings.createDoubleBinding(xPosition, highligther.translateXProperty(), rectangle.boundsInParentProperty()));
            label.translateYProperty().bind(Bindings.createDoubleBinding(yPosition, label.heightProperty(), gotItButton.heightProperty()));

            //label.setTranslateY(nodeBounds.getMinY());
            gotItButton.translateXProperty().bind(label.translateXProperty());

            gotItButton.setPrefWidth(hintWidth);

            gotItButton.getStyleClass().add(IjfxCss.WARNING);

            gotItButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.CHECK));

            // when clicking on the anywhere on the screen
            highligther.setOnMouseClicked(event -> {

                // creating a transition
                Transition transition = Animations.DISAPPEARS_RIGHT.configure(highligther, ImageJFX.getAnimationDurationAsDouble());

                // and when the transition if finished
                transition
                        .setOnFinished(event2
                                -> {
                            //deleting the elements fromthe main panel
                            mainAnchorPane.getChildren().removeAll(highligther, label, gotItButton);
                        });

                // set that the hint is not displayed again
                isHintDisplaying = false;
                hintQueue.poll();
                // displaying the next hint
                nextHint();
                transition.play();

            });
            mainAnchorPane.getChildren().addAll(label, gotItButton);

            gotItButton.translateYProperty().bind(
                    Bindings.createDoubleBinding(
                            () -> {
                                return label.translateYProperty().getValue() + label.heightProperty().getValue() + hintMargin / 2;
                            }, label.translateYProperty(), label.heightProperty()));

            gotItButton.setOnAction(event -> {
                highligther.getOnMouseClicked().handle(null);
                hint.setRead();
            });

            //if(isHintDisplaying) {
            Animations.APPEARS_LEFT.configure(highligther, ImageJFX.getAnimationDurationAsDouble()).play();
            //}

            // gotItButton.setTranslateY(label.getTranslateY()+label.getBoundsInLocal().getHeight()+hintWidth/2);
            System.out.println(highligther.getLayoutBounds().getHeight());
        } catch (Exception e) {

            isHintDisplaying = false;
            nextHint();
            logger.log(Level.SEVERE, "Error when showing hint.", e);
        }

    }

    TransitionBinding<Number> sideMenuWidthBinding = new TransitionBinding<Number>(10d, 250d);

    /**
     *
     * Side Menu Actions
     *
     */
    public void initMenuAction() {

        sideMenuWidthBinding.bind(menuActivated, sideMenu.prefWidthProperty());

        sideMenu.setOnMouseEntered(event -> menuActivated.setValue(true));
        sideMenu.setOnMouseExited(event -> menuActivated.setValue(false));

        addSideMenuButton("Explore", FontAwesomeIcon.COMPASS, ExplorerActivity.class);

        addSideMenuButton("Visualize", FontAwesomeIcon.PICTURE_ALT, ImageJContainer.class, UiContexts.VISUALIZE);
        addSideMenuButton("Segment", FontAwesomeIcon.EYE, ImageJContainer.class, UiContexts.SEGMENT);

        addSideMenuButton("Batch process", FontAwesomeIcon.LIST, ExplorerActivity.class, UiContexts.BATCH);
        addSideMenuButton("Correction", FontAwesomeIcon.COFFEE, FolderSelection.class);

        new TransitionBinding<Number>(0d, 1d)
                .bind(sideMenuWidthBinding.stateProperty(), memoryLabel.opacityProperty())
                .setDuration(Duration.millis(150));

        new TransitionBinding<Number>(0d, 1d)
                .bind(sideMenuWidthBinding.stateProperty(), memoryProgressBar.opacityProperty())
                .setDuration(Duration.millis(150));

        menuActivated.setValue(false);
    }

    private static final UsageLocation SIDE_MENU_BAR = UsageLocation.get("Side menu bar");
    
    private SideMenuButton addSideMenuButton(String title, FontAwesomeIcon icon, Class<? extends Activity> actClass) {

        SideMenuButton sideMenuButton = new SideMenuButton(title, actClass).setIcon(icon);

        // adding listening of the menu bar events
        sideMenuButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event->{
             Usage
                    .factory()
                    .createUsageLog(UsageType.CLICK, title, SIDE_MENU_BAR)
                    .send();
        
        });
        
        sideMenuTopVBox.getChildren().add(sideMenuButton);

        return sideMenuButton;
    }

    private void addSideMenuButton(String title, FontAwesomeIcon icon, Class<? extends Activity> actClass, String context) {

        SideMenuButton button = new SideMenuButton(title);
        button.setIcon(icon);

        button.setOnMouseClicked(click -> {

            uiContextService.enter(context);
            if (activityService.getCurrentActivityAsClass() != actClass) {
                activityService.openByType(actClass);
            }
            uiContextService.update();
            menuActivated.setValue(false);

        });

        sideMenuTopVBox.getChildren().add(button);

    }

    public void removeBlacklisted() {

        try {
            String blacklistRaw = TextFileUtils.readFileFromJar("/blacklist.txt");
            Stream
                    .of(blacklistRaw.split("\n"))
                    .filter(s -> s.startsWith("#") == false)
                    .map(pluginSrv::getPlugin)
                    //.map(info->(PluginInfo<?>)info.get)
                    .forEach(pluginSrv::removePlugin);

        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error when blacklisting plugings", ex);

        }
    }

    private class SideMenuButton extends HBox {

        String appToOpen;
        Class<? extends Activity> activityClass;

        Label label = new Label();
        FontAwesomeIconView iconNode;
        String initialText;

        public SideMenuButton(String name) {

            super();
            label.setText(name);
            initialText = name;

            setOnMouseClicked(this::onAction);
            getStyleClass().add("side-menu-button");
            setMaxWidth(Double.MAX_VALUE);
            iconNode = new FontAwesomeIconView(FontAwesomeIcon.QUESTION);
            iconNode.getStyleClass().add("side-menu-icon");

            getChildren().addAll(iconNode, label);

            new TransitionBinding<Number>(0d, 1d)
                    .bind(sideMenuWidthBinding.stateProperty(), label.opacityProperty())
                    .setDuration(Duration.millis(150));

            label.textProperty().bind(Bindings.createStringBinding(this::getText, menuActivated));

        }

        public String getText() {
            if (menuActivated.getValue()) {
                return initialText;
            } else {
                return "";
            }
        }

        public SideMenuButton(String name, String appToOpen) {
            this(name);

            this.appToOpen = appToOpen;

        }

        public SideMenuButton(String name, Class<? extends Activity> activityClass) {
            this(name);
            this.activityClass = activityClass;
        }

        public SideMenuButton setIcon(FontAwesomeIcon icon) {
            //GlyphsDude.createIcon(icon);

            iconNode.setIcon(icon);
            return this;
        }

        public void onAction(Event event) {

            if (appToOpen != null) {

                appService.showApp(appToOpen);

            } else {
                activityService.openByType(this.activityClass);
            }
        }
    }  
  }
