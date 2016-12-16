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
package ijfx.service.notification;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class DefaultNotification implements Notification{
    private String title;
    private String text;
    
    private List<NotificationAction> actionList = new ArrayList<>();

    
    
    
    public DefaultNotification() {
        
        
    }

    public DefaultNotification(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public DefaultNotification setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getText() {
        return text;
    }

    public DefaultNotification setText(String text) {
        this.text = text;
        return this;
    }

    public List<NotificationAction> getActionList() {
        return actionList;
    }

    public DefaultNotification addAction(final String title, Runnable runnable) {
        actionList.add(new NotificationAction() {

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public void run() {
                runnable.run();
            }
        });
        return this;
    }

    @Override
    public BufferedImage getIcon() {
        return null;
    }

    @Override
    public List<NotificationAction> getActions() {
        return getActionList();
    }
    
   
    
    
    
}
