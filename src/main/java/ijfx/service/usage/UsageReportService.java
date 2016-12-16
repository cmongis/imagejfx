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

import ijfx.service.IjfxService;
import ijfx.service.notification.DefaultNotificationService;
import org.scijava.plugin.Parameter;
import org.scijava.service.AbstractService;

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
public class UsageReportService extends AbstractService implements IjfxService{
    
    
    
    
    
    @Parameter
    DefaultNotificationService notificationService;
    
    
    
    
    
    
    
}
