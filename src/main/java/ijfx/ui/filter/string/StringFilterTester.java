/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.ui.filter.string;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author tuananh
 */
public class StringFilterTester extends Application
        
{
    public static void main(String[] args) {
        launch(args);
      
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        DefaultStringFilter ui = new DefaultStringFilter();
        Scene scene = new Scene(ui);
        scene.getStylesheets().add("/ijfx/ui/main/flatterfx.css");
        
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
      
    }
    
}
