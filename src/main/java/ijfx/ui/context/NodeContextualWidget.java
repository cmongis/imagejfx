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
package ijfx.ui.context;

import javafx.scene.Node;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class NodeContextualWidget implements ContextualWidget<Node>{

    /**
     *
     * @param name
     * @param node
     */
    public NodeContextualWidget(String name, Node node) {

        setName(name);
        setNode(node);
    }

    /**
     *
     * @param node
     */
    public NodeContextualWidget(Node node) {
        setName(node.getId());
        setNode(node);

    }

    @Override
    public String toString() {
        return String.format("[NodeContextualWidget : name=%s, visible=%s, enabled=%s]", getName(), isHidden() == false, isEnabled());
    }

    public void setName(String name) {
        this.name = name;
    }
    Node node;
    String name;
    double order;

    /**
     *
     * @param name
     */
    public Node getNode() {
        return node;
    }

    /**
     *
     * @param node
     */
    public void setNode(Node node) {
        this.node = node;
    }

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
        return node.isVisible() == false || node.getParent() == null;
    }

    /**
     *
     */
    @Override
    public void enable() {
        node.setDisable(false);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return node.isDisabled() == false;
    }

    /**
     *
     */
    @Override
    public void disable() {
        node.setDisable(true);
    }

    /**
     *
     */
    @Override
    public void show() {
        node.setVisible(true);
    }

    /**
     *
     */
    @Override
    public void hide() {
        node.setVisible(false);
    }

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
        return node;
    }

}

/**
 *
 * @return
 */
