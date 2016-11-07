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
package mongis.utils.transition;

import javafx.beans.binding.Binding;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class OpacityTransitionBinding extends TransitionBinding<Number>{
    
    public OpacityTransitionBinding(Node node,ReadOnlyProperty<Boolean> property) {
        super(0d, 1d);
        
        bind(property, node.opacityProperty());
    }
    
    public OpacityTransitionBinding(Node node, Binding<Boolean> binding) {
        super(0d,1d);
        bind(binding,node.opacityProperty());
    }
    
    
    
    public OpacityTransitionBinding(Node node, ReadOnlyProperty<Boolean> property,double onFalse, double onTrue) {
        super(onFalse,onTrue);
        bind(property,node.opacityProperty());
    }
    
}
