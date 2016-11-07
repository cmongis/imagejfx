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
package ijfx.ui.utils;

import ijfx.ui.main.ImageJFX;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public abstract class BaseTester extends Application {

    BorderPane borderPane = new BorderPane();

    ToolBar toolbar = new ToolBar();

    Stage primaryStage;
    
    public BaseTester() {

       
       
        
        addAction("Refresh css",this::refreshCss);
      
        borderPane.setBottom(toolbar);
    }

    protected void setContent(Node node) {
        borderPane.setCenter(node);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.primaryStage = primaryStage;
        
        borderPane.getStylesheets().add(ImageJFX.getStylesheet());
        borderPane.getStyleClass().add("explorer-filter");

        Scene scene = new Scene(borderPane);
        primaryStage.setWidth(600);
        primaryStage.setHeight(400);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        initApp();

        addAction("Reset",this::initApp);
        
    }

    public void addAction(String label, Runnable action) {
        Button button = new Button(label);
        button.setOnAction(event -> action.run());
        toolbar.getItems().add(button);
    }

    private void refreshCss() {
        borderPane.getStylesheets().remove(ImageJFX.getStylesheet());
        borderPane.getStylesheets().add(ImageJFX.getStylesheet());
    }
    
    abstract public void initApp();

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    
    
}
