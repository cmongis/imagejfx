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
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    Pane flowPane;
    HBox fakeToolBar;
    BorderPane borderPane;
    private PopOver popOver;
HBox toolbarTest ;
    public ContextExample() {

    }

    public void init(Context context) {
        JsonReader jsonReader = new JsonReader();
        jsonReader.read();
        jsonReader.separate();
        borderPane = new BorderPane();
        context.inject(this);
        flowPane = new FlowPane();
        fakeToolBar = new HBox();
        flowPane.setPrefSize(30, 30);
        toolbarTest = new HBox();

        Button fruitButton = new Button("fruit");
        fruitButton.setId("fruit");

        Button vegetableButton = new Button("vegetable");
        vegetableButton.setId("vegetable");

        Button bananaButton = new Button("Banana");
        bananaButton.setId("banana");

        Button aubergineButton = new Button("Aubergine");
        aubergineButton.setId("aubergine");

        PaneContextualView contextualView = new PaneContextualView(contextService, flowPane, "flowPane");
 jsonReader.getCategoryList().stream().forEach( (e) ->{
            PaneIconCell <ItemCategory> paneIconCell = FactoryPaneIconCell.generate(e);
            paneIconCell.setTitleFactory(f -> f.getName());
            
            ItemCategory itemCategory= (ItemCategory)paneIconCell.getItem();
            String itemContext = ((ItemCategory) paneIconCell.getItem()).getContext();
            paneIconCell.setId(((ItemCategory) paneIconCell.getItem()).getName());
            //paneIconCell.setTitle("e");
            toolbarTest.getChildren().add(paneIconCell);
                    paneIconCell.setOnMouseClicked(event -> {
            //contextService.leave("vegetable");
            contextService.enter(paneIconCell.getItem().getContext());
            contextService.update();
        });
                    
                            paneIconCell.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, (ee) -> {
            createPopOver(paneIconCell);
                        contextService.enter(paneIconCell.getItem().getContext());
            contextService.update();

        });
            //contextualView.registerNode(paneIconCell, itemContext);
            
        });
 
 
 
 jsonReader.getWidgetList().stream().forEach( (e) ->{
            PaneIconCell <ItemWidget> paneIconCell = FactoryPaneIconCell.generate(e);
            paneIconCell.setTitleFactory(f -> f.getLabel());
            
            ItemWidget itemWidget= (ItemWidget)paneIconCell.getItem();
            String itemContext = ((ItemWidget) paneIconCell.getItem()).getContext();
            paneIconCell.setId(((ItemWidget) paneIconCell.getItem()).getLabel());
            //paneIconCell.setTitle("e");
            //toolbarTest.getChildren().add(paneIconCell);
                    paneIconCell.setOnMouseClicked(event -> {
                        System.out.println(paneIconCell.getItem().getLabel());
            //contextService.leave("vegetable");
            contextService.enter(paneIconCell.getItem().getContext());
            contextService.update();
        });
            contextualView.registerNode(paneIconCell, itemContext);
            
        });
 
 
 
 
        //contextualView.registerNode(fruitButton, "always");
        //contextualView.registerNode(vegetableButton, "always");
        contextualView.registerNode(bananaButton, "fruit");
        contextualView.registerNode(aubergineButton, "vegetable");

        Button show = new Button("Show");

        show.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, (e) -> {
            createPopOver(show);

        });

        fakeToolBar.getChildren().add(show);
        //toolbarTest.getChildren().addAll(fruitButton, vegetableButton);
        borderPane.setTop(fakeToolBar);
        /*Rectangle c = new Rectangle();
        c.widthProperty().bind(borderPane.widthProperty());
        c.setHeight(600);
        c.setFill(Color.RED);
        borderPane.setCenter(c);*/
        borderPane.setBottom(toolbarTest);
        fruitButton.setOnAction(event -> {
            contextService.leave("vegetable");
            contextService.enter("fruit");
            contextService.update();
        });

        vegetableButton.setOnAction(event -> {
            contextService.leave("fruit");
            contextService.enter("vegetable");
            contextService.update();

        });

        contextService.enter("always");
        contextService.update();

    }

    private void createPopOver(Node node) {
        if (popOver == null) {
            popOver = new PopOver(flowPane);
            setPopOver(node);

        } else if (!popOver.isShowing()) {
            setPopOver(node);
        }

    }

    private void setPopOver(Node node) {
        popOver.setDetached(false);
        popOver.setDetachable(false);
        popOver.setHideOnEscape(false);
        popOver.setAutoFix(true);
        popOver.setAutoHide(true);
        popOver.setOpacity(1.0);
        popOver.setHideOnEscape(false);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.show(node, node.localToScreen(10, 10).getX(), node.localToScreen(10, 10).getY());

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
