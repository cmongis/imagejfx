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
package ijfx.ui.filter;

import java.util.Collection;
import java.util.function.Predicate;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.scene.Node;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public interface NumberFilter {
    
    public Node getContent();
    
    public DoubleProperty maxProperty();
    public DoubleProperty minProperty();
    
    public void setAllPossibleValue(Collection<? extends Number> values);
    
    public Property<Predicate<Double>> predicateProperty();
    
}
