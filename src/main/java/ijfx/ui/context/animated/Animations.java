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
package ijfx.ui.context.animated;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * List of animations that can be used in any situation. It defines also an
 * interface for creating your own :-D
 *
 * @author Cyril MONGIS, 2015
 */
// configure and node and generate a animation.
public interface Animations {

    /**
     *
     * @param node
     * @param miliseconds
     * @return
     */
    public Transition configure(Node node, double miliseconds);

    /**
     *
     */
    public static Animations ZOOMIN = (node, ms) -> {

        ScaleTransition t = new ScaleTransition(new Duration(ms), node);
        t.setFromX(0);
        t.setFromY(0);
        t.setToX(t.getNode().getScaleX());
        t.setToY(t.getNode().getScaleY());

        return t;

    };

    /**
     *
     */
    public static Animations ZOOMOUT = (node, ms) -> {
        ScaleTransition t = new ScaleTransition(new Duration(ms), node);
        t.setFromX(t.getNode().getScaleX());
        t.setFromX(t.getNode().getScaleY());
        t.setToX(0);
        t.setToY(0);
        return t;
    };

    /**
     *
     */
    public static Animations DISAPPEARS_LEFT = (node, ms) -> {

        TranslateTransition t = new TranslateTransition(new Duration(ms), node);
        double shift = node.getBoundsInParent().getWidth();
        if (shift == 0) {
            shift = 200;
        }
        t.setFromX(node.getTranslateX());
        t.setToX(node.getTranslateX() - shift);

        return t;
    };

    /**
     *
     */
    public static Animations APPEARS_LEFT = (node, ms) -> {
        TranslateTransition t = new TranslateTransition(new Duration(ms), node);
        t.setToX(node.getTranslateX());
        t.setFromX(node.getTranslateX() - node.getBoundsInParent().getWidth());

        return t;
    };

    /**
     *
     */
    public static Animations DISAPPEARS_RIGHT = (node, ms) -> {

        TranslateTransition t = new TranslateTransition(new Duration(ms), node);

        t.setFromX(node.getTranslateX());
        t.setToX(node.getTranslateX() + node.getBoundsInParent().getWidth());
        return t;
    };

    /**
     *
     */
    public static Animations APPEARS_RIGHT = (node, ms) -> {
        TranslateTransition t = new TranslateTransition(new Duration(ms), node);
        t.setToX(node.getTranslateX());
        t.setFromX(node.getTranslateX() + node.getBoundsInParent().getWidth());
        return t;
    };

    /**
     *
     */
    public static Animations DISAPPEARS_UP = (node, ms) -> {

        TranslateTransition t = new TranslateTransition(new Duration(ms), node);

        t.setFromY(node.getTranslateY());
        t.setToY(node.getTranslateY() - node.getBoundsInParent().getHeight());

        return t;
    };

    /**
     *
     */
    public static Animations APPEARS_UP = (node, ms) -> {
        TranslateTransition t = new TranslateTransition(new Duration(ms), node);

        t.setToY(node.getTranslateY());
        t.setFromY(node.getTranslateY() - node.getBoundsInParent().getHeight());
        return t;
    };

    /**
     *
     */
    public static Animations DISAPPEARS_DOWN = (node, ms) -> {

        TranslateTransition t = new TranslateTransition(new Duration(ms), node);

        t.setFromY(node.getTranslateY());
        t.setToY(node.getBoundsInParent().getHeight());

        return t;
    };

    /**
     *
     */
    public static Animations APPEARS_DOWN = (node, ms) -> {
        TranslateTransition t = new TranslateTransition(new Duration(ms), node);

        t.setFromY(node.getBoundsInParent().getHeight());
        t.setToY(0);
        return t;
    };

    /**
     *
     */
    public static Animations FADEIN = (node, ms) -> {
        FadeTransition t = new FadeTransition(new Duration(ms), node);
        t.setFromValue(0);
        t.setToValue(1);
        return t;
    };

    /**
     *
     */
    public static Animations FADEOUT = (node, ms) -> {
        FadeTransition t = new FadeTransition(new Duration(ms), node);
        t.setFromValue(1);
        t.setToValue(0);
        return t;
    };

    /**
     *
     */
    public static Animations NOTHING = (node, ms) -> {
        return new Transition() {

            @Override
            protected void interpolate(double frac) {
            }
        };
    };
    
    public static Animations QUICK_EXPAND = (node,ms)-> {
        SequentialTransition sequence = new SequentialTransition();
        
        ScaleTransition growth = new ScaleTransition(Duration.millis(ms), node);
        ScaleTransition shrink = new ScaleTransition(Duration.millis(ms), node);
        
        double original = 1;
        double fnal = 2.0;
        
        growth.setFromX(original);
        growth.setToX(fnal);
        growth.setFromY(original);
        growth.setToY(fnal);
        
        shrink.setFromY(fnal);
        shrink.setToY(original);
        shrink.setFromX(fnal);
        shrink.setToX(original);
        
        
        sequence.getChildren().addAll(growth,shrink);
        
        return sequence;
    };
    
   
    
    

}
