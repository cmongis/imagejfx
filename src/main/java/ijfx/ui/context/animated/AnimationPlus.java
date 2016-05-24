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
package ijfx.ui.context.animated;

import ijfx.ui.main.ImageJFX;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author cyril
 */

@FunctionalInterface
public interface AnimationPlus {
    public javafx.animation.Animation configure(Node node, Duration duration);
    
    
    
    
    public static double FADE_LEFT_TRANSLATE = -100;
    
    public static AnimationPlus FADE_OUT_LEFT = (node,ms)->{
    
        if(ms == null) ms = ImageJFX.getAnimationDuration();
        
        KeyFrame begin = new KeyFrame(Duration.millis(0),
                new KeyValue(node.translateXProperty(),0)
                ,new KeyValue(node.opacityProperty(),1)
        );
        
        
        
        KeyFrame end = new KeyFrame(ms,
                new KeyValue(node.translateXProperty(),FADE_LEFT_TRANSLATE)
                ,new KeyValue(node.opacityProperty(),0)
        );
        
        
        Timeline timeLine = new Timeline(begin,end);
        return timeLine;
    };
    
    public static AnimationPlus FADE_IN_FROM_LEFT = (node,ms)->{
        
        if(ms == null) ms = ImageJFX.getAnimationDuration();
        
         KeyFrame begin = new KeyFrame(Duration.millis(0),
                new KeyValue(node.translateXProperty(),FADE_LEFT_TRANSLATE)
                ,new KeyValue(node.opacityProperty(),0)
        );
        KeyFrame end = new KeyFrame(ms,
                new KeyValue(node.translateXProperty(),0)
                ,new KeyValue(node.opacityProperty(),1)
        );
        
        
        Timeline timeLine = new Timeline(begin,end);
        return timeLine;
    };
    
    
    public static <T> Animation configurePropertyTransition(Property<T> property, T value, Duration duration) {
        KeyFrame keyFrame = new KeyFrame(duration, new KeyValue(property,value));
        return new Timeline(keyFrame);
    }
    
   
    
}