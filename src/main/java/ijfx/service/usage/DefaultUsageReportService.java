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
package ijfx.service.usage;

import ijfx.service.notification.DefaultNotificationService;
import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import rx.subjects.PublishSubject;

/**
 *  Service used to report usage report to the server
 * 
 *  @Parameter
 *  UsageReportService usageService;
 * 
 *  usageService
 *      .createUsage(UsageType.CLICK,"Activity",UsageLocation.SIDE_PANEL)
 *      .send();
 *  
 *  usageService.
 *      .createUsage(UsageType.SWITCH,"Threshold min/max",UsageLocation.LUT_PANEL)
 *      .setValue(true)
 *      .send();
 *  usageService.
 *      createUsage(UsageType.SET,"filter",UsageLocation.EXPLORER)
 *      .setValue("well")
 *      .send();
 * 
 * @author cyril
 */
@Plugin(type = Service.class)
public class DefaultUsageReportService extends AbstractService implements UsageReportService{
    
    
    
    
    
    @Parameter
    private DefaultNotificationService notificationService;
    
    private int idCount = 0;
    
    private final UUID sessionId = UUID.randomUUID();
    
    @Parameter
    PrefService prefService;
    
   
    private final PublishSubject<JSONObject> sendQueue = PublishSubject.create();
    
    private final List<JSONObject> notSent = new ArrayList<>();
    
    private static final String ACCEPTED = "IJFX_USAGE_REPORT_ACCEPTED";
    private static final String DECIDED = "IJFX_USAGE_REPORT_DECIDED";
   
    
    Logger logger = ImageJFX.getLogger();
    
    @Override
    public void initialize() {
        
        
        
        
                
        
        sendQueue
                
                .buffer(2, TimeUnit.SECONDS)
                .filter(list->list.isEmpty() == false)
                .subscribe(this::handleUsageReports);
        
        
        if(hasDecided() && hasAccepted() == false) {
            sendQueue.onCompleted();
        }
        
        createUsageLog(UsageType.SET, "CPU", UsageLocation.INFO)
                .setValue(Runtime.getRuntime().availableProcessors())
                .send();
        
        createUsageLog(UsageType.SET,"RAM",UsageLocation.INFO)
                .setValue(Runtime.getRuntime().totalMemory() / 1000 / 1000)
                .send();
        
    }
    
    
    @Override
    public UsageLog createUsageLog(UsageType type, String name, UsageLocation location) {
        return new MyUsageLog(type, name, location);
    }

    @Override
    public boolean hasDecided() {
        return prefService.getBoolean(DECIDED, false);
    }
    
    private boolean hasAccepted() {
        return prefService.getBoolean(ACCEPTED, false);
    }
    
   

    @Override
    public void setDecision(Boolean accept) {
        prefService.put(DECIDED,true);
        prefService.put(ACCEPTED, accept.booleanValue());
        
        if(accept == false) {
            sendQueue.onCompleted();
        }
        
    }
    
    private class MyUsageLog extends AbstractUsageLog {
    
        public MyUsageLog(UsageType type, String name, UsageLocation location) {
            super(type, name, location);
        }
    
        @Override
        public UsageLog send() {
            if(!hasDecided() || hasAccepted()) {
                sendQueue.onNext(toJSON());
            }
            
            return this;
        }
        
        @Override
        public JSONObject toJSON() {
           return super
                   .toJSON()
                   .put("session_id", sessionId.toString())
                   .put("position",idCount++);
        }
        
    }   
    
    public void handleUsageReports(List<JSONObject> objects) {
        
        
        // if the user decided to not send
        if(hasDecided() && hasAccepted() == false) return;
        
        
        notSent.addAll(objects);
       
        logger.info(String.format("Sending %d usage logs",notSent.size()));
        
        if(hasDecided() == false) {
            return;
        }
        Iterator<JSONObject> iterator = notSent.iterator();
        
        // emptying the list of object to send
        while(iterator.hasNext()) {
            
            JSONObject object = iterator.next();
            
            // if the socket is not available, we abort
            if(notificationService.getSocket() == null || notificationService.getSocket().connected() == false) {
                return;
            }
            // if not, we send the usage and remove it from the send queue
            notificationService.getSocket().emit("usage",object);
            
            iterator.remove();
            
        }
       
    }
    
    
  
    
}
