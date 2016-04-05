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
package ijfx.ui.messageBox;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 *
 * @author Pierre BONNEAU
 */
public class DefaultMessageBox extends Pane implements MessageBox{
    
    private Property<String> message;
    private Property<MessageType> type;
    
    private Timeline openAnimation;
    private Timeline closeAnimation;
    
    private Label label;
    
    private final String GREEN = "#00ff00";
    private final String ORANGE = "#ff6600";
    private final String RED = "#ff0000";
    
    private final double MAX_HEIGHT = 100.0;
    private final double CLOSED_HEIGHT = 0.0;
    
    private final double ANIM_DURATION = 200.0;
    
    public DefaultMessageBox(){
        
        message = new SimpleStringProperty();
        type = new SimpleObjectProperty();
        
        messageProperty().setValue(null);
        typeProperty().setValue(null);
        
        this.maxHeightProperty().setValue(MAX_HEIGHT);
        
        messageProperty().addListener(this::playTimeline);
        typeProperty().addListener(this::setBgColor);
        
        label = new Label();
        label.setWrapText(true);

        
        StringBinding sbinding = Bindings.createStringBinding(() -> {
            return messageProperty().getValue();
        }, messageProperty());
        
        label.textProperty().bind(sbinding);
        
        openAnimation = new Timeline();
        closeAnimation = new Timeline();
        
        configureOpenAnimation();
        configureCloseAnimation();
        
        this.getChildren().add(label);
        
    }
    
    
    @Override
    public Node getContent(){
        return this;
    }
    
    @Override
    public Property<String> messageProperty(){
        return this.message;
    }
    
    
    @Override
    public Property<MessageType> typeProperty(){
        return this.type;
    }
    

    public void playTimeline(Observable obs){
        if(messageProperty().getValue() == null)
            closeAnimation.playFromStart();
        else
            openAnimation.playFromStart();
    }
    
    
    public void setBgColor(Observable obs){
        
        String bgColor = null;
        
        if (typeProperty().getValue().equals(MessageType.SUCCESS))
            bgColor = GREEN;
        else if (typeProperty().getValue().equals(MessageType.WARNING))
            bgColor = ORANGE;
        else if (typeProperty().getValue().equals(MessageType.DANGER))
            bgColor = RED;
        
        this.setBackground(new Background(new BackgroundFill(Color.web(bgColor), CornerRadii.EMPTY, Insets.EMPTY)));
    }
    
    
    public double setBoxHeight(){
        double newHeight = 80.0;
        return newHeight;
    }
    
    
    public void configureOpenAnimation(){
        KeyValue kv = new KeyValue(this.prefHeightProperty(), setBoxHeight());
        KeyFrame kf = new KeyFrame(Duration.millis(ANIM_DURATION), kv);
        
        openAnimation.getKeyFrames().add(kf);     
    }
    
    
    public void configureCloseAnimation(){
        KeyValue kv = new KeyValue(this.prefHeightProperty(), CLOSED_HEIGHT);
        KeyFrame kf = new KeyFrame(Duration.millis(ANIM_DURATION), kv);
        
        closeAnimation.getKeyFrames().add(kf);
    }
}
