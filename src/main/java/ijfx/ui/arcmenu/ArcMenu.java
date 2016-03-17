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
package ijfx.ui.arcmenu;

import ijfx.ui.arcmenu.skin.ArcItemCircleSkin;
import ijfx.ui.main.ImageJFX;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import ijfx.ui.context.animated.Animations;
import javafx.scene.layout.Pane;

/**
 * The ArcMenu.
 *
 * The ArcMenu takes care of animating the ArcItems and deploy in the attached
 * StackPane when needed. The ArcMenu can only be attached to a StackPane
 * object. If not attached, the ArcMenu won't display.
 *
 * To create an ArcMenu you can for instance :
 *
 * ArcMenu menu = new ArcMenu(); menu.addItem(...); menu.addItem(...);
 * menu.addItem(...); menu.build(); menu.attachedTo(stackPane);
 *
 * @author Cyril MONGIS
 */
public class ArcMenu extends StackPane {

    double minRadius = 45.0f;
    double maxRadius = 70.0f;
    double centerX = 0.0f;
    double centerY = 0.0f;

    ArrayList<ArcItem> items = new ArrayList<>();

    Logger logger = ImageJFX.getLogger();

    Text text = new Text();

    /**
     *
     */
    protected boolean isAnimating = false;

    // attached stack pane
    Pane attachedPane = null;

    /**
     *
     */
    public static int ANIMATION_SPEED = 100;

    /**
     * Creates a ArcMenu. T
     */
    public ArcMenu() {
        super();

        getStyleClass().addAll("arc-group");
        setVisible(true);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        text.setFill(Color.WHITE);

    }

    /**
     * Add an ArcMenuItem
     *
     * @param item
     * @return the menu iteself
     */
    public ArcMenu addItem(ArcItem item) {
        items.add(item);

        item.setArcRenderer(this);
        item.setSkin(new ArcItemCircleSkin(item));
        return this;
    }

    /**
     *
     * @param items
     */
    public void addAll(ArcItem... items) {
        for (ArcItem item : items) {
            addItem(item);
        }

    }

