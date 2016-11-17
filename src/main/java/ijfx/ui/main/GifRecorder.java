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
package ijfx.ui.main;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class GifRecorder {

    private final Node node;

    public GifRecorder(Node node) {
        try {
            this.r = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(GifRecorder.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.node = node;

        node.getScene().addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        node.addEventHandler(MouseEvent.ANY, this::onMouseMoved);
        node.addEventHandler(DragEvent.ANY, this::onMouseDraged);
    }

    private final SnapshotParameters params = new SnapshotParameters();

    private int delay = 1000 / 10;

    private boolean shouldContinue;
    private long lastClick;
    private Thread currentThread;

    Consumer<String> notifier = System.out::println;

    boolean isMousePressed = false;

    public void toggle() {
        if (currentThread == null) {
            start();
        } else {
            stop();
        }
    }

    private void notify(String text) {
        notifier.accept(text);
    }

    public void start() {
        if (currentThread == null) {
            notify("Started Gif Recording");
            currentThread = new Thread(this::runCapture);
            currentThread.start();
        }
    }

    public GifRecorder setNotifier(Consumer<String> consumer) {
        this.notifier = consumer;
        return this;
    }

    public void stop() {

        currentThread = null;
        shouldContinue = false;
    }

    private void runCapture() {

        try {
            shouldContinue = true;
            AnimatedGifEncoder recorder = new AnimatedGifEncoder();
            recorder.start("animation-tmp.gif");
            recorder.setDelay(delay);
            recorder.setRepeat(0);
            int radius = 15;
            int x, y;
            Color color = new Color(1.0f, 1.0f, 1.0f, 0.5f);
            Color border = color.darker().darker();

            ExecutorService executor = Executors.newFixedThreadPool(1);
            while (shouldContinue) {
                long time = System.currentTimeMillis();
                x = toInt(lastX) - (radius / 2);
                y = toInt(lastY) - (radius / 2);
                BufferedImage image = capture();
                Graphics graphics = image.getGraphics();
                graphics.setColor(border);

                graphics.drawOval(x, y, radius, radius);
                graphics.drawOval(x + 1, y + 1, radius - 2, radius - 2);

                if (System.currentTimeMillis() - lastClick < 1000) {
                    graphics.setColor(Color.WHITE);
                } else {
                    graphics.setColor(color);
                }
                graphics.fillOval(x, y, radius, radius);

                executor.execute(() -> recorder.addFrame(image));

                time = System.currentTimeMillis() - time;

                System.out.println(String.format("Captured in %s ms", time));

                if (delay > time) {
                    Thread.sleep(delay - time);
                }

            }
            notify("Stop Gif recording");
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.SECONDS);

            recorder.finish();
            notify("Gif written");

        } catch (Exception ex) {
            Logger.getLogger(GifRecorder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public BufferedImage capture() {
        return captureFromScreen();
    }

    Robot r;

    public BufferedImage captureFromScreen() {

        Window window = node.getScene().getWindow();
        int x = toInt(window.getX());
        int y = toInt(window.getY());
        int w = toInt(window.getWidth());
        int h = toInt(window.getHeight());

        return r.createScreenCapture(new Rectangle(x, y, w, h));

    }

    public BufferedImage captureFromNode() {

        try {
            Task<WritableImage> task = new Task<WritableImage>() {
                public WritableImage call() {
                    return node.snapshot(params, null);
                }
            };

            Platform.runLater(task);

            WritableImage wi = task.get();
            return SwingFXUtils.fromFXImage((Image) wi, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private int toInt(double d) {
        return new Double(d).intValue();
    }

    private KeyCodeCombination comb = new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN);

    private void onKeyPressed(KeyEvent event) {
        if (comb.match(event)) {
            toggle();
        }
    }

    double lastX;
    double lastY;

    private void onMouseMoved(MouseEvent event) {
        lastX = event.getSceneX();
        lastY = event.getSceneY();
        isMousePressed = event.isPrimaryButtonDown() || event.isSecondaryButtonDown() || event.isMiddleButtonDown();
        if(isMousePressed) lastClick = System.currentTimeMillis();
    }

    private void onMouseDraged(DragEvent event) {
        lastX = event.getSceneX();
        lastY = event.getSceneY();
        isMousePressed = event.isDropCompleted() == false;
    }

}
