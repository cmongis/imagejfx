package ijfx.service;


import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.event.EventSubscriber;
import org.scijava.event.SciJavaEvent;
import org.scijava.service.AbstractService;
import org.scijava.util.ClassUtils;
import rx.subjects.PublishSubject;

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
/**
 *
 * @author Cyril MONGIS, 2016
 */

public class RxEventService extends AbstractService implements EventService {

    HashMap<Class, List<Handler>> handlerMap = new HashMap<>();

    PublishSubject<SciJavaEvent> eventBus = PublishSubject.create();

    
    public RxEventService() {
        
        
        
        
    }
    
    
    @Override
    public <E extends SciJavaEvent> void publish(E e) {
        eventBus.onNext(e);
    }

    @Override
    public <E extends SciJavaEvent> void publishLater(E e) {
    }

    @Override
    public List<EventSubscriber<?>> subscribe(Object o) {

        ClassUtils
                .getAnnotatedMethods(o.getClass(), EventHandler.class)
                .stream()
                .map(method -> {
                    return new Handler(o, method);
                })
                .filter(handler -> handler.isValid())
                .map(this::addHandler);
        
        return null;
    }

    @Override
    public void unsubscribe(Collection<EventSubscriber<?>> subscribers) {
        
    }

    @Override
    public <E extends SciJavaEvent> List<EventSubscriber<E>> getSubscribers(Class<E> c) {
        return handlerMap.get(c).stream().map(h->(EventSubscriber<E>)h).collect(Collectors.toList());
    }
    
    
   
    
    public class Handler<T extends SciJavaEvent> implements EventSubscriber<T>{

        WeakReference<Object> object;
        Method method;

        Class<T> eventClass;

        boolean valid = false;

        public Handler(Object o, Method m) {

            final Class<?>[] c = m.getParameterTypes();
            if (c == null || c.length != 1) {
                return; // wrong number of args
            }
            if (!SciJavaEvent.class.isAssignableFrom(c[0])) {
                return; // wrong class
            }
            // Cache the eventClass
            eventClass = (Class<T>) c[0];

            valid = true;
        }

        public void call(SciJavaEvent event) {

           

        }

        @Override
        public int hashCode() {
            return object.hashCode() + method.hashCode() + eventClass.hashCode();
        }

        public boolean isValid() {
            return valid;
        }

        public Class<T> getEventClass() {
            return eventClass;
        }

        public Method getMethod() {
            return method;
        }

        public Object getObject() {
            return object;
        }

        @Override
        public void onEvent(T event) {
             try {
                method.invoke(object, event);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(RxEventService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    
    
    protected List<Handler> addHandler(Handler handler){

        List<Handler> list = handlerMap
                .computeIfAbsent(handler.getEventClass(), h -> new ArrayList<>());
        if (list.contains(handler) == false) {
            list.add(handler);
        }

        return list;

    }
    
    protected void execute(EventSubscriber e) {
    }

}
