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
package ijfx.ui.plugin;

import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import com.sun.javafx.scene.control.skin.ComboBoxBaseSkin;
import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class LUTComboBoxSkin extends ComboBoxBaseSkin<LUTView> {
    
    LUTView view;
   // private final LUTComboBox outer;

    
    
    public LUTComboBoxSkin(ComboBoxBase<LUTView> c) {
        
        super(c,new ComboBoxBaseBehavior<>(c,new ArrayList<>()));
        
    }
   
    
    public LUTComboBoxSkin(ComboBoxBase<LUTView> comboBox, ComboBoxBaseBehavior<LUTView> behavior) {
        super(comboBox, behavior);
        //this.outer = outer;
        view = comboBox.getValue();
    }

    @Override
    public Node getDisplayNode() {
        if (view == null) {
            return new Label("Nothing for now");
        }
        return view.getImageView();
    }

    @Override
    public void show() {
        if(view != null)
        view.getImageView().setVisible(true);
    }

    @Override
    public void hide() {
        if(view != null)
        view.getImageView().setVisible(false);
    }
    
}
