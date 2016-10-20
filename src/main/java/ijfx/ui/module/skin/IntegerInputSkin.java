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
package ijfx.ui.module.skin;


import ijfx.ui.correction.ChannelSelector;
import ijfx.ui.module.InputSkinPlugin;
import ijfx.ui.module.input.Input;
import javafx.scene.Node;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = InputSkinPlugin.class)
public class IntegerInputSkin extends AbstractNumberInput<Integer> {

    Node node;
    
   ChannelSelector channelSelector;
    
    @Override
    public void init(Input input) {
        
        if(ChannelSelector.STYLE.equals(input.getWidgetType())) {
             channelSelector = new ChannelSelector(null);
            channelSelector.channelNumberProperty().setValue(10);
            valueProperty().bindBidirectional(channelSelector.selectedChannelProperty().asObject());
            //channelSelector.selectedChannelProperty().asObject().bindBidirectional(valueProperty());
            channelSelector.selectChannel((int)input.getValue());
            
            node = channelSelector;
        }
        
        else {
             super.init(input);
            node = super.getNode(); 
        }
        
        
        
    }
    
    @Override
    public Node getNode() {
        
        return node;
        
        
    }
    
    @Override
    Integer convert(String newValue) {
        return Integer.parseInt(newValue);
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return clazz == int.class || clazz == Integer.class;
    }

  

}
