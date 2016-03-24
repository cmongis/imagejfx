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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.imagej.ImageJ;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class ContextExample extends Application {

    public static ImageJ imageJ = new ImageJ();

    @Parameter
    UiContextService contextService;

    Pane flowPane;

    public ContextExample() {
        
        
    }
    public void init(Context context) {

        context.inject(this);

        flowPane = new HBox();
        flowPane.setPrefSize(300, 300);
        
        
        Button fruitButton = new Button("fruit");
        fruitButton.setId("fruit");

        Button vegetableButton = new Button("vegetable");
        vegetableButton.setId("vegetable");

        Button bananaButton = new Button("Banana");
        bananaButton.setId("banana");

        Button aubergineButton = new Button("Aubergine");
        aubergineButton.setId("aubergine");

        PaneContextualView contextualView = new PaneContextualView(contextService, flowPane, "flowPane");

        contextualView.registerNode(fruitButton, "always");
        contextualView.registerNode(vegetableButton, "always");
        contextualView.registerNode(bananaButton, "fruit");
        contextualView.registerNode(aubergineButton, "vegetable");

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

    public Pane flowPane() {
        return flowPane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        
        init(imageJ.getContext());
        
        Scene scene = new Scene(flowPane());
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }
    
    public static void main(String... args) {
        launch(args);
    }
}