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
import org.scijava.InstantiableException;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.service.AbstractService;

/**
 *
 * @author cyril
 */
public class DefaultActivityService extends AbstractService implements ActivityService {

    Stack<Activity> backStack = new Stack<>();

    Stack<Activity> forwardStack = new Stack<>();

    HashMap<Class<? extends Activity>, Activity> activityMap = new HashMap<>();
    HashMap<String, Activity> activityMapById = new HashMap<>();

    @Parameter
    UiContextService uiContextService;

    @Parameter
    EventService eventService;

    @Parameter
    PluginService pluginService;

    Activity currentActivity;

    Logger logger = ImageJFX.getLogger();

    @Override
    public Collection<Activity> getActivityList() {
        return activityMap.values();
    }

    @Override
    public String getActivityName(Class<? extends Activity> activity) {
        return activity.getName();
    }

    @Override
    public void switchTo(String activityId) {
        
        // checking if an activity with this has already been around
        if (activityMapById.containsKey(activityId) == false) {
            List<PluginInfo<Activity>> pluginsOfType = pluginService.getPluginsOfType(Activity.class);

            for (PluginInfo<Activity> info : pluginsOfType) {
                if (info.getName().equals(activityId)) {
                    switchTo(info.getPluginType());
                    return;
                }
            }

        }
        // otherwise it's created
        else {
            switchTo(activityMapById.get(activityId));
        }
    }

    @Override
    public void switchTo(Class<? extends Activity> activityClass) {

        if (activityMap.containsKey(activityClass) == false) {
            try {
                PluginInfo<SciJavaPlugin> plugin = pluginService.getPlugin(activityClass);
                Activity activity = (Activity) plugin.createInstance();
                activityMap.put(activityClass, activity);
                activityMapById.put(activity.getId(), activity);

            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error when initializing activity", ex);
            }
        }
        switchTo(activityMap.get(activityClass));
    }

    @Override
    public void switchTo(Activity activity) {
        // adding the activity to the back stack
        backStack.add(activity);
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
        switchTo(activity);
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
            uiContextService.leave(getCurrentActivity().getId());
        }
        uiContextService.enter(activity.getId());
        uiContextService.update();
        eventService.publish(new ActivityChangedEvent(activity));
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }
}
