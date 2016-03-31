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
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import mongis.utils.panecell.PaneIconCell;
import net.imagej.ImageJ;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author tuananh
 */
public class ContextExample extends Application {

    public static ImageJ imageJ = new ImageJ();

    @Parameter
    UiContextService contextService;
    BooleanProperty displayPopOver;
    Button removeContext;
    FlowPane flowPane;
    HBox fakeToolBar;
    BorderPane borderPane;
    private PopOver popOver;
    HBox toolbarTest;

    public ContextExample() {

    }

    public void init(Context context) {
        JsonReader jsonReader = new JsonReader();
        jsonReader.read();
        jsonReader.separate();
        borderPane = new BorderPane();
        context.inject(this);
        flowPane = new FlowPane();
        flowPane.setColumnHalignment(HPos.CENTER);
        flowPane.setRowValignment(VPos.CENTER);
        fakeToolBar = new HBox();
        toolbarTest = new HBox();
        popOver = new PopOver();
        // displayPopOver = popOver.getDisplayableProperty();

        PaneContextualView contextualView = new PaneContextualView(contextService, flowPane, "flowPane");
        jsonReader.getCategoryList().stream().forEach((e) -> {
            PaneIconCell<ItemCategory> paneIconCell = FactoryPaneIconCell.generate(e);

            paneIconCell.setId(((ItemCategory) paneIconCell.getItem()).getName());
            //paneIconCell.setTitle("e");
            toolbarTest.getChildren().add(paneIconCell);
            paneIconCell.addEventFilter(MouseEvent.MOUSE_CLICKED, (ee) -> {
                if (popOver != null) {
                    if (flowPane.getChildren().isEmpty())
                    {
                        
                    }
                    else if (!popOver.isShowing()) {
                        contextService.enter(paneIconCell.getItem().getName());
                        contextService.update();
                        //displayPopOver.set(true);
                        createPopOver(flowPane, paneIconCell);
                    }

                } else {
                    System.out.println("ijfx.examples.context.ContextExample.init()");
                    createPopOver(flowPane, paneIconCell);

                }
                // System.out.println(flowPane.getChildren().isEmpty());
                /*try {
                            popOver.setOnHiding((eee) -> contextService.leave(paneIconCell.getItem().getName()));
                            popOver.setOnAutoHide((eeee)-> contextService.leave(paneIconCell.getItem().getName()));
                            
                        } catch (Exception exception) {
                        }*/
            });
            paneIconCell.addEventFilter(MouseEvent.MOUSE_EXITED, (ee) -> {
                contextService.leave(paneIconCell.getItem().getName());
                //contextService.update();
            });
            paneIconCell.addEventFilter(MouseEvent.MOUSE_ENTERED, (ee) -> {

                //contextService.leave(paneIconCell.getItem().getName());
                //contextService.update();
//                        displayPopOver.set(false);
                if (popOver.isShowing() && popOver.getOpacity() > 0) {

                    //popOver = null;  
                    popOver.setOpacity(0);
                    //popOver.
                    System.out.println("opacity " + popOver.getOpacity());
             
                        
                    createPopOver(flowPane, paneIconCell);
                    

                }

                contextService.enter(paneIconCell.getItem().getName());
                contextService.update();

            });
            //contextualView.registerNode(paneIconCell, itemContext);

        });

        jsonReader.getWidgetList().stream().forEach((e) -> {
            PaneIconCell<ItemWidget> paneIconCell = FactoryPaneIconCell.generate(e);
            paneIconCell.setLoadImageOnlyWhenVisible(false);
            ItemWidget itemWidget = (ItemWidget) paneIconCell.getItem();
            String itemContext = ((ItemWidget) paneIconCell.getItem()).getContext();
            paneIconCell.setId(((ItemWidget) paneIconCell.getItem()).getLabel());
            contextualView.registerNode(paneIconCell, itemContext);
            System.out.println(itemContext);
            //paneIconCell.setTitle("e");
            //toolbarTest.getChildren().add(paneIconCell);
            paneIconCell.setOnMouseClicked(event -> {
                System.out.println("Click" + paneIconCell.getItem().getLabel() + paneIconCell.getItem().getContext());

            });

        });

        TextField textField = new TextField();
        textField.promptTextProperty();
        textField.setPromptText("Enter Context");
        Button validateContext = new Button("Validate context");
        validateContext.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, (e) -> {
            contextService.enter(textField.getText());

        });
        removeContext = new Button("Remove context");
        removeContext.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, (e) -> contextService.leave(contextService.getActualContextListAsString()));

        fakeToolBar.getChildren().addAll(textField, validateContext, removeContext);
        //toolbarTest.getChildren().addAll(fruitButton, vegetableButton);
        borderPane.setBottom(fakeToolBar);
        Rectangle c = new Rectangle();
        c.widthProperty().bind(borderPane.widthProperty());
        c.setHeight(100);
        c.setFill(Color.RED);
        borderPane.setCenter(c);
        borderPane.setTop(toolbarTest);

        contextService.enter("always image-open multi-z");
        contextService.update();

    }

    private void createPopOver(FlowPane pane, Node owner) {
        /*Stage stage = new Stage();
        FlowPane flowPane2 = new FlowPane(pane);
        Scene scene = new Scene(flowPane2);
        stage.setScene(scene);
        stage.setHeight(flowPane2.getHeight());
        stage.show();*/
        if (popOver == null /*&& !pane.getChildren().isEmpty()*/) {
            popOver = new PopOver(pane);
            setPopOver(pane, owner);

        } else if (!popOver.isShowing() || popOver.getOpacity() == 0.0) {
            System.out.println(popOver.getOpacity());
            popOver.setContentNode(pane);
            setPopOver(pane, owner);
        }

    }

    private void setPopOver(Pane pane, Node owner) {

        //popOver.setConsumeAutoHidingEvents(true);
//        displayPopOver.bind(popOver.showingProperty());
        popOver.setWidth(pane.getWidth());
        popOver.setDetached(false);
        popOver.setDetachable(false);
        popOver.setHideOnEscape(false);
        popOver.setAutoFix(true);
        popOver.setAutoHide(true);
        popOver.setOpacity(1.0);
        popOver.setArrowLocation(PopOverFlowPane.ArrowLocation.TOP_CENTER);
        popOver.show(owner);//, owner.localToScreen(0,0).getX(), owner.localToScreen(0,0).getY()+owner.getBoundsInLocal().getHeight(),new Duration(10000000.0));
        System.out.println("ijfx.examples.context.ContextExample.setPopOver()");

    }

    public Pane flowPane() {
        return flowPane;
    }

    public BorderPane getLayout() {
        return borderPane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        init(imageJ.getContext());

        Scene scene = new Scene(getLayout());
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String... args) {
        launch(args);
    }
}
