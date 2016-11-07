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
package ijfx.ui.explorer;

import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public abstract class AbstractExplorable implements Explorable{
    
    private final BooleanProperty selectedProperty = new SimpleBooleanProperty();

    protected final MetaDataSet metadataSet = new MetaDataSet();
    
    protected static final Logger logger = ImageJFX.getLogger();
   

    @Override
    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }

    @Override
    public MetaDataSet getMetaDataSet() {
        return metadataSet;
    }

    protected File getFile()  {
        return new File(getMetaDataSet().get(MetaData.ABSOLUTE_PATH).getStringValue());
    }
  
    
    
}
