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
import ijfx.core.metadata.MetaDataJsonModule;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    private ObjectMapper mapper;
    
    private final static Logger logger = ImageJFX.getLogger();
    
   public ObjectMapper getMapper() {
        if(mapper == null) {
            mapper = new ObjectMapper();
            
            mapper.registerModule(new MetaDataJsonModule());
            
        }
        return mapper;
    }
    
    public <T> void savePreference(T toSaveAsJson, String filename) {
        try {
            
            
            
            
            getMapper().writeValue(new File(configDirectory,addExtension(filename)), toSaveAsJson);
        } catch (IOException ex) {
             ImageJFX.getLogger().log(Level.SEVERE,"Error when saving the configuration file"+filename,ex);
        }
    }
    
    public <K extends Object> K loadFromJson(String filename, K defaultPreference) {
        
        try {
            return getMapper().readValue(new File(configDirectory,addExtension(filename)), (Class<K>) defaultPreference.getClass());
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
            
            File finput = new File(configDirectory,addExtension(filename));
            if(finput.exists() == false) {
                logger.warning("No such file as "+filename);
                return new ArrayList<K>();
                
            }
            
            return getMapper().readValue(finput,getMapper().getTypeFactory().constructCollectionType(List.class,clazz));
        }
        
        catch(Exception e) {
             logger.log(Level.SEVERE,"Error when reading the configuration file"+filename,e);
            return new ArrayList<K>();
        }
        
        
    }
    
    public <K extends Object> Set<K> loadSetFromJson(String filename, Class<K> clazz) {
        
         try {
            return getMapper().readValue(new File(configDirectory,addExtension(filename)),getMapper().getTypeFactory().constructCollectionType(Set.class,clazz));
        }
        catch(Exception e) {
             ImageJFX.getLogger().log(Level.SEVERE,"Error when reading the configuration file"+filename,e);
            return new HashSet<K>();
        }
        
    }
    
    public <K,O> Map<K,O> loadMapFromJson(String filename, Class< ? extends K> keyType, Class<? extends O> valueType) {
        try {
            return getMapper().readValue(new File(configDirectory,addExtension(filename)), getMapper().getTypeFactory().constructMapLikeType(Map.class, keyType, valueType));
        }
        catch(Exception e) {
            ImageJFX.getLogger().log(Level.SEVERE,"Error when loading the configuration file"+filename,e);
            return new HashMap<>();
        }
    }
    
    
    
    
}
