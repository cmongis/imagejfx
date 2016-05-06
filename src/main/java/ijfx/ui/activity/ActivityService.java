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

import ijfx.service.IjfxService;
import ijfx.ui.plugin.WebAppContainer;
import java.util.Collection;

/**
 *
 * @author cyril
 */
public interface ActivityService extends IjfxService{
    
    public Collection<Activity> getActivityList();
    
    public String getActivityName(Class<? extends Activity> activity);
    
    public Activity getActivityByName(String id);
    public Activity getActivity(Class<? extends Activity> activityClass);
    
    public void openByName(String string); 
    
    public void openByType(Class<? extends Activity> activity);
    
    public void open(Activity activity);
    
    public void back();
    
    public void forward();
    
    public boolean isForwardPossible();
    
    public boolean isBackPossible();

    public Activity getCurrentActivity();
    
    public String getCurrentActivityId();
    
    public Class<?> getCurrentActivityAsClass();
    
    
}
