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
package ijfx.ui.project_manager.projectdisplay.card;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.ui.card.Card;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;

/**
 *
 * @author cyril
 */
public class DismissableCardDecorator<T> implements Card<T>{
    
    private  final Card<T> card;

    private final Property<Boolean> dismissableProperty = new SimpleBooleanProperty(false);
    private final Property<Boolean> dismissedProperty = new SimpleBooleanProperty(false);
    
    public DismissableCardDecorator(Card card) {
        this.card = card;
    }

    @Override
    public Node getContent() {
        return card.getContent();
    }

    @Override
    public Task<Boolean> update(T source) {
        return card.update(source);
    }

    @Override
    public String getName() {
        return card.getName();
    }

    @Override
    public FontAwesomeIcon getIcon() {
        return card.getIcon();
    }

    @Override
    public Property<Boolean> dismissable() {
        return dismissableProperty;
    }

    @Override
    public Property<Boolean> dismissed() {
        return dismissedProperty;
    }
    
    
    
    
    
}
