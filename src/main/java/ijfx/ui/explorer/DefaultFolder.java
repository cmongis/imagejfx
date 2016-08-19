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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import ijfx.core.imagedb.ImageRecord;
import ijfx.core.imagedb.ImageRecordService;
import ijfx.core.imagedb.MetaDataExtractionService;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.metadata.MetaDataSetType;

import ijfx.core.stats.IjfxStatisticService;
import ijfx.service.ImagePlaneService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.service.batch.ObjectSegmentedEvent;
import ijfx.service.batch.SegmentedObject;
import ijfx.service.overlay.io.OverlayIOService;
import ijfx.service.thumb.ThumbService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.watch_dir.DirectoryWatchService;
import ijfx.service.watch_dir.FileChangeListener;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.explorer.event.FolderUpdatedEvent;
import ijfx.ui.explorer.view.SegmentedObjectExplorerWrapper;
import ijfx.ui.main.ImageJFX;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import mongis.utils.CallbackTask;
import mongis.utils.ProgressHandler;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS
 */
public class DefaultFolder implements Folder, FileChangeListener {

    private File file;
    private String name;
    private List<Explorable> files;
    private List<Explorable> planes;
    private List<Explorable> objects = new ArrayList<>();

    Logger logger = ImageJFX.getLogger();

    @Parameter
    ImageRecordService imageRecordService;

    @Parameter
    EventService eventService;

    @Parameter
    Context context;

    @Parameter
    TimerService timerService;

    @Parameter
    IjfxStatisticService statsService;

    @Parameter
    StatusService statusService;

    @Parameter
    MetaDataExtractionService metadataExtractionService;

    @Parameter
    ImagePlaneService imagePlaneService;

    @Parameter
    UIService uiService;

    @Parameter
    ThumbService thumberService;

    @Parameter
    DatasetIOService datasetIoService;

    @Parameter
    ActivityService activityService;

    @Parameter
    DirectoryWatchService dirWatchService;

    @Parameter
    OverlayIOService overlayIOService;

    @Parameter
    LoadingScreenService loadingScreenService;

    Property<Task> currentTaskProperty = new SimpleObjectProperty<>();

    public DefaultFolder() {

    }

    public DefaultFolder(File file) {
        setPath(file.getAbsolutePath());
    }

    @Override
    @JsonGetter("name")
    public String getName() {
        return name == null ? file.getName() : name;
    }

