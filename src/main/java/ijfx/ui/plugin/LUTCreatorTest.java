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
package ijfx.ui.plugin;

import com.sun.javafx.scene.control.skin.ColorPickerSkin;
import com.sun.javafx.scene.control.skin.CustomColorDialog;
import ijfx.ui.module.ModuleConfigPane;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
//import javafx.scene.control.ColorPicker;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 *
 * @author Tuan anh TRINH
 */
public class LUTCreatorTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
//        ColorPicker colorPicker = new ColorPicker();
//        ColorPickerSkin colorPickerSkin =(ColorPickerSkin) colorPicker.getSkin();
//        colorPicker.getStyleClass().add("split-button");
//        root.getChildren().add(colorPicker);
//            new ColorPickerSkin(colorPicker).show();
        btn.setOnAction(event -> onAction(event));
    }
public void onAction(ActionEvent event) {
        ModuleConfigPane fxFormDialog = new ModuleConfigPane();
        /*
        fxFormDialog.addField("Double", "Double ", Double.class);
        fxFormDialog.addField("double", "double ", double.class);
        fxFormDialog.addField("String", "String", String.class);
        // fxFormDialog.addField("String[]", "String[]", String.class);
        fxFormDialog.addField("Integer", "Integer", Integer.class);
        fxFormDialog.addField("integer", "integer", int.class);
        fxFormDialog.addField("Boolean", "Boolean", Boolean.class);
        fxFormDialog.addField("boolean", "boolean", boolean.class);*/


            //fxFormDialog.getField("blue").setDefaultValue(4.0);
        //fxFormDialog.getField("bla").setDefaultValue("go");
//        LUTCreatorDialog lUTCreatorDialog = new LUTCreatorDialog();
//        lUTCreatorDialog.showAndWait();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
