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
package ijfx.ui.activity;

import ijfx.service.uicontext.UiContextService;
import ijfx.ui.main.ImageJFX;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import mercury.core.AngularMethod;
import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class DefaultActivityService extends AbstractService implements ActivityService {

    Stack<Activity> backStack = new Stack<>();

    Stack<Activity> forwardStack = new Stack<>();

    HashMap<String, Activity> activityMap = new HashMap<>();
    HashMap<String, Activity> activityMapById = new HashMap<>();

    @Parameter
    UiContextService uiContextService;

    @Parameter
    EventService eventService;

    @Parameter
    PluginService pluginService;

    @Parameter
    Context context;

    Activity currentActivity;

    Logger logger = ImageJFX.getLogger();

    boolean debug = true;

    @Override
    public Collection<Activity> getActivityList() {
        return activityMap.values();
    }

    @Override
    public String getActivityName(Class<? extends Activity> activity) {
        return activity.getName();
    }

    @Override

    public void openByName(String activityId) {

        open(getActivityByName(activityId));
    }

    @AngularMethod(sync = true, description = "displays an activity")
    public void switchToActivity(String activityId) {
        openByName(activityId);
    }

    @Override
    public void openByType(Class<? extends Activity> activityClass) {

        open(getActivity(activityClass));
    }

    @Override
    public Activity getActivity(Class<? extends Activity> activityClass) {
        if (activityClass == null) {
            logger.severe("Passing NULL as activity parameter !");
            return null;
        }
        if (activityMap.containsKey(activityClass.getName()) == false || debug) {
            try {
                logger.info("Loading activity : " + activityClass.getName());
                PluginInfo<SciJavaPlugin> plugin;

                plugin = pluginService.getPlugin(activityClass.getName());

                logger.info("Activity loaded");
                Activity activity = (Activity) plugin.createInstance();

                context.inject(activity);
                activityMap.put(activity.getClass().getName(), activity);
                activityMapById.put(activity.getActivityId(), activity);

            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error when initializing activity", ex);
            }
        } else {
            System.out.println("it exists !");
        }
        return activityMap.get(activityClass.getName());
    }

    @Override
    public Activity getActivityByName(String activityId) {
        logger.info(activityId);
        // checking if an activity with this has already been around
        if (activityMapById.containsKey(activityId) == false || debug) {
            List<PluginInfo<Activity>> pluginsOfType = pluginService.getPluginsOfType(Activity.class);

            for (PluginInfo<Activity> info : pluginsOfType) {
                logger.info("Scanning : " + info.toString());
                if (info.getName().equals(activityId)) {
                    try {
                        logger.info("Found " + activityId + " = " + info.getClassName());

                        Class<? extends Activity> clazz = (Class<? extends Activity>) Class.forName(info.getClassName());

                        System.out.println(clazz.getName());

                        return getActivity(clazz);
                    } catch (ClassNotFoundException exception) {
                        logger.severe("For some reason, the class couln't found anymore ....");
                        return null;
                    }
                }
            }

        } // otherwise it's created
        else {
            return activityMapById.get(activityId);
        }

        return null;

    }

    @Override
    public void open(Activity activity) {

        // adding the activity to the back stack
        if (activity != null) {
            backStack.add(activity);
        }
        setCurrentActivity(activity);
    }

    @Override
    public void back() {
        Activity activity = backStack.pop();
        forwardStack.add(activity);
        setCurrentActivity(activity);
    }

    @Override
    public void forward() {
        Activity activity = forwardStack.pop();
        open(activity);
    }

    @Override
    public boolean isForwardPossible() {
        return forwardStack.isEmpty() == false;
    }

    @Override
    public boolean isBackPossible() {
        return backStack.isEmpty();
    }

    private void setCurrentActivity(Activity activity) {

        // handling context changing
        if (getCurrentActivity() != null) {
            uiContextService.leave(getCurrentActivity().getActivityId());
        }

        if (activity == null) {
            logger.warning("The Current activity is null");
            return;
        }
        System.out.println("setting the current activity");
        currentActivity = activity;
        uiContextService.enter(activity.getActivityId());
        uiContextService.update();
        eventService.publish(new ActivityChangedEvent(activity));
        logger.info("Event published");
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }
}
