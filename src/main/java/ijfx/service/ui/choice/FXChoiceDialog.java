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
package ijfx.service.ui.choice;

import java.util.List;
import java.util.stream.Stream;
import javafx.scene.control.Dialog;

/**
 *
 * @author cyril
 */
public class FXChoiceDialog<T> implements ChoiceDialog<T>{

    
    Dialog<List<? extends T>> dialog;
    
    
    
    
    @Override
    public ChoiceDialog<T> setTitle(String title) {
        dialog.setTitle(title);
        return this;
    }

    @Override
    public ChoiceDialog<T> setMessage(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChoiceDialog<T> addChoice(Choice<T> choice) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChoiceDialog<T> addChoices(List<? extends Choice<T>> choices) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChoiceDialog<T> addChoices(Stream<? extends Choice<T>> choices) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChoiceDialog<T> setEmptyAllowed(boolean emptyAllowed) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChoiceDialog<Integer> addChoice(String title, String description, Integer value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
