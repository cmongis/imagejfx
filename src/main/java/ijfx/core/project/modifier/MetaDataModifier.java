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
package ijfx.core.project.modifier;

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.project.command.AddMetaDataCommand;
import ijfx.core.project.command.Command;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.main.ImageJFX;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = ModifierPlugin.class)
public class MetaDataModifier implements ModifierPlugin{

    
    String keyName;
    String value;
    
    public static final Pattern PARSER =  Pattern.compile("add metadata:(.*)=(.*)");
    public static final String PHRASE = "Adds the metadata %s = %s";
    
    @Override
    public boolean configure(String query) {
        
        Matcher m = PARSER.matcher(query);
        
        if(m.matches()) {
            keyName = m.group(1).trim();
            value = m.group(2).trim();
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public Command getModifyingCommand(PlaneDB planeDB) {
        if(keyName == null) {
            ImageJFX.getLogger().warning("No key name for the Modifier");
            return null;
        }
        return new AddMetaDataCommand(planeDB, new GenericMetaData(keyName, value));
        
    }

    @Override
    public boolean wasApplied(PlaneDB planeDB) {
        if(keyName == null) return false;
        return planeDB.metaDataSetProperty().get(keyName).getStringValue().equals(value);
    }

    @Override
    public String phraseMe() {
        return String.format(PHRASE,keyName,value);
    }

    public String getKeyName() {
        return keyName;
    }

    public String getValue() {
        return value;
    }

    public MetaDataModifier setKeyName(String keyName) {
        this.keyName = keyName;
        return this;
    }

    public MetaDataModifier setValue(String value) {
        this.value = value;
        return this;
    }
    
    
    
    
}
