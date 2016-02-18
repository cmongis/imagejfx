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
package ijfx.core.project;

import ijfx.core.project.modifier.ModifierPlugin;
import ijfx.core.project.query.Selector;
import ijfx.core.project.query.Modifier;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;


public class AnnotationRuleImpl implements AnnotationRule {
    private final Selector selector;
    private final ModifierPlugin modifier;
    private final BooleanProperty unableProperty;
    public AnnotationRuleImpl(Selector selector, ModifierPlugin modifier) {
        this.selector = selector;
        this.modifier = modifier;
        unableProperty = new SimpleBooleanProperty(true);
    }
    @Override
    public Selector getSelector() {
        return selector;
    }

    @Override
    public ModifierPlugin getModifier() {
        return modifier;
    }

    @Override
    public BooleanProperty unableProperty() {
        return unableProperty;
    }

    @Override
    public void setUnable(boolean unable) {
        unableProperty.set(unable);
    }
    
}
