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
package ijfx.ui.correction;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 *
 * @author cyril
 */
public class ChannelSelector extends HBox {

    private final Label titleLabel = new Label();

    private final ComboBox<Channel> channelComboBox = new ComboBox();

    private final IntegerProperty selectedChannel = new SimpleIntegerProperty(-1);

    private final IntegerProperty channelNumber = new SimpleIntegerProperty(0);

    public ChannelSelector(String title) {
        setTitle(title);
        getStyleClass().add("channel-selector");
        channelComboBox.getSelectionModel().selectedItemProperty().addListener(this::onSelectedChannelChanged);
        channelNumber.addListener(this::onChannelNumberChanged);
        
        getChildren().addAll(titleLabel,channelComboBox);
        
    }

    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }
    
    private void onChannelNumberChanged(Observable obs, Number oldValue, Number newValue) {
        updateChoices(newValue.intValue());
    }
    
    private void onSelectedChannelChanged(Observable obs, Channel oldValue, Channel newValue) {
        selectedChannel.setValue(newValue.getId());
    }

    private void updateChoices(Integer choiceNumber) {
        List<Channel> choices = IntStream.range(-1, choiceNumber).mapToObj(Channel::new).collect(Collectors.toList());
        channelComboBox.getItems().clear();
        channelComboBox.getItems().addAll(choices);
    }
    
    public IntegerProperty channelNumberProperty() {
        return channelNumber;
    }
    
    public IntegerProperty selectedChannelProperty() {
        return selectedChannel;
    }

    private class Channel {

        private Integer id;

        public Channel(Integer id) {
            this.id = id;
        }

        @Override
        public String toString() {
            if (id == -1) {
                return "All channels";
            } else {
                return String.format("Channel %d", id);
            }
        }

        public Integer getId() {
            return id;
        }

    }

}
