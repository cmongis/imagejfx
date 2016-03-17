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
package ijfx.service.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.service.ui.hint.DefaultHint;
import ijfx.service.ui.hint.Hint;
import ijfx.service.ui.hint.HintRequestEvent;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import mongis.utils.TextFileUtils;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class DefaultHintService extends AbstractService implements HintService {

    Set<String> history;

    @Parameter
    JsonPreferenceService prefService;

    @Parameter
    EventService eventService;

    private static final String CONFIGURATION_FILE = "hints.json";

    private final Logger logger = ImageJFX.getLogger();
    
    
    @Override
    public void displayHint(Hint hint, boolean force) {

    }

    @Override
    public void displayHints(List<? extends Hint> hintList, boolean force) {

        
        
        if(force) {
            eventService.publishLater(new HintRequestEvent(hintList));
        }
        
        
        else {
            List<Hint> nonReadHint = new ArrayList<>();
            
            hintList.forEach(hint->{
                if(wasRead(hint) == false) {
                    nonReadHint.add(new HintDecorator(hint));
                }
            });
            
            
            eventService.publishLater((new HintRequestEvent(nonReadHint)));
        }
        
    }
    
    

    @Override
    public void initialize() {
        loadConfiguration();
    }

    @Override
    public void displayHints(String url, boolean force) {
        try {
            List<DefaultHint> hintList = jsonToHintList(TextFileUtils.readFileFromJar(url));
            displayHints(hintList, force);
        } catch (IOException ex) {
            Logger.getLogger(DefaultHintService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void resetConfiguration() {
    }

    @Override
    public void displayHints(Class clazz, boolean force) {
        logger.info("Trying to hint widget "+clazz.getSimpleName());
        try {
            String url = clazz.getSimpleName()+"Hints.json";
            
            if(clazz.getResource(url) == null) return;
            
            List<DefaultHint> hintList = jsonToHintList(TextFileUtils.readFileFromJar(url,clazz));
            hintList.forEach(hint->hint.setId(clazz.getSimpleName()+hint.getTarget()));
            logger.info(String.format ("%s had %d hints loaded",clazz.getSimpleName(),hintList.size()));
            displayHints(hintList, force);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    private List<DefaultHint> jsonToHintList(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
           List<DefaultHint> hints =  mapper.readValue(json,mapper.getTypeFactory().constructCollectionType(List.class,DefaultHint.class));
            return hints;
        } catch (IOException ex) {
            Logger.getLogger(DefaultHintService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    // check if a hint was read
    private boolean wasRead(Hint hint) {
        //return false;
       return history.contains(hint.getId());

    }

    protected void loadConfiguration() {

        history = prefService.loadSetFromJson(CONFIGURATION_FILE, String.class);

    }

    @Override
    public void saveConfiguration() {
        if (history == null) {
            history = new HashSet<>();
        }
        prefService.savePreference(history, CONFIGURATION_FILE);
    }
    
    
    private class HintDecorator implements Hint {
        
        Hint hint;
        
        public HintDecorator(Hint hint) {
            this.hint = hint;
        }

        @Override
        public String getTarget() {
            return hint.getTarget();
        }

        @Override
        public String getId() {
            return hint.getId();
        }

        @Override
        public String getText() {
            return hint.getText();
        }

        @Override
        public boolean isRead() {
            return hint.isRead();
        }

        @Override
        public void setRead() {
            
            hint.setRead();
            history.add(hint.getId());
            saveConfiguration();
            
        }
        
        
        
    }

}
