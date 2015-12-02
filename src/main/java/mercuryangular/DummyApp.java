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
package mercuryangular;

import java.io.File;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import mercury.core.AngularBinder;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class DummyApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
       
        // creating a webview for our web app
        WebView webview = new WebView();
        
        // creating a binder
        AngularBinder binder = new AngularBinder();
        
        // binding the webview
        binder.bindWebView(webview);
        
        // registering the service so the bind can bind it each time the 
        // page is reloaded
        binder.registerService("DummyService", new DummyService());
       
        
        // simple root
        StackPane root = new StackPane();
        root.getChildren().add(webview);
        
        Scene scene = new Scene(root, 300, 250);
        
       // primaryStage.titleProperty().bind(webview.getEngine().titleProperty());
        primaryStage.setScene(scene);
        primaryStage.show();

        webview.getEngine().load(new File("./src/mercury/test/htmlapp/index.html").toURI().toString());
        
        // we make sure we can reload the view by pressing F5
        scene.setOnKeyTyped(keyEvent->{
            if(keyEvent.getCode() == KeyCode.F5) webview.getEngine().reload();
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
