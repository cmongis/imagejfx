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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import ijfx.ui.context.animated.Animations;

/**
 * FXML Controller class
 *
 * @author Cyril MONGIS, 2015
 */
public class LoadingIcon2Ctrl implements Initializable {

    @FXML
    FontAwesomeIconView icon;
    
    @FXML
    Circle circle;
    
    @FXML
    Group group;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    
    
    
    Transition iconTransition;

    public void play() {
        if (iconTransition == null) {
            RotateTransition rotateTransition;

            // animating the loading thing
            rotateTransition = new RotateTransition(Duration.millis(1000), icon);
            rotateTransition.setByAngle(360);
            //rotateTransition.setCycleCount(300);
            rotateTransition.setInterpolator(Interpolator.LINEAR);
            double scale = 1.1;
            // circle animatino : hearbeat
            SequentialTransition st = new SequentialTransition();
            Duration halfTime = Duration.millis(MainWindowController.ANIMATION_DURATION / 3);
            ScaleTransition scaleUp = new ScaleTransition(halfTime, circle);
            scaleUp.setToX(scale);
            scaleUp.setToY(scale);
            ScaleTransition scaleDown = new ScaleTransition(halfTime, circle);
            scaleDown.setToX(1);
            scaleDown.setToY(1);
            st.getChildren().addAll(scaleUp, scaleDown);
            st.setDelay(Duration.millis(1000 - 300));
            //st.setCycleCount(20);
            ParallelTransition pt = new ParallelTransition(rotateTransition, st);
            pt.setCycleCount(300);
            iconTransition = pt;
        }
        Animations.FADEIN.configure(group, ImageJFX.getAnimationDurationAsDouble());
        iconTransition.play();
    }
    
    public void stop() {
        Animations.FADEOUT.configure(group, ImageJFX.getAnimationDurationAsDouble());
        if(iconTransition != null) iconTransition.stop();
    }
    
}
