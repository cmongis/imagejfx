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
package ijfx.ui.canvas;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ij.ImagePlus;

import ijfx.bridge.FxMenuCreator;
import ijfx.ui.module.json.ModuleItemSerializer;
import ijfx.ui.module.json.ModuleSerializer;
import ijfx.ui.arcmenu.ArcMenu;
import ijfx.ui.datadisplay.image.ImageWindowContainer;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.module.input.InputControl;
import io.scif.SCIFIO;
import io.scif.filters.ReaderFilter;
import java.io.File;
import java.net.URL;

import java.util.HashMap;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.imagej.ImageJ;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplayService;
import net.imagej.plugins.commands.imglib.GaussianBlur;
import net.imagej.plugins.commands.misc.ApplyLookupTable;
import org.scijava.Context;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandModuleItem;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.io.IOService;
import org.scijava.menu.MenuService;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugins.commands.io.OpenFile;
import static javafx.application.Application.launch;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class FxCanvasTester extends Application {

    ImagePlus imp;

    int currentPlane = 0;
    int maxPlane = 20;
    ReaderFilter reader;

    Logger logger = ImageJFX.getLogger();

    Context context;

    @Parameter
    IOService ioservice;

    @Parameter
    CommandService commandService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    ImageJService imageJService;

    @Parameter
    DisplayService displayService;

    @Parameter
    ModuleService moduleService;

    @Parameter
    MenuService menuService;

    @Override
    public void start(Stage primaryStage) {

        final SCIFIO scifio = new SCIFIO();
        MenuBar menuBar = new MenuBar();
        InputControl parameterInput = null;
        try {
            System.setProperty("imagej.legacy.sync", "true");
            //reader.getContext().inject(this);
            ImageJ imagej = new ImageJ();
            context = imagej.getContext();
            CommandInfo command = imagej.command().getCommand(GaussianBlur.class);

            CommandModuleItem input = command.getInput("sigma");
            Class<?> type = input.getType();
            if (type == double.class) {
                type = Double.class;
            }
           
            context.inject(this);

            GaussianBlur module = new GaussianBlur();

            imagej.ui().showUI();

            //reader = scifio.initializer().initializeReader("./stack.tif");
            commandService.run(OpenFile.class, true, new HashMap<String, Object>());

            menuBar = new MenuBar();
            menuService.createMenus(new FxMenuCreator(), menuBar);
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule simpleModule = new SimpleModule("ModuleSerializer");
            // simpleModule.addSerializer(ModuleItem<?>.class,new ModuleItemSerializer());
            simpleModule.addSerializer(ModuleInfo.class, new ModuleSerializer());
            simpleModule.addSerializer(ModuleItem.class, new ModuleItemSerializer());
            mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.registerModule(simpleModule);

            mapper.writeValue(new File("modules.json"), moduleService.getModules());


        } catch (Exception ex) {
            ImageJFX.getLogger();
        }

        //imageView.fitImageToScreen();
        Button reset = new Button("Reset");

        reset.setOnAction(event -> update());

        BorderPane pane = new BorderPane();

        Button test = new Button("Test");

        AnchorPane root = new AnchorPane();
        root.getChildren().add(pane);
        root.getStylesheets().add(ArcMenu.class.getResource("arc-default.css").toExternalForm());
        root.getStylesheets().add(ImageJFX.class.getResource(("flatterfx.css")).toExternalForm());
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
        pane.setTop(menuBar);

        HBox vbox = new HBox();
        vbox.getChildren().addAll(reset, test, parameterInput);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        // update();
        pane.setCenter(ImageWindowContainer.getInstance());
        // pane.setPrefSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        pane.setBottom(vbox);

        Scene scene = new Scene(root, 600, 600);

        test.setOnAction(event -> {

            test();
        });

        primaryStage.setTitle("ImageCanvasTest");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void update() {

        commandService.run(OpenFile.class, true, new HashMap<String, Object>());
    }

    public void test() {

        URL url;
        try {
            url = new File("/home/cyril/gyr.lut").toURL();

            commandService.run(ApplyLookupTable.class, true, "tableURL", url);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
