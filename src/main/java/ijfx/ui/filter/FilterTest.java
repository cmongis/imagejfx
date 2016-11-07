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
package ijfx.ui.filter;

import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.math3.random.RandomDataGenerator;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class FilterTest extends Application {

    NumberFilter filter;
    
    BorderPane borderPane;
    
    @Override
    public void start(Stage primaryStage) throws Exception {

        borderPane = new BorderPane();
        
        borderPane.getStylesheets().add(ImageJFX.getStylesheet());
        borderPane.getStyleClass().add("explorer-filter");
        Button updateButton = new Button("Update values");
        
        
        filter = new DefaultNumberFilter();
        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);

       borderPane.setCenter(filter.getContent());
       borderPane.setBottom(updateButton);
        
        updateButton.setOnAction(this::update);
        
        primaryStage.show();
        
    }
    
    public void update(ActionEvent event) {
         ArrayList<Double> numbers = new ArrayList<>();
        RandomDataGenerator generator = new RandomDataGenerator();

        for (int i = 0; i != 300; i++) {
            numbers.add(new Double(generator.nextUniform(0, 0.5)));
        }

        filter.setAllPossibleValue(numbers);
        
        borderPane.getStylesheets().remove(ImageJFX.getStylesheet());
        borderPane.getStylesheets().add(ImageJFX.getStylesheet());
        
    }
    
    public static void main(String... args) {
        launch(args);
    }
}
