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

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class NodeContextualView extends ArrayList<ContextualWidget<Node>> implements ContextualView<Node> {

    String name;

    /**
     *
     * @param name
     */
    public NodeContextualView(String name) {
        setName(name);
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param widget
     * @return
     */
    public NodeContextualView registerWidget(ContextualWidget widget) {
        add(widget);
        return this;
    }

    /**
     *
     * @return
     */
    public List<ContextualWidget<Node>> getWidgetList() {
        return this;
    }

    /**
     *
     * @param toShow
     * @param toHide
     * @return
     */
    @Override
    public ContextualView<Node> onContextChanged(List<? extends ContextualWidget<Node>> toShow, List<? extends ContextualWidget<Node>> toHide) {
        toShow.forEach(widget -> widget.show());
        toHide.forEach(widget -> widget.hide());
        return this;
    }
}
