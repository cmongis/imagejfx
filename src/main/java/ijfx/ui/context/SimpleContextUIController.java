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

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class SimpleContextUIController extends ArrayList<ContextualWidget> implements ContextualView {

    String name;

    /**
     *
     * @param name
     */
    public SimpleContextUIController(String name) {
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
    public SimpleContextUIController registerWidget(ContextualWidget widget) {
        add(widget);
        return this;
    }

    /**
     *
     * @return
     */
    public List<ContextualWidget> getWidgetList() {
        return this;
    }

    /**
     *
     * @param toShow
     * @param toHide
     * @return
     */
    @Override
    public ContextualView onContextChanged(List<? extends ContextualWidget> toShow, List<? extends ContextualWidget> toHide) {
        toShow.forEach(widget -> widget.show());
        toHide.forEach(widget -> widget.hide());
        return this;
    }
}
