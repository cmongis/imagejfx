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
import ijfx.ui.context.animated.Animations;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;

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
     private final DoubleProperty size = new SimpleDoubleProperty();
    protected final double  circleGrowth = 0.1;
    
    Transition iconTransition;
    
    public LoadingIcon(double size) {
        super();

        setPrefHeight(size);
        setPrefWidth(size);
        
        //iconNode = new FontAwesomeIconView(FontAwesomeIcon.CIRCLE_ALT_NOTCH);
        
        // creatin the node that contains the circle
        iconNode = GlyphsDude.createIcon(FontAwesomeIcon.CIRCLE_ALT_NOTCH,"16");
        iconNode.setFill(Color.WHITE);
        
        // binding it scales so it adapt to the StackPane size
        iconNode.scaleXProperty().bind(Bindings.createDoubleBinding(this::calculateScale, sizeProperty()));
        iconNode.scaleYProperty().bind(Bindings.createDoubleBinding(this::calculateScale,sizeProperty()));
        
        // creating a circle so the radius also adapt to the scale pane
        circle = new Circle(size * (1.0 - circleGrowth));
         
        circle.radiusProperty().bind(Bindings.createDoubleBinding(this::calculateCircleRadius, sizeProperty()));
        
        circle.setStroke(Color.WHITE);
        
        //
        circle.setStrokeWidth(size * 2 / 16);
        circle.setStroke(Color.WHITE);
        circle.setFill(null);


// adding classes
        iconNode.getStyleClass().add(CSS_CLASS_GLYPH);
        circle.getStyleClass().add(CSS_CLASS_CIRCLE);
        getStyleClass().add(CSS_CLASS);
        
        getChildren().addAll(circle,iconNode);
        
        sizeProperty().bind(prefWidthProperty());

        opacityProperty().addListener(this::onOpacityChanged);
        
    }
   
    public void onOpacityChanged(Observable obs, Number oldValue, Number newValue) {
        
        Status now = iconTransition.getStatus();
        if(newValue.doubleValue() == 0.0) {
            iconTransition.stop();
        }
        
        
        else if(now == Status.STOPPED || now == Status.PAUSED) {
            iconTransition.play();
        }
    }

    
    
    public double getSize() {
        return size.get();
    }

    public void setSize(double value) {
        size.set(value);
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    
    private double calculateScale() {
        return getSize() * 0.85 / 16;
    }
    
    private double calculateCircleRadius() {
        System.out.println(getSize());
        return sizeProperty().doubleValue() * (1. - circleGrowth) - circle.getStrokeWidth();
    }
        
    public Animation getIconAnimation() {
        if(iconTransition == null) {
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
        return iconTransition;
    }
    
    public void play() {
        Animations.FADEIN.configure(this, ImageJFX.getAnimationDurationAsDouble());
        getIconAnimation().play();
    }
    
    public void stop() {
        Animations.FADEOUT.configure(this, ImageJFX.getAnimationDurationAsDouble());
        getIconAnimation().stop();
    }
    

}
