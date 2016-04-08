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
package ijfx.ui.context;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import ijfx.ui.previewToolbar.ContextualPaneIconWrapper;
import javafx.scene.layout.Pane;

/**
 *
 * @author cyril
 */
public class PaneContextualView implements ContextualView<Node> {

    final Pane pane;
    final String name;

    final UiContextManager manager;

    ArrayList<ContextualWidget<Node>> wrapperList = new ArrayList<>();

    public PaneContextualView(UiContextManager manager, Pane pane, String name) {
        this.pane = pane;
        this.name = name;
        this.manager = manager;
        manager.addContextualView(this);
    }

    public void registerNode(Node node, String context) {
        ContextualPaneIconWrapper wrapper = new ContextualPaneIconWrapper(pane, node);
        manager.link(wrapper.getName(), context);
        wrapperList.add(wrapper);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<ContextualWidget<Node>> getWidgetList() {
        return wrapperList;
    }

    @Override
    public ContextualView<Node> onContextChanged(List<? extends ContextualWidget<Node>> toShow, List<? extends ContextualWidget<Node>> toHide) {

        pane.getChildren().removeAll(toHide.stream().map(widget -> widget.getObject()).collect(Collectors.toList()));
        pane.getChildren().addAll(toShow.stream().map(widget -> widget.getObject()).collect(Collectors.toList()));

        return this;
    }
/*
    // widget that show itself by adding itself itside a pane
    private class ContextualPaneNodeWrapper implements ContextualWidget<Node> {

        final Pane parentPane;
        final Node node;

        public ContextualPaneNodeWrapper(Pane pane, Node node) {
            this.parentPane = pane;
            this.node = node;
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
    }*/

}
