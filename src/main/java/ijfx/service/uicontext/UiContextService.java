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
package ijfx.service.uicontext;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import ijfx.ui.context.UiContextLink;
import ijfx.ui.context.ContextLinkSet;
import ijfx.ui.context.ContextualView;
import ijfx.ui.context.ContextualWidget;
import ijfx.ui.context.UiContext;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import mercury.core.AngularMethod;
import net.imagej.ImageJService;
import org.apache.commons.io.IOUtils;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import mongis.utils.ConditionList;
import ijfx.ui.context.UiContextManager;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class UiContextService extends AbstractService implements UiContextManager, ImageJService {

    private HashMap<String, UiContext> uiContextMap = new HashMap();

    private HashMap<String, ContextualWidget> widgets = new HashMap<>();

    final private Set<String> currentContextList = new CopyOnWriteArraySet<>();

    private ContextLinkSet linkSet = new ContextLinkSet();

    private HashMap<String, ContextualView> viewMap = new HashMap<>();

    private static final Logger logger = ImageJFX.getLogger();

    public UiContextManager registerWidget(ContextualWidget widget) {
        widgets.put(widget.getName(), widget);
        importContextConfiguration();
        return this;
    }

    boolean hasChanged = false;

    @Override
    @AngularMethod(sync = true, description = "Enters a context")
    public synchronized UiContextManager enter(String enteredContext) {

        if (enteredContext.contains(" ")) {
            for (String context : enteredContext.split(" ")) {
                enter(context);
            }
            return this;
        }
        logger.info("Tries to enter : " + enteredContext);
        if (currentContextList.contains(enteredContext)) {
            return this;
        }

        logger.info("Entering context " + enteredContext);
        
            // for each context acutally present
            for (String currentContext : currentContextList) {
       //currentContextList.forEach(currentContext -> {

                // if the context is incompatible with the newly entered context
                // the actual context is left to the profit of the new one
                getUIContext(currentContext).getIncompatibles().forEach(concurrent -> {
                    if (enteredContext.equals(concurrent)) {
                        leave(currentContext);
                    }
                });

            };

            //every incompatible context are left
            getUIContext(enteredContext).getIncompatibles().forEach(concurrent -> leave(concurrent));

            logger.info("Entering context " + enteredContext);

            // for each context acutally present
            currentContextList.forEach(currentContext -> {

                // if the context is incompatible with the newly entered context
                // the actual context is left to the profit of the new one
                getUIContext(currentContext).getIncompatibles().forEach(concurrent -> {
                    if (enteredContext.equals(concurrent)) {
                        leave(currentContext);
                    }
                });

            });

            //every incompatible context are left
            getUIContext(enteredContext).getIncompatibles().forEach(concurrent -> leave(concurrent));

            currentContextList.add(enteredContext);
        
        hasChanged = true;

        return this;
    }

    @Override
    public synchronized UiContextManager leave(String context) {

        if (currentContextList.contains(context) == false) {
            return this;
        }

        logger.info("Leaving context " + context);

        currentContextList.remove(context);

        hasChanged = true;
        return this;
    }

    public String getActualContextListAsString() {
        StringBuilder builder = new StringBuilder();
        currentContextList.forEach(str -> builder.append(str).append(" "));
        return builder.toString();
    }

    @Override
    @AngularMethod(sync = true, description = "Update the different widgets depending on the newly created context.")
    public synchronized UiContextManager update() {
        if (!hasChanged) {
            return this;
        }
        logger.info("Updating...");
        logger.info("Actual context : " + getActualContextListAsString());

        //logger.info(linkSet.toString());
        // for each controller, update the controller by telling it which widget it should show
        // and which widget it should hide
        viewMap.values().parallelStream().forEach(controller -> updateController(controller));
        return this;
    }

    public void updateController(ContextualView controller) {

        ArrayList<ContextualWidget> toShow = new ArrayList<>();
        ArrayList<ContextualWidget> toHide = new ArrayList<>();

        controller.getWidgetList().parallelStream().forEach(widget -> {
            boolean shouldShow = shouldShow(widget);
            boolean shouldHide = !shouldShow;

            //logger.info("Should "+widget+" be shown :"+shouldShow);
            if (shouldShow && widget.isHidden()) {
                toShow.add(widget);
                logger.info("Widget " + widget + " added to the toShow list.");
            } else if (shouldHide && !widget.isHidden()) {
                toHide.add(widget);
                logger.info("Widget " + widget + " will be hidden");
            }

        });
        ImageJFX.getThreadQueue().submit(() -> controller.onContextChanged(toShow, toHide));
    }

    public boolean shouldShow(String contextualWidget) {
        return shouldShow(getContextualWidget(contextualWidget));
    }

    public boolean shouldShow(ContextualWidget widget) {

        ConditionList isPresentInAContext = new ConditionList();
        
        ConditionList isNegatedByAContext = new ConditionList();
        
        
        
        
        /*
         //check for each actual context token if the widget is linked to it
         for (String contextName : currentContextList) {

            
         UiContextLink link = new UiContextLink(widget.getName(), contextName, LinkType.VISIBLE);
         logger.info("Does it contains  " + link + " : " + linkSet.contains(link));
            

         //check
         isPresentInAContext.add(linkSet.contains(link));*/

        linkSet.parallelStream().filter(link -> link.getWidgetName().equals(widget.getName())).forEach(link -> {
            isPresentInAContext.add(link.fullFill(currentContextList));
        });
        
        linkSet.parallelStream().filter(link -> link.getWidgetName().equals(widget.getName())).forEach(link -> {
            isNegatedByAContext.add(link.negate(currentContextList));
        });
        
        // the widget should be hidden if one of the link negates it appearance.
        if(isNegatedByAContext.isOneTrue()) return false;

        // if the widget is at least linked to one context, then it should be shown
        return isPresentInAContext.isOneTrue();
    }

    public UiContext getUIContext(String context) {
        if (uiContextMap.containsKey(context) == false) {
            logger.warning("Context doesn't exist : " + context);
            registerContext(context);
        }
        return uiContextMap.get(context);
    }

    public ContextualWidget getWidget(String widget) {
        return widgets.get(widget);
    }

    @Override
    public UiContextManager link(String widget, String context, String linkType) {
        if (context.contains(" ")) {
            for (String subContext : context.split(" ")) {
                link(widget, subContext, linkType);
            }

            return this;
        }
        linkSet.add(new UiContextLink(widget, context, linkType));
        return this;
    }

    @Override
    public UiContextManager registerContext(String context) {
        uiContextMap.put(context, new UiContext(context));
        return this;
    }

    @Override
    public boolean isEnabled(String widget) {
        return getWidget(widget).isEnabled();
    }

    @Override
    public UiContextManager unlink(String widget, String context, String linkType) {
        linkSet.remove(new UiContextLink(widget, context, linkType));
        return this;
    }

    @Override
    public UiContextManager addContextualView(ContextualView contextualView) {

        logger.info(String.format("Registering the contextual view : %s", contextualView.getName()));
        viewMap.put(contextualView.getName(), contextualView);
        contextualView
                .getWidgetList()
                .forEach(widget -> registerWidget(widget));
        linkSet.forEach(link -> link.updateLink(this));
        return this;
    }

    public boolean isDisplayed(String widget) {
        //System.out.println("Is " + widget + " displayed :" + (widgets.get(widget).isHidden() == false));
        return widgets.get(widget).isHidden() == false;
    }

    @Override
    public ContextualWidget getContextualWidget(String widgetName) {
        return widgets.get(widgetName);
    }

    public String exportContextConfiguration() {

        String resultJson = "{'contextList':[]}";

        ObjectMapper mapper = new ObjectMapper();

        try {
            resultJson = mapper.writeValueAsString(uiContextMap.values());
        } catch (JsonProcessingException ex) {
            ImageJFX.getLogger().log(Level.SEVERE,"Error when exporting context configuration.",ex);;
        }

        return resultJson;

    }

    public void importContextConfiguration() {

        try {
            String json = IOUtils.toString(getClass().getResourceAsStream("ContextService.json"));
            importContextConfiguration(json);
        } catch (IOException ex) {
            logger.log(Level.SEVERE,"Failed to load the ContextService.json file",ex);
        }

    }

    public void importContextConfiguration(String json) {
        ObjectMapper mapper = new ObjectMapper();

        List<UiContext> contextList = null;

        try {
            
            contextList = mapper.readValue(json, TypeFactory.defaultInstance().constructCollectionType(List.class, UiContext.class));

        } catch (IOException ex) {
            logger.log(Level.SEVERE,"Error when converting the json file into context configuration.",ex);
        }

        contextList.forEach(context -> {
            uiContextMap.put(context.getId(), context);
        });

    }

    @Override
    public boolean isCurrent(String context) {

        return getCurrentContextAsList().contains(context);

    }

    private Collection<String> getCurrentContextAsList() {
        return currentContextList;
    }

    public class UIContextList {

        @JsonProperty
        private List<UiContext> contextList = new ArrayList<UiContext>();

        public UIContextList() {
        }

        public UIContextList(String string) {

        }

        public UIContextList(Collection<UiContext> contextList) {
            contextList.forEach(ctx -> this.contextList.add(ctx));
            //this.contextList = contextList;
        }

        public List<UiContext> getContextList() {
            return contextList;
        }

        public void setContextList(List<UiContext> contextList) {
            this.contextList = contextList;
        }

    }

    public HashMap<String, UiContext> getUIContextMap() {
        return uiContextMap;
    }
}
