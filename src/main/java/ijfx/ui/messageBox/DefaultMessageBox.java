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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 *
 * @author Pierre BONNEAU
 */
public class DefaultMessageBox extends Pane implements MessageBox {

    private Property<String> message;
    private Property<MessageType> type;

    private Label label;

    private SequentialTransition openSeqTransition;

    private Timeline openTimeline;
    private final Timeline closeTimeline;

    private final FadeTransition fadeIn;

    private final String GREEN = "#00ff00";
    private final String ORANGE = "#ff6600";
    private final String RED = "#ff0000";

    private final double MAX_HEIGHT = 100.0;
    private final double MAX_WIDHT = 260.0;
    private final double CLOSED_HEIGHT = 0.0;

    private final double TIMELINE_DURATION = 250.0;
    private final double FADE_DURATION = 200.0;

    public DefaultMessageBox() {

        message = new SimpleStringProperty();
        type = new SimpleObjectProperty();

        messageProperty().setValue(null);
        typeProperty().setValue(null);

        messageProperty().addListener(this::playTimeline);
        typeProperty().addListener(this::setBgColor);

        this.maxHeightProperty().setValue(MAX_HEIGHT);
        this.maxWidthProperty().setValue(MAX_WIDHT);

        label = new Label();
        label.setMaxWidth(MAX_WIDHT);
        label.setMaxHeight(MAX_HEIGHT);
        label.setWrapText(true);
        label.setOpacity(0.0);
        
        StringBinding sbinding = Bindings.createStringBinding(() -> {
            return messageProperty().getValue();
        }, messageProperty());

        label.textProperty().bind(sbinding);
        

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
    public Property<String> messageProperty() {
        return this.message;
    }

    @Override
    public Property<MessageType> typeProperty() {
        return this.type;
    }
    
    
    public void playTimeline(Observable obs) {
        if (messageProperty().getValue() == null)
            closeTimeline.playFromStart();
        else {
            configureOpenTimeline();
            openSeqTransition = new SequentialTransition(openTimeline, fadeIn);
            openSeqTransition.playFromStart();
        }
    }

    public void setBgColor(Observable obs) {

        String bgColor = null;

        switch (typeProperty().getValue()) {
            case SUCCESS:
                bgColor = GREEN; break;
            case WARNING:
                bgColor = ORANGE; break;
            case DANGER:
                bgColor = RED; break;
            default:
                break;
        }

        this.setBackground(new Background(new BackgroundFill(Color.web(bgColor), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public double setBoxHeight() {
        double newHeight = 50.0;
        return newHeight;
    }

    public void configureOpenTimeline() {
        openTimeline.getKeyFrames().clear();
        KeyValue kv = new KeyValue(this.prefHeightProperty(), setBoxHeight());
        KeyFrame kf = new KeyFrame(Duration.millis(TIMELINE_DURATION), kv);

        openTimeline.getKeyFrames().add(kf);
    }

    public void configureCloseTimeline() {
        KeyValue kv = new KeyValue(this.prefHeightProperty(), CLOSED_HEIGHT);
        KeyFrame kf = new KeyFrame(Duration.millis(TIMELINE_DURATION), kv);

        closeTimeline.getKeyFrames().add(kf);
    }
}
