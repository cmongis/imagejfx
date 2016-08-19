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
package mongis.utils;

import ijfx.core.listenableSystem.Listening;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Cyril MONGIS
 */
public class FXUtilities {

    public static Node loadView(URL url, Object controller, boolean setRoot) {
        FXMLLoader loader = setLoaderController(createLoader(), controller);
        setLoaderUrl(loader, url);
        if (setRoot) {
            loader.setRoot(controller);
        }
        return loadController(loader);
    }

    public static Pane loadView(URL url) {
        try {
            return setLoaderUrl(createLoader(), url).load();
        } catch (IOException ex) {
            ImageJFX.getLogger();
        }
        return null;
    }

    public static ResourceBundle getResourceBundle() {
        return ImageJFX.getResourceBundle();
    }

    private static FXMLLoader createLoader() {
        return new FXMLLoader();
    }

    private static FXMLLoader setLoaderController(FXMLLoader loader, Object controller) {
        loader.setController(controller);
        return loader;
    }

    private static FXMLLoader setLoaderUrl(FXMLLoader loader, URL url) {
        loader.setLocation(url);
        loader.setResources(getResourceBundle());
        return loader;
    }

    private static Node loadController(FXMLLoader loader) {
        try {
            loader.load();
            Node controller = (Node) loader.getController();
            return controller;
        } catch (IOException ex) {
            ImageJFX.getLogger();
            return null;
        }
    }

    public static void emptyPane(Pane pane) {

        if (pane == null) {
            return;
        }
        for (Node child : pane.getChildren()) {
            if (child instanceof Pane) {
                emptyPane((Pane) child);
            }

        }
        if (pane instanceof Listening) {
            Listening listening = (Listening) pane;
            listening.stopListening();
        }
        pane.getChildren().clear();

    }

