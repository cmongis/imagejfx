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

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.ui.context.animated.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class LoadingIcon extends StackPane {

    Text iconNode;
    Circle circle;

    public static final String CSS_CLASS = "loading-icon";
    public static final String CSS_CLASS_CIRCLE = "circle";
    public static final String CSS_CLASS_GLYPH = "glyph";
    
    protected final double  circleGrowth = 0.1;
    
    public LoadingIcon(double size) {
        super();

        setPrefHeight(size);
        setPrefWidth(size);
        
        //iconNode = new FontAwesomeIconView(FontAwesomeIcon.CIRCLE_ALT_NOTCH);
        iconNode = GlyphsDude.createIcon(FontAwesomeIcon.CIRCLE_ALT_NOTCH);
        iconNode.setFill(Color.WHITE);
        iconNode.setScaleX(16.0 / size /2);
        iconNode.setScaleY(16.0 / size /2);
        circle = new Circle(size * (1.0 - circleGrowth));
        circle.setStroke(Color.WHITE);
        
        //
        circle.setStrokeWidth(size * 2.5 / 16);
        circle.setStroke(Color.WHITE);
        circle.setFill(null);


// adding classes
        iconNode.getStyleClass().add(CSS_CLASS_GLYPH);
        circle.getStyleClass().add(CSS_CLASS_CIRCLE);
        getStyleClass().add(CSS_CLASS);
        
        getChildren().addAll(circle,iconNode);
        
        

    }
    private final DoubleProperty size = new SimpleDoubleProperty();

    public double getSize() {
        return size.get();
    }

    public void setSize(double value) {
        size.set(value);
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    
    
    
    Transition iconTransition;

    public void play() {
        if (iconTransition == null) {
            RotateTransition rotateTransition;

            // animating the loading thing
            rotateTransition = new RotateTransition(Duration.millis(1000), iconNode);
            rotateTransition.setByAngle(360);
            //rotateTransition.setCycleCount(300);
            rotateTransition.setInterpolator(Interpolator.LINEAR);
            double scale = 1 + circleGrowth;
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
        Animation.FADEIN.configure(this, ImageJFX.getAnimationDurationAsDouble());
        iconTransition.play();
    }
    
    public void stop() {
        Animation.FADEOUT.configure(this, ImageJFX.getAnimationDurationAsDouble());
        if(iconTransition != null) iconTransition.stop();
    }
    

}
