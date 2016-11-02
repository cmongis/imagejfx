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
package ijfx.ui.datadisplay.image;

import ijfx.service.overlay.OverlaySelectedEvent;
import ijfx.service.overlay.OverlaySelectionEvent;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.imagej.display.OverlayView;
import net.imagej.display.event.AxisPositionEvent;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.display.event.LUTsChangedEvent;
import net.imagej.event.DatasetUpdatedEvent;
import net.imagej.event.OverlayCreatedEvent;
import net.imagej.event.OverlayUpdatedEvent;
import org.apache.commons.lang.ArrayUtils;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.SciJavaEvent;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class SciJavaEventBus {

    PublishSubject<SciJavaEvent> bus = PublishSubject.create();

   

    

    public SciJavaEventBus() {

    }
    


    public <T extends SciJavaEvent> Observable<T> getStream(final Class<T> eventClass) {

        // streamMap.putIfAbsent(eventClass, new Observab);
        return bus
                .filter(obj -> eventClass.isAssignableFrom(obj.getClass()))
                .map(obj -> (T) obj);

    }
    
    public <T extends SciJavaEvent> Observable<List<T>> getBufferedStream(final Class<T> eventClass,long delay) {
        return getStream(eventClass)
                .buffer(delay, TimeUnit.MILLISECONDS)
                .filter(list->list.isEmpty() == false);
    }

    public void channel(SciJavaEvent event) {
        //System.out.println("Channeling ! " + event.getClass().getSimpleName());
        bus.onNext(event);
    }

}
