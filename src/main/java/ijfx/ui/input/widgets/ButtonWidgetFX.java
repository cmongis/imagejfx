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
package ijfx.ui.input.widgets;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import org.scijava.plugin.Plugin;
import org.scijava.widget.AbstractInputWidget;
import org.scijava.widget.ButtonWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;


/**
 *
 * @author cyril
 */
@Plugin(type = InputWidget.class)
public class ButtonWidgetFX extends AbstractInputWidget<org.scijava.widget.Button,Node> implements ButtonWidget<Node>{
 
    Button button = new Button();

    public ButtonWidgetFX() {
        super();
        
        button.setOnAction(this::onClick);
    }

    
    
    
    
    @Override
    public void set(WidgetModel model) {
        super.set(model);
        
        button.setText(model.getWidgetLabel());
        
        
        
    }
    
    
    private void onClick(ActionEvent event) {
        get().callback();
        
        get().getPanel().refresh();
        
    }
    
    @Override
    public org.scijava.widget.Button getValue() {
        return (org.scijava.widget.Button) get().getValue();
    }

    @Override
    public void refreshWidget() {
       button.setText(get().getWidgetLabel());
    }

    @Override
    public Node getComponent() {
       return button;
    }

    @Override
    public Class<Node> getComponentType() {
      return Node.class;
    }

    @Override
    public boolean supports(WidgetModel model) {
        return super.supports(model) && model.isType(Button.class);
    }
    
    
  
    
    
}
