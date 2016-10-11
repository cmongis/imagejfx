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

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    private final BooleanProperty allowAllChannels = new SimpleBooleanProperty(true);
    
    private Property<String[]> choicesProperty = new SimpleObjectProperty();
    
    public ChannelSelector(String title) {
        setTitle(title);
        getStyleClass().add("channel-selector");
        channelComboBox.getSelectionModel().selectedItemProperty().addListener(this::onSelectedChannelChanged);
       
        channelComboBox.getStyleClass().add("normal");
       
        
        
        
        
        if(title != null) {
            getChildren().add(titleLabel);
        }
        
        getChildren().add(channelComboBox);
        
        channelComboBox.itemsProperty().bind(
                Bindings.createObjectBinding(this::getChoices,channelNumber,allowAllChannels)
        );
        
        channelComboBox.itemsProperty().addListener(this::onChoicesChanged);
        
        
    }

    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }
    
 
    
    private void onSelectedChannelChanged(Observable obs, Channel oldValue, Channel newValue) {
        if(newValue != null)selectedChannel.setValue(newValue.getId());
    }

    public void onChoicesChanged(Observable obs, Object oldalue, Object newValue) {
        channelComboBox.getSelectionModel().select(0);
    }
   
    
    public int getChannelNumber() {
        return channelNumber.getValue();
    }
    
    private ObservableList<Channel> getChoices() {
       
        ObservableList<Channel> choices = FXCollections.observableArrayList();
       
        int choiceNumber = getChannelNumber();
        
       
        
        choices.addAll(IntStream.range(-1, choiceNumber).mapToObj(Channel::new).collect(Collectors.toList()));
        
       return choices;
        
    }
    
    
    
    
    public IntegerProperty channelNumberProperty() {
        return channelNumber;
    }
    
    public IntegerProperty selectedChannelProperty() {
        return selectedChannel;
    }

    private boolean allowAllChannels() {
        return allowAllChannels.getValue();
    }

    void setAllowAllChannels(boolean b) {
        allowAllChannels.setValue(b);
    }

    int getSelectedChannel() {
        return selectedChannel.getValue();
    }
    
    
    
    
    private class Channel {

        private final Integer id;

        public Channel(Integer id) {
            this.id = id;
        }

        @Override
        public String toString() {
            if (id == -1) {
                
                if(allowAllChannels()) {
                
                     return "All channels";
                }
                else {
                    return "Select a channel";
                }
            } else {
                return String.format("Channel %d", id+1);
            }
        }

        public Integer getId() {
            return id;
        }

    }

}
