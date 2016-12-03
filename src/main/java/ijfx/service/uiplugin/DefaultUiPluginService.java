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
package ijfx.service.uiplugin;

import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.ui.UiPlugin;
import ijfx.service.log.DefaultLoggingService;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.LoadingScreen;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import mercury.core.LogEntry;
import mercury.core.LogEntryType;
import mercury.core.MercuryTimer;
import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import ijfx.ui.UiConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import mongis.utils.CallbackTask;
import mongis.utils.CountProperty;
import mongis.utils.ProgressHandler;
import mongis.utils.SilentProgressHandler;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public final class DefaultUiPluginService extends AbstractService implements UiPluginService {

    //ArrayList<FxWidgetPlugin> widgetList;
    @Parameter
    PluginService pluginService;

    HashMap<String, UiPlugin> uiPluginMap;

    HashMap<String, Double> orderMap = new HashMap<>();

    HashMap<String, String> localizationMap = new HashMap<>();

    @Parameter
    Context context;

    @Parameter
    DefaultLoggingService logErrorService;

    @Parameter
    EventService eventService;

    @Parameter
    TimerService timerService;

    @Parameter
    UiContextService contextService;

    Logger logger = ImageJFX.getLogger();

    public static final String MSG_INITALIZE = "[%s] Initializing...";
    public static final String MSG_LOADING_WIDGET = "[%s] Loading Widget";
    public static final String MSG_NO_INFO_SPECIFIED = "[%s] No plugin info specified !";
    public static final String MSG_NO_INFO_DESCRIPTION = "Please use the @WidgetInfo annoation so the interface can handle your widget correctly";
    public static final String MSG_ERROR_WHEN_LAUNCHING = "[%s] Error when initializing.";
    public static final String MSG_NODE_NULL = "[%s] getWidget() returns null !";
    public static final String MSG_LOADING_COUNT = "Loading widget %d / %d";

    public Collection<UiPlugin> loadAll(final ProgressHandler handler) {

        Timer timer = timerService.getTimer(this.getClass());

        if (uiPluginMap == null) {
            uiPluginMap = new HashMap<>();

            // counting
            int count = 0;
            int total = pluginService.getPluginsOfType(UiPlugin.class).size();

            // starting a timer
            handler.setTotal(total);

            timer.start();
            handler.setStatus("Initializing UI...");
            pluginService.getPluginsOfType(UiPlugin.class)
                    .stream()
                    .parallel()
                    .forEach(pi -> {

                        loadWidget(pi);
                        timer.elapsed("Widget loading");
                        // Updating task progress
                        handler.increment(1.0);

                    });

            // for each plugin
            /*
            for (PluginInfo<UiPlugin> pi : pluginService.getPluginsOfType(UiPlugin.class)) {

                count++;
                loadWidget(pi);
                timer.elapsed("Widget loading");
                // Updating task progress
                handler.setProgress((long) count, total);
                handler.setStatus(String.format(MSG_LOADING_COUNT, count, total));

            }*/
            logger.info(String.format("%d UiPlugins created", count));
        }

        timer.logAll();
        return uiPluginMap.values();
    }

    public void loadWidgetList() {
        loadAll(null);
    }

    @Override
    public Collection<UiPlugin> getUiPluginList() {
        if (uiPluginMap == null) {
            loadAll(null);
        }
        return uiPluginMap.values();
    }

    private UiPlugin loadWidget(PluginInfo<UiPlugin> uiPluginInfos) {

        // starting a timer
        MercuryTimer timer = new MercuryTimer("Starting");
        timer.setLogger(logger);
        timer.start();
        //getting the class name for further use
        String widgetClassName = uiPluginInfos.getClassName();

        UiPlugin uiPlugin = null;
        UiConfiguration uiConfiguration = null;
        try {

            String pluginId;

            // some logging
            logger.info(String.format(MSG_INITALIZE, widgetClassName));

            // creating the instance
            uiPlugin = uiPluginInfos.createInstance();
            timer.elapsed(String.format("[%s]Instance creation", widgetClassName));

            // context injection
            context.inject(uiPlugin);
            timer.elapsed(String.format("[%s]Instance injection", widgetClassName));

            // initializing the plugin
            uiPlugin.init();
            timer.elapsed(String.format("[%s]plugin.init()", widgetClassName));

            if (uiPlugin.getUiElement() == null) {
                logger.log(Level.SEVERE, String.format(MSG_NODE_NULL, widgetClassName));
                logErrorService.notifyError(
                        new LogEntry(LogEntryType.ERROR)
                        .setTitle(String.format(MSG_NODE_NULL, widgetClassName))
                );
                return null;
            }

            // getting the plugin infos
            try {
                uiConfiguration = getInfos(uiPlugin);
                pluginId = uiConfiguration.id();
                uiPlugin.getUiElement().setId(pluginId);
                uiPlugin.getUiElement().getStyleClass().add(pluginId);
                setOrder(pluginId, uiConfiguration.order());
                setLocalization(pluginId, uiConfiguration.localization());

            } catch (NullPointerException ne) {

                logger.log(Level.SEVERE, null, ne);

                // in case of error, the user is also notified
                logger.warning(String.format(MSG_NO_INFO_SPECIFIED, widgetClassName));
                logErrorService.notifyError(
                        new LogEntry(LogEntryType.ERROR)
                        .setTitle(String.format(MSG_NO_INFO_SPECIFIED, widgetClassName))
                        .setText(MSG_NO_INFO_DESCRIPTION)
                        .setException(ne)
                );
                return null;
            }

            // adding the widget to the map and list
            uiPluginMap.put(pluginId, uiPlugin);

            localizationMap.put(pluginId, getDefaultLocalization(uiPlugin));
            orderMap.put(pluginId, getDefaultOrder(uiPlugin));

            // the widget has been launch successfully
            logger.info(String.format(MSG_INITALIZE, widgetClassName));
//            uiPlugin.init();
        } catch (Exception ex) {

            logger.log(Level.SEVERE, MSG_ERROR_WHEN_LAUNCHING, ex);
        }

        return uiPlugin;
    }

    @Override
    public UiConfiguration getInfos(UiPlugin plugin) {
        return plugin.getClass().getAnnotation(UiConfiguration.class);
    }

    public Node getWidgetNode(String id) {
        return uiPluginMap.get(id).getUiElement();
    }

    public void reload() {

        uiPluginMap = null;
        getUiPluginList();
    }

    @Override
    public void reload(Class<? extends UiPlugin> clazz) {

        PluginInfo<UiPlugin> pluginInfo = pluginService.getPlugin(clazz, UiPlugin.class);

        UiPlugin plugin = loadWidget(pluginInfo);
        eventService.publish(new UiPluginReloadedEvent(plugin));

    }

    private void setLocalization(String id, String localization) {
        localizationMap.put(id, localization);
    }

    @Override
    public String getDefaultLocalization(UiPlugin uiPlugin) {
        return localizationMap.get(getInfos(uiPlugin).id());
    }

    @Override
    public String getLocalization(String id) {
        return localizationMap.get(id);
    }

    @Override
    public Double getOrder(String id) {
        return orderMap.get(id);
    }

    @Override
    public Double setOrder(String id, Double order) {
        orderMap.put(id, order);
        return order;
    }

    @Override
    public Double getDefaultOrder(UiPlugin uiPlugin) {
        return getInfos(uiPlugin).order();
    }

    @Override
    public List<Node> getSortedList(List<Node> input) {
        ArrayList<Node> sortedNodes = new ArrayList<>(input);
        sort(sortedNodes);
        return sortedNodes;
    }

    @Override
    public void sort(List<Node> pluginList) {
        Collections.sort(pluginList, (n1, n2) -> getOrder(n1.getId()).compareTo(getOrder(n2.getId())));
    }

}
