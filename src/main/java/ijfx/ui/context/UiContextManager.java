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

/**
 *
 * @author Cyril MONGIS, 2015
 */
public interface UiContextManager {

    /**
     *
     */
    public static String LINK_VISIBLE = "visible";

    /**
     *
     */
    public static String LINK_ENABLE = "enable";

    /**
     *
     * @param context
     * @return
     */
    public UiContextManager registerContext(String context);

    /**
     *
     * @param context
     * @return
     */
    public UiContextManager enter(String context);

    /**
     *
     * @param contextArray
     * @return
     */
    public default UiContextManager enter(String... contextArray) {
        for (String context : contextArray) {
            enter(context);
        }
        return this;
    }

    /**
     *
     * @param contextArray
     * @return
     */
    public default UiContextManager leave(String... contextArray) {
        for (String context : contextArray) {
            leave(context);
        }
        return this;
    }

    
   
    
    public default UiContextManager toggleContext(String context, boolean toggle) {
        
        if(toggle) enter(context);
        else leave(context);
        return this;
    }
    
    
    public boolean isCurrent(String context);
        
    
    
    /**
     *
     * @param widget
     * @return
     */
    public boolean isDisplayed(String widget);

    /**
     *
     * @param widget
     * @return
     */
    public boolean isEnabled(String widget);

    /**
     *
     * @param context
     * @return
     */
    public UiContextManager leave(String context);

    /**
     *
     * @return
     */
    public UiContextManager update();

    /**
     *
     * @param widget
     * @param context
     * @return
     */
    public default UiContextManager link(String widget, String context) {
        link(widget, context, LinkType.ENABLED);
        link(widget, context, LinkType.VISIBLE);
        return this;
    }

    /**
     *
     * @param contextName
     * @return
     */
    public UiContext getUIContext(String contextName);

    /**
     *
     * @param widgetName
     * @return
     */
    public ContextualWidget getContextualWidget(String widgetName);

    /**
     *
     * @param widget
     * @param context
     * @param linkType
     * @return
     */
    public UiContextManager link(String widget, String context, String linkType);

    /**
     *
     * @param widget
     * @param context
     * @param linkType
     * @return
     */
    public UiContextManager unlink(String widget, String context, String linkType);

    /**
     *
     * @param contextualView
     * @return
     */
    public <T> UiContextManager  addContextualView(ContextualView<T> contextualView);

}
