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
package ijfx.core.listenableSystem;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ListeningSupportImpl implements ListeningSupport {
    List<Listening> stopListenList = new ArrayList<>();
    HashMap<Listenable,List<Listening>> listenableMap = new HashMap<>();
    @Override
    public void listenTo(Listenable listenable, PropertyChangeListener listener) {
        addListenableKey(listenable);
        
        Listening listening =  new Listening() {

            public void stopListening() {
                listenable.removePropertyChangeListener(listener);
            }
        };
        stopListenList.add(listening);
        listenableMap.get(listenable).add(listening);
         listenable.addPropertyChangeListener(listener);
    }

    @Override
    public void listenTo(Listenable listenable, String propertyName, PropertyChangeListener listener) {
        addListenableKey(listenable);
        Listening listening = new Listening() {

            @Override
            public void stopListening() {
                listenable.removePropertyChangeListener(propertyName, listener);
            }
        };
        stopListenList.add(listening);
        listenableMap.get(listenable).add(listening);
        listenable.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void stopListening() {
        for (Listening listening: stopListenList) {
            listening.stopListening();
        }
        stopListenList.clear();
        listenableMap.clear();
    }

    @Override
    public void stopListening(Listenable listenable) {
        if (listenableMap.containsKey(listenable)) {
            for (Listening listening: listenableMap.get(listenable)) {
                listening.stopListening();
            }
            listenableMap.remove(listenable);
        }
    }
    private void addListenableKey(Listenable listenable) {
        if (!listenableMap.containsKey(listenable)) {
            listenableMap.put(listenable, new ArrayList<>());
        }
    }
    
}