    /**
     *
     * @param pane
     */
    public void attachedTo(Pane pane) {
        attachedPane = pane;
        attachedPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            toggle(event);

        });
    }

    /**
     *
     * @param event
     */
    public void toggle(MouseEvent event) {

        logger.info("Toggling");

        // if not pane is attached, we return
        if (attachedPane == null || isAnimating) {
            return;
        }

        // cheking if the attached pane is already showing the element
        if (attachedPane.getChildren().contains(this) && event.getEventType() == MouseEvent.MOUSE_CLICKED) {

            logger.info("Hiding");

            // creating the fading transition that removes the object
            Transition transition = Animations.FADEOUT.configure(this, ANIMATION_SPEED);

            // when the transition is over, the pane is removed
            transition.setOnFinished(evt -> {
                attachedPane.getChildren().remove(this);
                text.setText("");
            });

            transition.play();
        } else {

            logger.info("Showing");

            //if it wasn't a right button, we abort the function
            if (event.getButton() != MouseButton.SECONDARY) {
                logger.info("It wasn't the right button");
                return;
            }
            // adding the element to the pane
            attachedPane.getChildren().add(this);

            // setting the opacity
            this.setOpacity(1.0);

            // getting the width and height of the pane to calculate the translation
            // of the center (the group we be placed at the center by the stackpane)
            double paneWidth = attachedPane.widthProperty().getValue();
            double paneHeight = attachedPane.heightProperty().getValue();

            // calculating the distance to the center of the pane and the mouse event
            double xToCenter =  event.getX();//-paneWidth / 2 + event.getX();
           // xToCenter *= attachedPane.getScaleX();
            double yToCenter = event.getY(); //paneHeight / 2 + event.getY();
           // yToCenter *= 1 / attachedPane.getScaleY();

            
            //this.setLayoutX(centerX);
            //this.setLayoutY(centerY);
            
            // setting the translation 
            this.setTranslateX(xToCenter);
            this.setTranslateY(yToCenter);

            animate();
        }
    }

    /**
     *
     */
    public void build() {
        double itemLength = 360f / items.size();
        getChildren().clear();

        double margin = 7;

        for (int i = 0; i != items.size(); i++) {

            // calculating the start angle
            double itemStart = itemLength * i - 90f - itemLength / 2;

            // calculating the item start
            double itemCenter = itemStart + (itemLength / 2);

            // rendering the item
            ArcItem shape = items.get(i);//itemRenderer.render(items.get(i), i, items.size()); //new Circle(maxRadius - minRadius); //drawArc(centerX, centerY, minRadius, maxRadius, itemStart + (margin / 2), itemSubLength);

            //setting the style for the item
            shape.getStyleClass().add("arc-item");

            //setting the polar system and coordinates (easier to get x and y);
            PolarSystem ps = new PolarSystem(centerX, centerY);
            PolarCoord pc = new PolarCoord(ps, minRadius + ((maxRadius - minRadius) / 2), itemCenter);
            shape.setPolarCoordinates(pc);

            shape.setTranslateX(pc.getX());
            shape.setTranslateY(pc.getY());

            Label selectionLabel = shape.getSelectionLabel();
            getChildren().add(selectionLabel);

            // adding the shape
            getChildren().add(shape);
            addEvents(shape);
        }
        animate();
        addSlider();
    }

    /**
     *
     */
    protected void addSlider() {
        getChildren().add(text);
        text.setTranslateY(maxRadius + 40);
    }

    /**
     *
     * @param itemCtrl
     */
    protected void addEvents(ArcItem itemCtrl) {
        itemCtrl.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            animateOtherThan(itemCtrl, Animations.FADEOUT);

        });
        itemCtrl.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            animateOtherThan(itemCtrl, Animations.FADEIN);
        });
    }

    /**
     *
     * @param excludedItemCtrl
     * @param animation
     */
    protected void animateOtherThan(ArcItem excludedItemCtrl, Animations animation) {

        items.forEach(itemCtrl -> {
            if (itemCtrl == excludedItemCtrl) {
                return;
            }
            Transition transition = animation.configure(itemCtrl, ANIMATION_SPEED);
            transition.play();
            Transition labelTransition = animation.configure(itemCtrl.getSelectionLabel(), ANIMATION_SPEED);
            labelTransition.play();
        });

    }

    // animate the items appearance
    /**
     *
     */
    protected void animate() {

        // used in the loop
        int count = 0;

        // delay between the appereace of each item
        double delay = 50;

        // animation duration
        double duration = 200;

        logger.info("Animating !");

        // flag to avoid animation quircks
        isAnimating = true;

        // Each item appearing is a transition.
        // Those transitions are played in parallel with
        // a delay for a different delay for each.
        ParallelTransition animation = new ParallelTransition();

        // when the whole animation is finished, the flag is turned down
        animation.setOnFinished(event -> isAnimating = false);

        // for 0 to n-items
        IntStream.range(0, items.size()).forEach(index -> {

            ArcItem item = items.get(index);
            // we make the item invisible
            item.setOpacity(0);

            Duration delayDuration = Duration.millis(index * delay);

            // fade in transition of the item
            Transition fadeIn = Animations.FADEIN.configure(item, ANIMATION_SPEED);
            fadeIn.setDelay(delayDuration);

            // translate transition : from the center to his edge
            TranslateTransition fromCenter = new TranslateTransition(Duration.millis(duration), item);

            // calculating the departure coordinates.
            // The items don't go from the center but from a point
            // a bit further from the center.
            Point2D closerToCenterPoint = item
                    .getPolarCoordinates()
                    .getLocationCloserToCenter(minRadius);

            // configuring the translate transition
            fromCenter.setFromX(closerToCenterPoint.getX());
            fromCenter.setFromY(closerToCenterPoint.getY());
            fromCenter.setToX(item.getPolarCoordinates().getX());
            fromCenter.setToY(item.getPolarCoordinates().getY());
            fromCenter.setDelay(delayDuration); // same delay as the fade in

            // both animation will play with the others
            animation.getChildren().addAll(fadeIn, fromCenter);
            // fromCenter.play();
            //fadeIn.play();
        });

        // starting the animation
        animation.play();
    }

    /**
     *
     * @return
     */
    public double getMinRadius() {
        return minRadius;
    }

    /**
     *
     * @param minRadius
     */
    public void setMinRadius(double minRadius) {
        this.minRadius = minRadius;
    }

    /**
     *
     * @return
     */
    public double getMaxRadius() {
        return maxRadius;
    }

    /**
     *
     * @param maxRadius
     */
    public void setMaxRadius(double maxRadius) {
        this.maxRadius = maxRadius;
    }

    /**
     *
     * @return
     */
    public double getCenterX() {
        return centerX;
    }

    /**
     *
     * @param centerX
     */
    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    /**
     *
     * @return
     */
    public double getCenterY() {
        return centerY;
    }

    /**
     *
     * @param centerY
     */
    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    int getItemPosition(ArcItem item) {
        return items.indexOf(item);
    }

    int getItemCount() {
        return items.size();
    }

    /**
     *
     * @param stackPane
     */
    public void detachFrom(Pane stackPane) {

        if (stackPane.getChildren().contains(this)) {
            stackPane.getChildren().remove(this);
        }
        this.attachedPane = null;
    }

}
