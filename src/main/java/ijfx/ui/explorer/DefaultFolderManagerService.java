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
import ijfx.core.metadata.MetaData;
import ijfx.core.stats.IjfxStatisticService;
import ijfx.service.ui.JsonPreferenceService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.explorer.event.FolderAddedEvent;
import ijfx.ui.explorer.event.FolderUpdatedEvent;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.notification.DefaultNotification;
import ijfx.ui.notification.NotificationService;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import mongis.utils.AsyncCallable;
import mongis.utils.AsyncCallback;
import mongis.utils.ProgressHandler;
import mongis.utils.SilentProgressHandler;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
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

    @Parameter
    LoadingScreenService loadingScreenService;
    
    @Parameter
    IjfxStatisticService statsService;
    
    @Parameter
    NotificationService notificationService;
    
    private static String FOLDER_PREFERENCE_FILE = "folder_db.json";

    Logger logger = ImageJFX.getLogger();

    ExplorationMode currentExplorationMode;

    List<Explorable> currentItems;

    ExecutorService executorService = Executors.newFixedThreadPool(1);

    
    @Override
    public Folder addFolder(File file) {
        Folder f = new DefaultFolder(file);
        context.inject(f);
        return addFolder(f);
    }

    protected Folder addFolder(Folder f) {
        getFolderList().add(f);

        if (folderList.size() == 1) {
            setCurrentFolder(f);
        }

        eventService.publish(new FolderAddedEvent().setObject(f));
        save();
        return f;
    }

    @Override
    public List<Folder> getFolderList() {

        if (folderList == null) {
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
        updateExploredElements();
    }

    @EventHandler
    public void onFolderUpdated(FolderUpdatedEvent event) {
        logger.info("Folder updated ! " + event.getObject().getName());
        if (currentFolder == event.getObject()) {
            updateExploredElements();
        }
    }

    @Override
    public void setExplorationMode(ExplorationMode mode) {
        if (mode == currentExplorationMode) {
            return;
        }
        currentExplorationMode = mode;
        eventService.publish(new ExplorationModeChangeEvent().setObject(mode));
        updateExploredElements();
    }

    private void updateExploredElements() {

        ExplorationMode mode = currentExplorationMode;

        AsyncCallable<List<Explorable>> task = new AsyncCallable<>();
        task.setTitle("Fetching elements...");

        if (currentFolder == null) {
            return;
        } else {

            if (null != mode) {
                switch (mode) {
                    case FILE:
                        task.run(currentFolder::getFileList);
                        break;
                    case PLANE:
                        task.run(currentFolder::getPlaneList);
                        break;
                    default:
                        task.run(currentFolder::getObjectList);
                        break;
                }
            }
            task.then(this::setItems);
            task.start();
            loadingScreenService.frontEndTask(task, false);
        }
        logger.info("Exploration mode changed : " + mode.toString());
    }
    private void setItems(List<Explorable> items) {
        explorerService.setItems(items);
        
        new AsyncCallback<List<Explorable>,Integer>(items)
                .run(this::fetchMoreStatistics)
                .then(n->{
                 if(n > 0 && explorerService.getItems() == items) { 
                     explorerService.setItems(items);
                     notificationService.publish(new DefaultNotification(getCurrentFolder().getName(), String.format("The statistics of %d images has been completed.",n)));
                 }
                })
                .start();
        
    }
    private Integer fetchMoreStatistics(ProgressHandler progress,List<Explorable> explorableList) {
        if(progress == null) progress = new SilentProgressHandler();
        progress.setStatus("Completing statistics of the objects");
        Integer elementAnalyzedCount = 0;
        int elements = explorableList.size();
        int i = 0;
        for (Explorable e : explorableList) {
            if (!e.getMetaDataSet().containsKey(MetaData.STATS_PIXEL_MIN)) {
                progress.setProgress(i,elements);
                if (e instanceof ImageRecordIconizer) {
                    ImageRecordIconizer iconizer = (ImageRecordIconizer) e;
                    SummaryStatistics stats = statsService.getStatistics(iconizer.getImageRecord().getFile());
                    iconizer.getMetaDataSet().putGeneric(MetaData.STATS_PIXEL_MIN, stats.getMin());
                    iconizer.getMetaDataSet().putGeneric(MetaData.STATS_PIXEL_MAX, stats.getMax());
                    iconizer.getMetaDataSet().putGeneric(MetaData.STATS_PIXEL_MEAN, stats.getMean());
                    iconizer.getMetaDataSet().putGeneric(MetaData.STATS_PIXEL_STD_DEV,stats.getMean());
                    elementAnalyzedCount++;
                }
            }
            i++;
        }
        return elementAnalyzedCount;
    }
    @Override
    public ExplorationMode getCurrentExplorationMode() {
        return currentExplorationMode;
    }
    private void save() {
        HashMap<String, String> folderMap = new HashMap<>();
        for (Folder f : getFolderList()) {
            folderMap.put(f.getName(), f.getDirectory().getAbsolutePath());
        }
        jsonPrefService.savePreference(folderMap, FOLDER_PREFERENCE_FILE);
    }
    private synchronized void load() {
        Map<String, String> folderMap = jsonPrefService.loadMapFromJson(FOLDER_PREFERENCE_FILE, String.class, String.class);
        folderMap.forEach((name, folderPath) -> {
            DefaultFolder folder = new DefaultFolder(new File(folderPath));
            context.inject(folder);
            folder.setName(name);
            folderList.add(folder);
        });
    }

    @Override
    public void completeStatistics() {
        loadingScreenService.frontEndTask(
          new AsyncCallback<List<Explorable>,Integer>()
                .run(this::fetchMoreStatistics)
                .setInput(getCurrentFolder().getFileList())
                .then(this::onStatisticComputingEnded)
                .start()
        );
        
    }
    
    private void onStatisticComputingEnded(Integer computedImages) {
        notificationService.publish(new DefaultNotification("Statistic Computation", String.format("%d images where computed.",computedImages)));
    }
    
}
