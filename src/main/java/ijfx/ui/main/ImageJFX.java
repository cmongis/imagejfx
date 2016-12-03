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
package ijfx.ui.main;

import java.awt.SplashScreen;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.DefaultLinearAxis;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class ImageJFX extends Application {

    public static final double MARGIN = 10;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private static Logger logger;

    public static boolean formatted = false;

    public static Stage PRIMARY_STAGE;

    public static final AxisType SERIES = Axes.get("Series");

    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("ImageJFX");

        }

        return logger;
    }

    FXMLLoader loader;
    MainWindowController controller;

    protected static String STYLESHEET_NAME = "flatterfx.css";

    public static final String CSS_DARK_FILL = "dark-fill";
    public static final String IMAGEJFX_PREF_NODE = "/imagejfx/";
    public static final String STYLESHEET_ADDR = ImageJFX.class.getResource(STYLESHEET_NAME).toExternalForm();

    public static final String VBOX_CLASS = "vbox";

    public static final String IJFX_FOLDER_NAME = ".imagejfx";
    public static final String FILE_FAVORITES = "favorites.json";

    public static final String CSS_SMALL_BUTTON = "small";
    public static final String BUTTON_DANGER_CLASS = "danger";

    public static final ScheduledExecutorService scheduleThreadPool = Executors.newScheduledThreadPool(2);

    public static final int CORE_NUMBER = getCoreNumber() > 1 ? getCoreNumber() - 1 : getCoreNumber();
    
    private static final ExecutorService service = Executors.newFixedThreadPool(CORE_NUMBER);

    public static double getAnimationDurationAsDouble() {
        return ANIMATION_DURATION.toMillis();
    }

    public static Duration getAnimationDuration() {
        return ANIMATION_DURATION;
    }

    private static ResourceBundle resourceBundle;

    public static final String RESSOURCE_BUNDLE_ADDR = "ijfx/ui/res/MenuBundle";

    @Override
    public void start(Stage primaryStage) {

        Parent root;
        try {

            PRIMARY_STAGE = primaryStage;

            LogRecorderService.getInstance();

            getLogger().info("You running Java " + System.getProperty("java.version"));
            File pluginDir = new File("./", "plugins/");
            if (pluginDir.exists() == false) {
                pluginDir.mkdir();
            }
            getLogger().info(pluginDir.getAbsolutePath());
            System.setProperty("imagej.dir", new File(".").getAbsolutePath());
            System.setProperty("plugins.dir", pluginDir.getAbsolutePath());

          
            controller = new MainWindowController();//loader.<MainWindowController>getController();

            Scene scene = new Scene(controller);

            scene.getStylesheets().add(getStylesheet());

            // scene.getStylesheets().add("http://fonts.googleapis.com/css?family=Open+Sans");
            //scene.getStylesheets().add("http://maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css");
            //Font.loadFont(FontAwesomeIconView.class.getResource("fontawesome-webfont.ttf").toExternalForm().toString(), 0);
            primaryStage.setTitle("ImageJ FX");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    Platform.exit();
                    System.exit(0);
                }
            });
            primaryStage.show();
            controller.setScene(scene);
            Platform.runLater(controller::init);

            SplashScreen splashScreen = SplashScreen.getSplashScreen();
            if (splashScreen != null) {
                splashScreen.close();
            }

        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);;
        }

    }

    public static String getStylesheet() {
        return STYLESHEET_ADDR;
    }

    public static final Duration ANIMATION_DURATION = Duration.millis(300);

    public static File getConfigDirectory() {
        File configDirectory = new File(System.getProperty("user.home") + File.separator + IJFX_FOLDER_NAME);
        if (configDirectory.exists() == false) {
            configDirectory.mkdir();
        }

        return configDirectory;
    }

    public static String getConfigFile(String filename) {
        return new File(getConfigDirectory(), filename).getAbsolutePath();
    }

    public static ExecutorService getThreadPool() {
        return service;
    }

    private static ExecutorService threadQueue = Executors.newSingleThreadExecutor();

    public static ExecutorService getThreadQueue() {
        return threadQueue;
    }

    public static ScheduledExecutorService getScheduledThreadPool() {
        return scheduleThreadPool;
    }

    public static ResourceBundle getResourceBundle() {
        if (resourceBundle == null) {
            resourceBundle = ResourceBundle.getBundle(RESSOURCE_BUNDLE_ADDR);
        }
        return resourceBundle;
    }

    public static int getCoreNumber() {
        return Runtime.getRuntime().availableProcessors();
    }

}
