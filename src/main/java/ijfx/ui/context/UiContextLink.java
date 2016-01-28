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
import java.util.Collection;
import mongis.utils.ConditionList;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class UiContextLink implements Comparable<UiContextLink> {

    UiContext context;
    ContextualWidget widget;
    String linkType;
    String contextName;
    String widgetName;

    /**
     *
     * @param widget
     * @param uiContext
     * @param linkType
     */
    public UiContextLink(String widget, String uiContext, String linkType) {
        this.widgetName = widget;
        this.contextName = uiContext;
        this.linkType = linkType;
    }

    /**
     *
     * @return
     */
    public String getContextName() {
        return contextName;
    }

    /**
     *
     * @return
     */
    public String getWidgetName() {
        return widgetName;
    }

    /**
     *
     * @return
     */
    public UiContext getContext() {
        return context;
    }

    /**
     *
     * @param context
     */
    public void setContext(UiContext context) {
        this.context = context;
    }

    /**
     *
     * @return
     */
    public ContextualWidget getWidget() {
        return widget;
    }

    /**
     *
     * @param widget
     */
    public void setWidget(ContextualWidget widget) {
        this.widget = widget;
    }

    /**
     *
     * @return
     */
    public String getLinkType() {
        return linkType;
    }

    /**
     *
     * @param linkType
     */
    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    /**
     *
     * @param manager
     * @return
     */
    public UiContextLink updateLink(UiContextManager manager) {
        setContext(manager.getUIContext(contextName));
        setWidget(manager.getContextualWidget(widgetName));
        return this;
    }

    /**
     *
     * @param msg
     */
    public void log(String msg) {

    }

    public boolean fullFill(Collection<String> currentContextList) {

        if (contextName.contains("+")) {
           
            String[] contextList = contextName.split("[\\+]");
           
            ConditionList requiredContextPresence = new ConditionList();
            for (String requiredContext : contextList) {

                //System.out.println(String.format("%s contains %s", requiredContext, currentContextList.contains(requiredContext)));
                requiredContextPresence.add(currentContextList.contains(requiredContext));

            }

            return requiredContextPresence.isAllTrue();
        }
        return currentContextList.contains(contextName);
    }
    
    public boolean negate(Collection<String> currentContextList) {
        if(contextName.startsWith("-") && currentContextList.contains(contextName.substring(1))) {
              return true;
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(getWidgetName()).append(" --> ").append(getContextName()).append(" by ").append(getLinkType()).append("]");
        return builder.toString();
    }

    @Override
    public int compareTo(UiContextLink o) {
        if (true) {
            return o.toString().compareTo(toString());
        }
        if (getWidget() == o.getWidget() && getContext() == o.getContext() && getLinkType().equals(o.getLinkType())) {
            return 0;
        }
        return -1;
    }

}
