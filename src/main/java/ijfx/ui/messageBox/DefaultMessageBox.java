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

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import javafx.util.Duration;

/**
 *
 * @author Pierre BONNEAU
 */
public class DefaultMessageBox extends HBox implements MessageBox {

    private Property<Message> message;

    private Label label;

    private SequentialTransition openSeqTransition;

    private Timeline openTimeline;
    private final Timeline closeTimeline;

    private final FadeTransition fadeIn;

    private final String GREEN = "#00ff00";
    private final String ORANGE = "#ff6600";
    private final String RED = "#ff0000";

    private final double MAX_HEIGHT = 300.0;
    private final double MIN_HEIGHT = 0.0;
    private final double PADDING = 4.0;

    private final double TIMELINE_DURATION = 300.0;
    private final double FADE_DURATION = 300.0;
    
    private final double LINE_HEIGHT = 22.0;

    public DefaultMessageBox() {
        
        this.setMinHeight(MIN_HEIGHT);
        this.setMaxHeight(MAX_HEIGHT);
        this.setPadding(new Insets(PADDING));

        message = new SimpleObjectProperty<>(new DefaultMessage());


        getMessage().textProperty().addListener(this::playAnimation);
        getMessage().typeProperty().addListener(this::setBgColor);


        label = new Label();
        label.setWrapText(true);
        label.setOpacity(0.0);
        label.setMinHeight(0.0);
        
//        StringBinding sbinding = Bindings.createStringBinding(() -> {
//            return messageProperty().getValue();
//        }, messageProperty());

        label.textProperty().bind(getMessage().textProperty());
        
        
        openTimeline = new Timeline();
        closeTimeline = new Timeline();

        fadeIn = new FadeTransition(Duration.millis(FADE_DURATION), label);
        fadeIn.setToValue(1.0);

        configureCloseTimeline();
        
        this.getChildren().add(label);
    }

    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public Property<Message> messageProperty() {
        return this.message;
    }
    
    @Override
    public Message getMessage(){
        return messageProperty().getValue();
    }

    
    
    public void playAnimation(Observable obs) {
        if (messageProperty().getValue().getText() == null){
            label.setOpacity(0.0);
            closeTimeline.playFromStart();
        }
        else {
            configureOpenTimeline();
            openSeqTransition = new SequentialTransition(openTimeline, fadeIn);
            openSeqTransition.playFromStart();
        }
    }

    public void setBgColor(Observable obs) {

            String bgColor = null;

            switch (messageProperty().getValue().getType()){
                case SUCCESS:
                    bgColor = GREEN; break;
                case WARNING:
                    bgColor = ORANGE; break;
                case DANGER:
                    bgColor = RED; break;
                default:
                    break;
            }

            this.setBackground(new Background(
                    new BackgroundFill(
                            Color.web(bgColor), 
                            new CornerRadii(4.0), 
                            Insets.EMPTY)));

    }

    
    public void configureOpenTimeline() {
        openTimeline.getKeyFrames().clear();
        KeyValue heightValue = new KeyValue(this.prefHeightProperty(), computeTextHeight(), Interpolator.EASE_OUT);
        
        KeyFrame kf = new KeyFrame(Duration.millis(TIMELINE_DURATION), heightValue);

        openTimeline.getKeyFrames().add(kf);
    }

    
    public void configureCloseTimeline() {
        KeyValue kv = new KeyValue(this.prefHeightProperty(), MIN_HEIGHT, Interpolator.EASE_OUT);
        KeyFrame kf = new KeyFrame(Duration.millis(TIMELINE_DURATION), kv);

        closeTimeline.getKeyFrames().add(kf);
    }
    
    
    public double computeTextHeight(){
        
        double computedHeight;
        
        String[] subString = messageProperty().getValue().getText().split("\n");
        int lineCount = 0;
        double subStringLenght;
        int lineToAdd;
        
        for(String s : subString){
            if(s != null && !s.equals("")){
                Text text = new Text(s);
                subStringLenght = text.getLayoutBounds().getWidth();
                lineToAdd = (int)Math.ceil(subStringLenght/this.getBoundsInParent().getWidth());
                lineCount = lineCount + lineToAdd;
            }
            else
                lineCount++;
        }
        
        computedHeight = lineCount * LINE_HEIGHT;
        return computedHeight;
    }
}
