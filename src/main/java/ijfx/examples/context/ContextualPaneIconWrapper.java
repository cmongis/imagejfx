/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.examples.context;

import ijfx.ui.context.ContextualWidget;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import mongis.utils.panecell.PaneIconCell;

/**
 *
 * @author tuananh
 */
public class ContextualPaneIconWrapper extends PaneIconCell implements ContextualWidget, ContextualPaneIcon {

    private String context;
    private String pathIcon;
    private final Node node;
    private final Pane parentPane;

    public ContextualPaneIconWrapper(Pane parentPane, Node node) {
        this.node = node;
        this.parentPane = parentPane;
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public String getIcon() {
        return pathIcon;
    }

    @Override
    public Event getOnAction() {
        //TODO
        return null;
    }

    @Override
    public String getName() {
        return node.getId();
    }

    @Override
    public boolean isHidden() {
        return !parentPane.getChildren().contains(node);
    }

    @Override
    public void enable() {
        node.setDisable(false);
    }

    @Override
    public boolean isEnabled() {
        return !node.isDisabled();
    }

    @Override
    public void disable() {
        node.setDisable(true);
    }

    @Override
    public void show() {
        System.out.println("Showing " + node.getId());
        parentPane.getChildren().add(node);
        node.setVisible(true);
    }

    @Override
    public void hide() {
        System.out.println("Hiding " + node.getId());
        parentPane.getChildren().remove(node);
    }

    @Override
    public Node getObject() {
        System.out.println(node);
        return node;
    }

    public String toString() {
        return node.toString();
    }

}
