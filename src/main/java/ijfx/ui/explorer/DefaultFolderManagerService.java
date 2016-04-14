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

import ijfx.core.imagedb.MetaDataExtractionService;
import ijfx.core.metadata.MetaDataSet;
import ijfx.service.ui.JsonPreferenceService;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.explorer.event.FolderAddedEvent;
import ijfx.ui.explorer.event.FolderUpdatedEvent;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.scijava.Context;
import org.scijava.event.EventHandler;
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
public class DefaultFolderManagerService extends AbstractService implements FolderManagerService {

    List<Folder> folderList;

    Folder currentFolder;

    @Parameter
    ExplorerService explorerService;

    @Parameter
    EventService eventService;

    @Parameter
    Context context;

    @Parameter
    UiContextService uiContextService;

    @Parameter
    JsonPreferenceService jsonPrefService;

    @Parameter
    MetaDataExtractionService metaDataExtractionService;

    
    private static String FOLDER_PREFERENCE_FILE = "folder_db.json";
    
    Logger logger = ImageJFX.getLogger();

    ExplorationMode currentExplorationMode;

    
   List<Explorable> currentItems;
    
    @Override
    public Folder addFolder(File file) {
        Folder f = new DefaultFolder(file);
        context.inject(f);
        return addFolder(f);
    }
    protected Folder addFolder(Folder f) {
        folderList.add(f);

        if (folderList.size() == 1) {
            setCurrentFolder(f);
        }

        eventService.publish(new FolderAddedEvent().setObject(f));
        save();
        return f;
    }

    @Override
    public List<Folder> getFolderList() {
        
        if(folderList == null) {
            folderList = new ArrayList<>();
            load();
        }
        
        return folderList;
    }

    @Override
    public Folder getCurrentFolder() {
        return currentFolder;
    }

    @Override
    public void setCurrentFolder(Folder folder) {
        currentFolder = folder;

        logger.info("Setting current folder " + folder.getName());

        explorerService.setItems(currentFolder.getFileList());

        uiContextService.enter("explore-files");
        uiContextService.update();

    }

    @EventHandler
    public void onFolderUpdated(FolderUpdatedEvent event) {
        logger.info("Folder updated ! " + event.getObject().getName());
        if (currentFolder == event.getObject()) {
            explorerService.setItems(event.getObject().getFileList());
        }
    }

    @Override
    public void setExplorationMode(ExplorationMode mode) {
        if(mode == currentExplorationMode) return;
        currentExplorationMode = mode;
        eventService.publish(new ExplorationModeChangeEvent().setObject(mode));
        
        
        if(mode == ExplorationMode.FILE) {
            explorerService.setItems(currentFolder.getFileList());
        }
        else if (mode == ExplorationMode.PLANE) {
            explorerService.setItems(currentFolder.getPlaneList());
        }
        else {
            explorerService.setItems(currentFolder.getObjectList());
        }
        
        logger.info("Exploration mode changed : "+mode.toString());
    }

    @Override
    public ExplorationMode getCurrentExplorationMode() {
        return currentExplorationMode;
    }

  

    private void save() {
        HashMap<String,String> folderMap = new HashMap<>();
        for(Folder f : getFolderList()) {
            folderMap.put(f.getName(),f.getDirectory().getAbsolutePath());
        }
        
        
        jsonPrefService.savePreference(folderMap,FOLDER_PREFERENCE_FILE);
        
    }

    private synchronized void load() {
        Map<String,String> folderMap  = jsonPrefService.loadMapFromJson(FOLDER_PREFERENCE_FILE,String.class,String.class);
        folderMap.forEach((name,folderPath)->{
            DefaultFolder folder = new DefaultFolder(new File(folderPath));
            context.inject(folder);
            folder.setName(name);
            folderList.add(folder);
        });
    }
    
   
}
