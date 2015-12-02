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

import ijfx.service.thumb.ThumbService;
import ijfx.ui.context.animated.Animation;
import ijfx.ui.context.animated.TransitionQueue;
import ijfx.ui.plugin.panel.LUTPanel;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import net.imagej.ImageJ;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class UITesterCtrl implements Initializable {

    @FXML
    VBox vbox;

    @FXML
    ToggleButton toggleButton;

    @FXML
    ImageView imageView;

    @FXML
    Button selectButton;

    ObjectProperty<File> planeDBProperty = new SimpleObjectProperty<File>();

    ImageJ imageJ = new ImageJ();

    @Parameter
    ThumbService thumbService;

    TransitionQueue queue = new TransitionQueue();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        vbox.getChildren().add(new LUTPanel());
        imageJ.context().inject(this);
        planeDBProperty.addListener(this::onSelectionChanged);

    }

    @FXML
    public void selectFile() {
        FileChooser chooser = new FileChooser();
        File f = chooser.showOpenDialog(null);

        if (f != null) {
            planeDBProperty.setValue(f);
            selectButton.setText(f.getName());
        }

    }

    public void onSelectionChanged(Observable obs, File oldValue, File newValue) {
        selectButton.setText("Loading...");
        ImageJFX.getThreadPool().submit(
                new Task<Image>() {

                    @Override
                    protected Image call() throws Exception {
                        return thumbService.getThumb(newValue, 20, 300, 300);
                    }

                    @Override
                    protected void succeeded() {
                        imageView.setImage(getValue());
                    }
                });

    }

    @FXML
    public void runAnimation(ActionEvent event) {

        Transition tr = Animation.FADEOUT.configure(toggleButton, 300);
        tr.setOnFinished(evt -> System.out.println("something"));
        queue.emptyQueue();
        queue.queue(tr);
        queue.queue(new PauseTransition(Duration.seconds(1)));
        queue.queue(Animation.FADEIN.configure(toggleButton, 300));

    }

}