    public static void modifyUiThreadSafe(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public static void close(Pane pane) {
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.close();
    }

    /**
     * Simple helper class.
     *
     * @author hendrikebbers
     *
     */
    public static final String BUTTON_PRIMARY_CLASS = "primary";
    public static final String BUTTON_SUCCESS_CLASS = "success";
    public static final String BUTTON_DANGER_CLASS = "danger";

    private static class ThrowableWrapper {

        Throwable t;
    }

    /**
     * Invokes a Runnable in JFX Thread and waits while it's finished. Like
     * SwingUtilities.invokeAndWait does for EDT.
     *
     * @param run The Runnable that has to be called on JFX thread.
     * @throws InterruptedException f the execution is interrupted.
     * @throws ExecutionException If a exception is occurred in the run method
     * of the Runnable
     */
    public static void runAndWait(final Runnable run)
            throws InterruptedException, ExecutionException {
        if (Platform.isFxApplicationThread()) {
            try {
                run.run();
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        } else {
            final Lock lock = new ReentrantLock();
            final Condition condition = lock.newCondition();
            final ThrowableWrapper throwableWrapper = new ThrowableWrapper();
            lock.lock();
            try {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        lock.lock();
                        try {
                            run.run();
                        } catch (Throwable e) {
                            throwableWrapper.t = e;
                        } finally {
                            try {
                                condition.signal();
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                });
                condition.await();
                if (throwableWrapper.t != null) {
                    throw new ExecutionException(throwableWrapper.t);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public static void injectFXML(Object rootAndController) throws IOException {

        String fileName = rootAndController.getClass().getSimpleName() + ".fxml";

        //System.out.println(rootAndController.getClass().getResource(fileName));
        injectFXML(rootAndController, fileName);
    }

    private static FXMLLoader loader = new FXMLLoader();

    public static void injectFXML(Object rootController, String location) throws IOException {
        FXMLLoader loader = new FXMLLoader(rootController.getClass().getResource(location));
        //FXMLLoader loader = new FXMLLoader();
        loader.setRoot(rootController);
        loader.setController(rootController);
        loader.setResources(ImageJFX.getResourceBundle());
        loader.setLocation(rootController.getClass().getResource(location));
        loader.setClassLoader(rootController.getClass().getClassLoader());

        loader.load();

        URL css
                = rootController.getClass().getResource(rootController.getClass().getSimpleName() + ".css");

        try {
            if (css != null) {

                // gets the root of the loader
                Node root = loader.getRoot();

                // get the url of the css
                String url = css.toExternalForm();

                // going through the list to check if there is an existing URL
                String existingURL = root.getScene()
                        .getStylesheets()
                        .stream()
                        .filter(str -> str.equals(url))
                        .findFirst().orElse(null);

                // if there is, it's deleted from the list to update the Scene with
                // possible modification
                if (existingURL != null) {
                    root.getScene().getStylesheets().remove(existingURL);
                }
                root.getScene().getStylesheets().add(css.toExternalForm());
            }
        } catch (Exception e) {
            Logger.getGlobal().warning("No CSS found for " + rootController.getClass().getSimpleName());
        }

    }

    public static void injectFXMLSafe(final Object controller) throws IOException {
        try {
            runAndWait(() -> {
                try {
                    injectFXML(controller);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "Couldn't load CSS", e);
        }

    }

    public static String javaClassToName(String className) {

        StringBuilder builder = new StringBuilder(30);

        if (className == null) {
            return "";
        }

        int start;

        if (className.contains(".")) {
            start = className.lastIndexOf('.') + 2;
        } else {
            start = 1;
        }

        builder.append(className.charAt(start - 1));

        boolean isFirstUppercase = true;

        //if(className.length() < 2) return className;
        for (int i = start; i < className.length(); i++) {

            char c = className.charAt(i);

            // if(i == start) builder.append(c);
            if (Character.isUpperCase(c)) {
                if (isFirstUppercase) {
                    builder.append(" ");
                }

                isFirstUppercase = false;
            } else {

                isFirstUppercase = true;
            }

            builder.append(Character.toLowerCase(c));
        }

        return builder.toString();
    }

    public static String javaClassToName(Class<?> clazz) {
        return javaClassToName(clazz.getName());
    }

    public static File openFile(String title, String defaultFolder, String extensionTitle, String... extensions) {
        Task<File> task = new Task<File>() {
            public File call() {
                FileChooser fileChooser = new FileChooser();

                File file = null;
                fileChooser.setTitle(title);
                fileChooser.setInitialDirectory(new File(defaultFolder));
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(extensionTitle, extensions));
                file = fileChooser.showOpenDialog(null);
                return file;
            }
        };
        Platform.runLater(task);

        try {
            return task.get();
        } catch (InterruptedException ex) {
            ImageJFX.getLogger();
        } catch (ExecutionException ex) {
            ImageJFX.getLogger();
        }

        return null;
    }

    public static List<File> openFiles(String title, String defaultFolder, String extensionTitle, String... extensions) {
        Task<List<File>> task = new Task<List<File>>() {
            public List<File> call() {
                FileChooser fileChooser = new FileChooser();

                List<File> files = null;
                fileChooser.setTitle(title);
                if (defaultFolder != null) {
                    fileChooser.setInitialDirectory(new File(defaultFolder));
                }
                if (extensionTitle != null) {
                    fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(extensionTitle, extensions));
                }
                files = fileChooser.showOpenMultipleDialog(null);

                return files;
            }
        };

        try {
            runAndWait(task);

            return task.get();
        } catch (InterruptedException ex) {
            ImageJFX.getLogger();
        } catch (ExecutionException ex) {
            ImageJFX.getLogger();
        }

        return null;
    }

    public static File openFolder(String title, String defaultFolder) {
        Task<File> task = new Task<File>() {
            public File call() {
                DirectoryChooser fileChooser = new DirectoryChooser();

                File file = null;
                fileChooser.setTitle(title);
                if (defaultFolder != null) {
                    fileChooser.setInitialDirectory(new File(defaultFolder));
                }

                file = fileChooser.showDialog(null);

                return file;
            }
        };

        try {
            if (Platform.isFxApplicationThread()) {
                task.run();
            } else {
                runAndWait(task);
            }

            return task.get();
        } catch (Exception ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public static File saveFileSync(String title, String defaultFolder, String extensionTitle, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        File file = null;
        fileChooser.setTitle(title);
        if(defaultFolder != null) fileChooser.setInitialDirectory(new File(defaultFolder));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(extensionTitle, extensions));
        file = fileChooser.showSaveDialog(null);
        return file;
    }

    public static File saveFile(String title, String defaultFolder, String extensionTitle, String... extensions) {
        Task<File> task = new Task<File>() {
            public File call() {
                FileChooser fileChooser = new FileChooser();
                File file = null;
                fileChooser.setTitle(title);
                fileChooser.setInitialDirectory(new File(defaultFolder));
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(extensionTitle, extensions));
                file = fileChooser.showSaveDialog(null);
                return file;
            }
        };

        try {

            if (Platform.isFxApplicationThread()) {
                task.run();

            } else {
                Platform.runLater(task);
            }

            return task.get();
        } catch (InterruptedException ex) {
            ImageJFX.getLogger();
        } catch (ExecutionException ex) {
            ImageJFX.getLogger();
        }

        return null;
    }

    public static void toggleCssStyle(Node node, String styleClass) {

        toggleCssStyle(node, styleClass, node.getStyleClass().contains(styleClass));

    }

    public static void toggleCssStyle(Node node, String styleClass, boolean toggle) {

        if (toggle) {
            if (node.getStyleClass().contains(styleClass) == false) {
                node.getStyleClass().add(styleClass);
            }
        } else {
            while (node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().remove(styleClass);
            }
        }

    }

    public static <T> void bindList(final ObservableList<T> listToUpdate, final ObservableList<? extends T> changingList) {

        changingList.addListener((ListChangeListener.Change<? extends T> c) -> {

            while (c.next()) {

                listToUpdate.removeAll(c.getRemoved());
                listToUpdate.addAll(c.getAddedSubList());

            }
        });

    }
}
