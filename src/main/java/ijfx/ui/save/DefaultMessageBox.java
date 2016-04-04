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
package ijfx.ui.save;

import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 *
 * @author Pierre BONNEAU
 */
public class DefaultMessageBox extends Pane implements MessageBox{
    
    private Property<String> message;
    private Property<MessageType> type;
    
    private Timeline timeline;
    
    private String GREEN = "#00ff00";
    private String ORANGE = "#ff6600";
    private String RED = "#ff0000";
    
    private Double MAX_HEIGHT = 100.0;
    
    public DefaultMessageBox(){
        
        messageProperty().setValue(null);
        typeProperty().setValue(null);
        
        this.maxHeightProperty().setValue(MAX_HEIGHT);
        
        DoubleBinding sizeBinding = Bindings.createDoubleBinding(() -> {
           return setHeight();
        }, messageProperty());
        
        this.prefHeightProperty().bind(sizeBinding);
        
        typeProperty().addListener(this::setBgColor);
        
        
    }
    
    
    @Override
    public Property<String> messageProperty(){
        return this.message;
    }
    
    
    @Override
    public Property<MessageType> typeProperty(){
        return this.type;
    }
    
    
    public double setHeight(){
        double newHeight = 0.0;
        
        return newHeight;
    }
    
    
    public void setBgColor(Observable obs){
        
        String bgColor = null;
        
        if (typeProperty().equals(MessageType.SUCCESS))
            bgColor = GREEN;
        else if (typeProperty().equals(MessageType.WARNING))
            bgColor = ORANGE;
        else if (typeProperty().equals(MessageType.DANGER))
            bgColor = RED;
        
        this.setBackground(new Background(new BackgroundFill(Color.web(bgColor), CornerRadii.EMPTY, Insets.EMPTY)));
    }
}
