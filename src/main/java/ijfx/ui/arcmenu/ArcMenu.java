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

import ijfx.ui.main.ImageJFX;

import java.util.logging.Logger;
import java.util.stream.IntStream;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import ijfx.ui.context.animated.Animations;
import java.util.List;
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.control.Skin;
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
public class ArcMenu extends StackPane implements ArcMenuSkin{

   
   
    

    Logger logger = ImageJFX.getLogger();


    PopArcMenu popArcMenu;
    
    
     Text text = new Text();
    /**
     *
     */
    protected boolean isAnimating = false;

    // attached stack pane

    /**
     *
     */
    public static int ANIMATION_SPEED = 100;

    /**
     * Creates a ArcMenu. T
     */
    public ArcMenu(PopArcMenu menu) {
        super();

        getStyleClass().addAll("arc-group");
        setVisible(true);
        //attachedPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        text.setFill(Color.WHITE);

        popArcMenu = menu;
        setStyle("-fx-background-color:null");
        setPrefWidth(300);
        setPrefHeight(300);
        menu.showingProperty().addListener(this::toggle);
        //setSkin(new ArcMenuSk());
        
        
        addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClick);
        
    }
    
    
    
    public List<ArcItem> getItems() {
        return getSkinnable().getItems();
    }

   

    
    public Node getRoot() {
        return this;
    }
    
   

    /**
     *
     * @param pane
     */
    public void attachedTo(Pane pane) {
        //attachedPane = pane;
        //attachedPane.addEventHandler(MouseEvent.MOUSE_CLICKED,this::toggle);
    }

  
    boolean isShowing = false;
    
    /**
     *
     * @param event
     */
    public void toggle(Observable obs, Boolean oldvalue, Boolean show) {

        logger.info("Toggling");
        prefWidth(400);
        prefHeight(400);
        // if not pane is attached, we return
        if (isAnimating) {
            return;
        }

        // cheking if the attached pane is already showing the element
        if (!show) {

            logger.info("Hiding");

            // creating the fading transition that removes the object
            Transition transition = Animations.FADEOUT.configure(this, ANIMATION_SPEED);

           

            transition.play();
 
        } else {

            logger.info("Showing");
            // setting the opacity
            this.setOpacity(1.0);

            animate();
            
        }
    }

    /**
     *
     */
    public void build() {
        
        List<ArcItem> items = getItems();
        
        double itemLength = 360f / items.size();
        getChildren().clear();

        double margin = 7;

        double minRadius = popArcMenu.getMinRadius();
        double maxRadius = popArcMenu.getMaxRadius();
        
        
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
            PolarSystem ps = new PolarSystem(popArcMenu.getCenterX(), popArcMenu.getCenterY());
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
        text.setTranslateY(popArcMenu.getMaxRadius() + 40);
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

        getItems().forEach(itemCtrl -> {
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
        IntStream.range(0, getItems().size()).forEach(index -> {

            ArcItem item = getItems().get(index);
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
                    .getLocationCloserToCenter(popArcMenu.getMinRadius());

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
     * @param stackPane
     */
    public void detachFrom(Pane stackPane) {

        if (stackPane.getChildren().contains(this)) {
            stackPane.getChildren().remove(this);
        }
        
    }

    @Override
    public PopArcMenu getSkinnable() {
        return popArcMenu;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void dispose() {
    }

    public void onMouseClick(MouseEvent event) {
        if(event.getTarget() == this)
        popArcMenu.hide();
        
    }
    
}
