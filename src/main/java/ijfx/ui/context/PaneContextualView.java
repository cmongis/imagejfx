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
import javafx.application.Platform;
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
        ContextualPaneIconWrapper wrapper = new ContextualPaneIconWrapper(pane, node, context);
        wrapper.setOrder(wrapperList.size());
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

        final List<Node> actual = new ArrayList();
        actual.addAll(pane.getChildren());

        actual.removeAll(toHide.stream().map(widget -> widget.getObject()).collect(Collectors.toList()));
        actual.addAll(toShow.stream().map(widget -> widget.getObject()).collect(Collectors.toList()));
        actual.sort(this::compare);


        Platform.runLater(() -> {
            pane.getChildren().clear();
            pane.getChildren().addAll(actual);

        });

        return this;
    }

    public Pane getPane() {
        return pane;
    }

    public int compare(Node node1, Node node2) {

        ContextualWidget<Node> w1 = getWrapperFromNode(node1);
        ContextualWidget<Node> w2 = getWrapperFromNode(node2);

        if (w1 == null && w2 == null) {
            return 0;
        }
        if (w1 == null) {
            return -100;
        }
        if (w2 == null) {
            return 100;
        }

        return Double.compare(w1.getPriority(), w2.getPriority());

    }

    private ContextualWidget<Node> getWrapperFromNode(Node node) {
        return wrapperList
                .stream().filter(wrapper -> wrapper.getObject() == node).findFirst().orElse(null);
    }

}
