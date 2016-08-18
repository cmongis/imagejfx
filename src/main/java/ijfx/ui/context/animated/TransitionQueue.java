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
package ijfx.ui.context.animated;

import java.util.LinkedList;
import java.util.Queue;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.event.EventHandler;

/**
 *
 */
public class TransitionQueue {
    //ArrayList<Transition> queue = new ArrayList<>();

    Queue<Transition> queue = new LinkedList<>();

    /**
     *
     * @param transition
     */
    public void queue(Transition transition) {

        final EventHandler handler = transition.getOnFinished();
        transition.setOnFinished(event -> {
            if (handler != null) {
                try {
                    
                handler.handle(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            playNext();

        });

        queue.add(transition);
        playNext();

        /*
        final EventHandler handler = transition.getOnFinished();
        transition.setOnFinished(event->{
            if(handler !=null)
            handler.handle(event); 
            playNext();
             
        });
        
        queue.add(transition);
        if(queue.size() == 1) playNext();*/
    }

    public void pop() {
        queue.remove(0);
    }

    /**
     *
     */
    private void playNext() {
        if (queue.size() > 0) {
            final Transition toPlay = queue.poll();
            Platform.runLater(() -> {
                toPlay.play();
            });
            //pop();
        }

    }

    public void emptyQueue() {
        queue.clear();
    }

    public void remove(Transition tr) {
        queue.remove(tr);
    }

    public boolean contains(Transition tr) {
        return queue.contains(tr);
    }

    public int size() {
        return queue.size();
    }

}
