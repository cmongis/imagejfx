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
package ijfx.ui.deprecated;

import ijfx.ui.context.ContextualUIController;
import ijfx.ui.context.ContextualWidget;
import ijfx.ui.context.NodeContextualWidget;
import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import ijfx.ui.context.animated.Animations;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class ContextualDisapearingPaneCtrl extends HashMap<String, NodeContextualWidget> implements ContextualUIController {

    String name;

    Parent parentNode;

    Animations animationOnShow = Animations.FADEIN;
    Animations animationOnHide = Animations.FADEOUT;

    List<ContextualWidget> toShow;
    List<ContextualWidget> toHide;

    Logger logger;

    int speed = 200;

    public ContextualDisapearingPaneCtrl(String name, Parent node) {
        setName(name);
        setParentNode(node);
        logger = ImageJFX.getLogger();

    }

    public ContextualDisapearingPaneCtrl(Parent node) {
        setName(node.getId());
        setParentNode(node);
        node.getChildrenUnmodifiable().forEach(child -> registerNode(child));
        logger = ImageJFX.getLogger();
    }

    public void setName(String name) {
        this.name = name;
    }

    public Parent getParentNode() {
        return parentNode;
    }

    public void setParentNode(Parent parentNode) {
        this.parentNode = parentNode;
    }

    public Animations getAnimationOnShow() {
        return animationOnShow;
    }

    public void setAnimationOnShow(Animations animationOnShow) {
        this.animationOnShow = animationOnShow;
    }

    public Animations getAnimationOnHide() {
        return animationOnHide;
    }

    public ContextualDisapearingPaneCtrl setAnimationOnHide(Animations animationOnHide) {
        this.animationOnHide = animationOnHide;
        return this;
    }

    public void registerNode(Node node) {
        put(node.getId(), new NodeContextualWidget(node));
    }

    public void registerNode(String id, Node node) {

        put(id, new NodeContextualWidget((id), node));

    }

    public int getSpeed() {
        return speed;
    }

    Lock lock = new ReentrantLock();

    public void lockPane() {

        try {
            lock.lockInterruptibly();
        } catch (InterruptedException ex) {
            ImageJFX.getLogger().log(Level.SEVERE,null,ex);
        }
    }

    public void unlockPane() {

        lock.unlock();
    }

    TransitionQueue transitionQueue = new TransitionQueue();

    public String msg(String format, Object... args) {
        format = String.format("[%s] ", getName()) + format;
        return String.format(format, args);
    }

    public void log(String format, Object... args) {
        logger.info(msg(format, args));
    }

    @Override
    public ContextualUIController onContextChanged(List<? extends ContextualWidget> toShow, List<? extends ContextualWidget> toHide) {

        // logging the items to show
        //logger.info(msg("Must show %d items : ", toShow.size()));
        //toShow.forEach(widget -> logger.info(" - " + widget.getName()));
        
        // logging the items to hide
        //logger.info("must hide " + toShow.size() + " items : ");
        //toHide.forEach(widget -> logger.info(" - " + widget.getName()));

        
        // if nothing must be done, nothing happens
        if (toShow.size() + toHide.size() == 0) {
            return this;
        }
        
        //creating the appearance and disapearance animations
        Transition disapearance = getAnimationOnHide().configure(getParentNode(), getSpeed());
        Transition appearance = getAnimationOnShow().configure(getParentNode(), getSpeed());
        
        // setting the update of the containers when the pane is not visible
        SequentialTransition sequence = new SequentialTransition(disapearance, appearance);
        disapearance.setOnFinished(actionEvent -> {
            log("Disapearance over");
            updateContainer(castedList(toShow), castedList(toHide));
        });
        
        // start the sequence by adding it to the animation queue
        log("Start disapearce !");
        
        transitionQueue.queue(sequence);

        return this;
    }

    public void updateContainer(List<NodeContextualWidget> toShow, List<NodeContextualWidget> toHide) {

        Pane pane = (Pane) getParentNode();

        toHide.forEach(widget -> {
            Node node = widget.getNode();
            logger.info("Removing  " + widget);
            pane.getChildren().remove(node);
            widget.hide();
        });
        toShow.forEach(widget -> {
            Node node = widget.getNode();

            widget.show();
            logger.info("Adding  " + widget + " to " + pane.getId());
            pane.getChildren().add(node);

            OrderService.getInstance().orderNode(pane.getChildren());

            pane.setVisible(true);

        });
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<ContextualWidget> getWidgetList() {
        ArrayList<ContextualWidget> list = new ArrayList<>();
        values().forEach(widget -> list.add(widget));
        return list;
    }

    public <A, B> ArrayList<B> castedList(List<A> origin) {
        ArrayList<B> newList = new ArrayList<>();
        origin.forEach(o -> newList.add((B) o));
        return newList;
    }

    public class TransitionQueue {

        ArrayList<Transition> queue = new ArrayList<>();

        public void queue(Transition transition) {

            queue.add(transition);
            transition.onFinishedProperty().addListener(actionEvent -> {

                playNext();
            });

            if (queue.size() == 1) {
                playNext();
            }

        }

        public void playNext() {
            if (queue.size() > 0) {
                queue.get(0).play();
                queue.remove(0);
            }
        }

    }

}
