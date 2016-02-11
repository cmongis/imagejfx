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

import ijfx.ui.context.ContextualWidget;
import java.util.HashMap;
import javafx.animation.Transition;
import javafx.scene.Node;

/**
 * Deprecated Class, don't bother with it
 *
 * @author Cyril MONGIS, 2015
 */
public class AnimatedContextualWidget implements ContextualWidget<Node> {

    String name;
    Node component;

    HashMap<String, Animations> transitionMap = new HashMap<>();

    Runnable onAnimationEnded;

    /**
     *
     * @param node
     * @param name
     */
    public AnimatedContextualWidget(Node node, String name) {
        this.name = name;
        this.component = node;

        setAnimation(AnimationAction.HIDE, Animations.NOTHING);
        setAnimation(AnimationAction.SHOW, Animations.NOTHING);

    }

    /**
     *
     * @param action
     * @param configurator
     * @return
     */
    public AnimatedContextualWidget setTransition(String action, Animations configurator) {
        return this;
    }

    /**
     *
     * @return
     */
    public Node getNode() {
        return component;
    }

    ;
    
    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isHidden() {
        return !component.isVisible();
    }

    /**
     *
     */
    @Override
    public void show() {
        component.setVisible(true);

    }

    /**
     *
     */
    @Override
    public void hide() {
        component.setVisible(false);
    }

    /**
     *
     */
    @Override
    public void enable() {
        component.setDisable(false);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return component.isDisabled() == false;
    }

    /**
     *
     */
    @Override
    public void disable() {
        component.setDisable(true);
    }

    /**
     *
     * @param action
     * @return
     */
    public Animations getAnimation(String action) {
        return transitionMap.get(action);
    }

    /**
     *
     * @param action
     * @param transition
     * @return
     */
    public AnimatedContextualWidget setAnimation(String action, Animations transition) {
        transitionMap.put(action, transition);
        return this;
    }

    /**
     *
     * @param action
     * @param duration
     * @return
     */
    public Transition getTransition(String action, int duration) {
        return getAnimation(action).configure(getNode(), duration);
    }

    double order = 1d;

    /**
     *
     * @return
     */
    public double getOrder() {
        return order;
    }

    /**
     *
     * @param order
     */
    public void setOrder(double order) {
        this.order = order;
    }

    @Override
    public Node getObject() {
        return component;
    }

}
