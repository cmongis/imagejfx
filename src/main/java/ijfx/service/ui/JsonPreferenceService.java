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
package ijfx.service.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import net.imagej.ImageJService;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class,priority = Priority.VERY_LOW_PRIORITY)
public class JsonPreferenceService extends AbstractService implements ImageJService{
    
    
    private final File configDirectory = ImageJFX.getConfigDirectory();  
    
    private ObjectMapper mapper = new ObjectMapper();
    
    
    
    public <T> void savePreference(T toSaveAsJson, String filename) {
        try {
            
            
            
            
            mapper.writeValue(new File(configDirectory,addExtension(filename)), toSaveAsJson);
        } catch (IOException ex) {
             ImageJFX.getLogger().log(Level.SEVERE,"Error when saving the configuration file"+filename,ex);
        }
    }
    
    public <K extends Object> K loadFromJson(String filename, K defaultPreference) {
        
        try {
            return mapper.readValue(new File(configDirectory,addExtension(filename)), (Class<K>) defaultPreference.getClass());
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE,"Error when reading the configuration file"+filename,ex);
        }
        
        return (K) defaultPreference;
        
    }
    
    public String addExtension(String filename) {
        if(filename.endsWith(".json") == false) {
            return filename + ".json";
        }
        return filename;
    }
    
    public <K extends Object> List<K> loadListFromJson(String filename, Class<K> clazz) {
        
        try {
            return mapper.readValue(new File(configDirectory,addExtension(filename)),mapper.getTypeFactory().constructCollectionType(List.class,clazz));
        }
        catch(Exception e) {
             ImageJFX.getLogger().log(Level.SEVERE,"Error when reading the configuration file"+filename,e);
            return new ArrayList<K>();
        }
        
        
    }
    
    public <K extends Object> Set<K> loadSetFromJson(String filename, Class<K> clazz) {
        
         try {
            return mapper.readValue(new File(configDirectory,addExtension(filename)),mapper.getTypeFactory().constructCollectionType(Set.class,clazz));
        }
        catch(Exception e) {
             ImageJFX.getLogger().log(Level.SEVERE,"Error when reading the configuration file"+filename,e);
            return new HashSet<K>();
        }
        
    }
    
    
    
    
}
