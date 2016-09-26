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
package ijfx.ui.context;

import ijfx.service.uicontext.UiContextService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class UiContextProperty extends ReadOnlyBooleanPropertyBase{

    Object bean;
    
    final String name;
    
    @Parameter
    UiContextService contextService;
    
    boolean isInCurrentContext = false;
    
    public UiContextProperty(Context context, String name) {
        this.name = name;
        context.inject(this);
        
        isInCurrentContext = contextService.isCurrent(name);
        Platform.runLater(this::fireValueChangedEvent);
    }

    
    
    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean get() {
        return isInCurrentContext;
    }
    
    @EventHandler
    public void onContextUpdated(UiContextUpdatedEvent event)  {
        isInCurrentContext = event.getObject().contains(name);
        Platform.runLater(this::fireValueChangedEvent);
    }
    
}
