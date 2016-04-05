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
package ijfx.examples.context;

import ijfx.service.uicontext.UiContextService;
import ijfx.ui.context.PaneContextualView;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.plugin.DebugButton;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import mongis.utils.panecell.PaneIconCell;
import net.imagej.ImageJ;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import static javafx.application.Application.launch;

/**
 *
 * @author Tuan anh TRINH
 */
public class ToolBarBuilder extends Application {

    public static ImageJ imageJ = new ImageJ();

    @Parameter
    UiContextService contextService;
    private FlowPane flowPane;
    private HBox fakeToolBar;
    private BorderPane borderPane;
    private PopOver popOver;
    private HBox toolbarTest;
    private JsonReader jsonReader;
    private PaneContextualView contextualView;
    private FlowPane f;

    public ToolBarBuilder() {

    }

    public void init(Context context) {
        f = new FlowPane();
        context.inject(this);
        jsonReader = new JsonReader();
        jsonReader.read("./src/main/resources/ijfx/ui/menutoolbar/myJson.json");
        jsonReader.separate();
        borderPane = new BorderPane();
        flowPane = new FlowPane();
        fakeToolBar = new HBox();
        toolbarTest = new HBox();
        popOver = new PopOver();
        flowPane.setPadding(new Insets(10,10,10,10));
        contextualView = new PaneContextualView(contextService, flowPane, "flowPane");
        generateItems(jsonReader, fakeToolBar, contextualView);

        //Just for test, has to be removed after
        TextField textField = new TextField();
        textField.promptTextProperty();
        textField.setPromptText("Enter Context");
        Button validateContext = new Button("Validate context");
        validateContext.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, (e) -> {
            contextService.enter(textField.getText());

        });
        MenuButton debugButton = new DebugButton();
        Button removeContext = new Button("Remove All context");
        removeContext.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            String[] toDelete = contextService.getActualContextListAsString().split(" ");
            for (String toDelete1 : toDelete) {
                contextService.leave(toDelete1);
            }
        });
        
        toolbarTest.getChildren().addAll(textField, validateContext, removeContext, debugButton);
        borderPane.setTop(fakeToolBar);
        Rectangle c = new Rectangle();
        c.widthProperty().bind(borderPane.widthProperty());
        c.setHeight(100);
        c.setFill(Color.RED);
        borderPane.setCenter(c);
        borderPane.setBottom(toolbarTest);
        contextService.enter("always image-open multi-z");
        contextService.update();

    }

    /**
     *
     * @param pane
     * @param owner
     */
    private void createPopOver(Pane pane, Node owner) {

        if (popOver == null) {
            popOver = new PopOver(pane);
            setPopOver(pane, owner);

        } else if (!popOver.isShowing() || popOver.getOpacity() == 0.0) {
            popOver.setContentNode(pane);
            setPopOver(pane, owner);
        }

    }

    /**
     * Set the PopOver properties
     *
     * @param pane
     * @param owner
     */
    private void setPopOver(Pane pane, Node owner) {
        popOver.setCornerRadius(0);       
        popOver.minWidthProperty().bind(borderPane.getScene().widthProperty());
        popOver.setWidth(borderPane.getScene().widthProperty().getValue());
        popOver.maxWidthProperty().bind(borderPane.getScene().widthProperty());
        pane.setMinWidth(popOver.minWidthProperty().getValue());
        pane.setMaxWidth(popOver.minWidthProperty().getValue());
        popOver.setDetached(false);
        popOver.setDetachable(false);
        popOver.setHideOnEscape(true);
        popOver.setAutoFix(true);
        popOver.setAutoHide(true);
        popOver.setOpacity(1.0);
        popOver.setArrowSize(0);
        popOver.show(owner);
        popOver.setAnchorX(borderPane.localToScreen(borderPane.getBoundsInParent()).getMinX()+1);
        popOver.setAnchorY(borderPane.localToScreen(borderPane.getTop().getBoundsInParent()).getMaxY()-1);
        popOver.getStyleClass().clear();
        popOver.getStyleClass().add("popoverToolBar");

    }

    /**
     * Set action when user hoovers PaneIconCell
     *
     * @param paneIconCell
     */
    public void setMouseAction(PaneIconCell<ItemCategory> paneIconCell) {
        paneIconCell.addEventFilter(MouseEvent.MOUSE_RELEASED, (ee)
                -> {
            System.out.println(popOver.getY());
        });
        paneIconCell.addEventFilter(MouseEvent.MOUSE_EXITED, (ee) -> {
            contextService.leave(paneIconCell.getItem().getName());
        });
        paneIconCell.addEventFilter(MouseEvent.MOUSE_ENTERED, (ee) -> {
            contextService.enter(paneIconCell.getItem().getName());
            contextService.update();

            if (!flowPane.getChildren().isEmpty()) {

                popOver.setOpacity(0);
                createPopOver(flowPane, paneIconCell);
            } 
            else if (flowPane.getChildren().isEmpty()) {
                popOver.setOpacity(0);
            }

        });
    }

    /**
     * Generate ItemCategory and ItemWidget with FactoryPaneCell
     *
     * @see ijfx.examples.context.FactoryPaneCell
     * @param jsonReader
     * @param fakeToolBar
     * @param contextualView
     */
    public void generateItems(JsonReader jsonReader, HBox fakeToolBar, PaneContextualView contextualView) {
        jsonReader.getCategoryList().stream().forEach((e) -> {
            PaneIconCell<ItemCategory> paneIconCell = FactoryPaneCell.generate(e);
            paneIconCell.setId(((ItemCategory) paneIconCell.getItem()).getName());
            fakeToolBar.getChildren().add(paneIconCell);
            setMouseAction(paneIconCell);
        });

        jsonReader.getWidgetList().stream().forEach((e) -> {
            PaneIconCell<ItemWidget> paneIconCell = FactoryPaneCell.generate(e);
            String itemContext = ((ItemWidget) paneIconCell.getItem()).getContext();
            paneIconCell.setId(((ItemWidget) paneIconCell.getItem()).getLabel());
            contextualView.registerNode(paneIconCell, itemContext);
            paneIconCell.setOnMouseClicked(event -> {
                System.out.println("Click Action " + paneIconCell.getItem().getLabel() + paneIconCell.getItem().getContext());

            });

        });
    }

    public BorderPane getLayout() {
        return borderPane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //init(imageJ.getContext());
        Scene scene = new Scene(getLayout());
        scene.getStylesheets().add(ImageJFX.class.getResource(("flatterfx.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String... args) {
        launch(args);
    }
}