    @Override
    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;

    }

    @Override
    public File getDirectory() {
        return file;
    }

    @Override
    public List<Explorable> getFileList() {

        if (files == null) {

            files = new ArrayList<>();

            Task task = new CallbackTask<Void, List<Explorable>>()
                    .setName("Getting file list...")
                    .run(this::fetchFiles)
                    .then(result -> {
                        files = result;
                        eventService.publish(new FolderUpdatedEvent().setObject(this));
                    })
                    .setIn(currentTaskProperty())
                    .start();

            loadingScreenService.frontEndTask(task);

        }
        listenToDirectoryChange();
        return files;
    }

    private List<Explorable> fetchFiles(ProgressHandler progress, Void v) {

        //if(progress == null) progress = new SilentProgressHandler();
        final ProgressHandler finalProgress = ProgressHandler.check(progress);

        Timer timer = timerService.getTimer(this.getClass());
        timer.start();
        Collection<? extends ImageRecord> records = imageRecordService.getRecordsFromDirectory(progress,file);
        timer.elapsed("record fetching");
        progress.setStatus("Reading folder...");

        progress.setTotal(records.size());
        
        List<Explorable> explorables = records
                .stream()
                .map(record -> {
                    
                    File overlayJsonFile = overlayIOService.getOverlayFileFromImageFile(record.getFile());
                    finalProgress.increment(0.5);
                    if (overlayJsonFile.exists()) {
                        //getObjectList().addAll(loadOverlay(record.getFile(), overlayJsonFile));
                    }
                    record.getMetaDataSet().setType(MetaDataSetType.FILE);
                    return record;
                })
                .map(record -> {
                    finalProgress.increment(0.5);
                    return new ImageRecordIconizer(context, record);

                })
                .collect(Collectors
                        .toList());

        System.out.println(String.format("%d records fetched", records.size()));
        imageRecordService.forceSave();
        return explorables;
    }

    @JsonSetter("path")
    public void setPath(String path) {
        file = new File(path);
        files = null;
    }

    @JsonGetter("path")
    public String getPath() {
        return file.getAbsolutePath();
    }

    private void addItems(List<Explorable> explorables) {
        files.addAll(explorables);
        eventService.publish(new FolderUpdatedEvent().setObject(this));
    }

    @Override
    public List<Explorable> getPlaneList() {

        if (planes == null) {

            System.out.println("Fetching planes !");
            List<MetaDataSet> mList = new ArrayList<>(getFileList().size() * 3);
            for (Explorable e : getFileList()) {
                mList.addAll(metadataExtractionService.extractPlaneMetaData(e.getMetaDataSet()));
            }

            planes = mList.stream()
                    .map(m->{ m.setType(MetaDataSetType.PLANE); return m;})
                    .map(m -> new PlaneMetaDataSetWrapper(context, m))
                    .collect(Collectors.toList());

        }
        System.out.println(String.format("%d planes fetched", planes.size()));
        return planes;
    }

    @Override
    public List<Explorable> getObjectList() {
        return objects;
    }

    public void onFileAdded(List<Explorable> files) {

    }

    private boolean registered = false;

    private void listenToDirectoryChange() {

        if (registered) {
            return;
        }

        try {
            System.out.println("Listening to " + getPath());
            dirWatchService.register(this, getPath());
            registered = true;
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void onFileCreate(String filePath) {
        logger.info("File added " + filePath);

        File file = new File(getDirectory(), filePath);

        if (file.getName().endsWith(OverlayIOService.OVERLAY_FILE_EXTENSION)) {
            File imageFile = overlayIOService.getImageFileFromOverlayFile(file);
            System.out.println(imageFile.getAbsolutePath());
            getObjectList().addAll(loadOverlay(imageFile, file));
        }

    }

    @Override
    public void onFileModify(String filePath) {
        logger.info("File was modified : " + filePath);
    }

    private List<? extends Explorable> loadOverlay(File imageFile, File overlayJsonFile) {
        List<? extends Explorable> collect = overlayIOService.loadOverlays(overlayJsonFile)
                .stream()
                .filter(o -> o != null)
                .map(overlay -> new OverlayExplorableWrapper(context, imageFile, overlay))
                .filter(expl -> expl.isValid())
                .collect(Collectors.toList());

        System.out.println("Overlay collecté : " + collect.size());
        return collect;

    }

    @Override
    public Property<Task> currentTaskProperty() {
        return currentTaskProperty;
    }
    
    @EventHandler
    public void onObjectSegmented(ObjectSegmentedEvent event) {
        if(event.getFile().getAbsolutePath().indexOf(file.getAbsolutePath()) == 0) {
            System.out.println("Adding objects");
            getObjectList().addAll(event
                    .getObject()
                    .stream()
                    .map(o->new MetaDataSetExplorerWrapper(o.getMetaDataSet()))
                    .collect(Collectors.toList())
            ); 
        }
    }

    @Override
    public void addObjects(List<SegmentedObject> objects) {
        
        
        logger.info(String.format("Adding %d objects",objects.size()));
        
        getObjectList().clear();
        
        getObjectList().addAll(objects.stream()
        .map(o->new SegmentedObjectExplorerWrapper(o))
        .collect(Collectors.toList()));
        

        
        eventService.publishLater(new FolderUpdatedEvent().setObject(this));
        
    }
    
    
    public boolean isFilePartOf(File f) {
        return f.getAbsolutePath().startsWith(file.getAbsolutePath());
    }
    
}
