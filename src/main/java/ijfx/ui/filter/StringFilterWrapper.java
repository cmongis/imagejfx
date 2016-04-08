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

import ijfx.core.metadata.MetaDataOwner;
import java.util.function.Predicate;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

/**
 *
 * @author Pierre BONNEAU
 */
public class StringFilterWrapper implements MetaDataOwnerFilter {

    private final StringFilter filter;
    private final Property<Predicate<MetaDataOwner>> metaDataOwnerProperty;

    public StringFilterWrapper(StringFilter filter, String keyName) {

        this.filter = filter;
        metaDataOwnerProperty = new SimpleObjectProperty<>(null);
       if(filter.predicateProperty().getValue() != null) metaDataOwnerProperty.setValue(new StringOwnerPredicate(keyName, filter.predicateProperty().getValue()));

        filter.predicateProperty().addListener(new ChangeListener<Predicate<String>>() {
            @Override
            public void changed(ObservableValue<? extends Predicate<String>> ov, Predicate<String> t, Predicate<String> t1) {
                
                // the predicate should be null if the user did nothing
                if (t1 == null) {
                    metaDataOwnerProperty.setValue(null);
                } else {
                    metaDataOwnerProperty.setValue(new StringOwnerPredicate(keyName, t1));
                }
            }
        });
    }

    @Override
    public Node getContent() {
        return filter.getContent();
    }

    @Override
    public Property<Predicate<MetaDataOwner>> predicateProperty() {
        return this.metaDataOwnerProperty;
    }

    public StringFilter getFilter() {
        return this.filter;
    }

    public void updatePredicate() {

    }

}
