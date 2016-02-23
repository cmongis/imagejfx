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
package ijfx.ui.project_manager.project.rules;

import ijfx.core.project.AnnotationRule;
import ijfx.core.project.modifier.ModifierPlugin;
import ijfx.core.project.query.Selector;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import static ucar.unidata.util.Format.l;

/**
 *
 * @author cyril
 */
public class EditableRule implements AnnotationRule{

    ObjectProperty<Selector> selector = new SimpleObjectProperty<>();
    ObjectProperty<ModifierPlugin> modifier = new SimpleObjectProperty<>();
    
    BooleanProperty unable = new SimpleBooleanProperty();
    
    
    public EditableRule() {}
    public EditableRule(AnnotationRule rule) {
        selector.setValue(rule.getSelector());
        modifier.setValue(rule.getModifier());
    }
    
    @Override
    public Selector getSelector() {
        return selector.getValue();
    }

    @Override
    public ModifierPlugin getModifier() {
        return modifier.getValue();
    }

    @Override
    public BooleanProperty unableProperty() {
        return unable;
    }

    @Override
    public void setUnable(boolean unable) {
        this.unable.setValue(unable);
    }
    
    public void setSelector(Selector selector) {
        this.selector.setValue(selector);
    }
    
    public void setModifier(ModifierPlugin modifier) {
        this.modifier.setValue(modifier);
    }
    
}
