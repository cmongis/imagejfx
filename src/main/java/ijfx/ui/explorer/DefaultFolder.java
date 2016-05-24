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
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.project.Project;
import ijfx.core.stats.IjfxStatisticService;
import ijfx.service.ImagePlaneService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.service.overlay.io.OverlayIOService;
import ijfx.service.thumb.ThumbService;
import ijfx.service.watch_dir.DirectoryWatchService;
import ijfx.service.watch_dir.FileChangeListener;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.explorer.event.FolderUpdatedEvent;
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
import mongis.utils.AsyncCallback;
import net.imagej.overlay.Overlay;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS
 */
public class DefaultFolder implements Folder,FileChangeListener{

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
            
            
            new AsyncCallback<Void,List<Explorable>>()
                    .setInput(null)
                    .run(this::fetchItems)
                    .then(result->{
                        files = result;
                        eventService.publish(new FolderUpdatedEvent().setObject(this));
                    })
                    .start();
                    
            //files = fetchItems(null);
            
            
           
            

        }
         listenToDirectoryChange();
        return files;
    }

    private List<Explorable> fetchItems(Void v) {

        
        
        Timer timer = timerService.getTimer(this.getClass());
        timer.start();
        Collection<? extends ImageRecord> records = imageRecordService.getRecordsFromDirectory(file);
        timer.elapsed("record fetching");
        List<Explorable> explorables = records
                .stream()
                .map(record->{
                    File overlayJsonFile = overlayIOService.getOverlayFileFromImageFile(record.getFile());
                    
                    if(overlayJsonFile.exists()) getObjectList().addAll(loadOverlay(record.getFile(), overlayJsonFile));
                    return record;
                })
                
                .map(record -> new ImageRecordIconizer(context, record))
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
        System.out.println("Added " + explorables.size());
        eventService.publish(new FolderUpdatedEvent().setObject(this));

        // now we can start a second thread that will slowly get statistics from images
        new AsyncCallback<List<Explorable>, Integer>()
                .setInput(files)
                .run(this::fetchMoreStatistics)
                .then(count -> {
                    if (count > 0) {
                        eventService.publish(new FolderUpdatedEvent().setObject(this));
                    }
                })
                .start();
    }

    private Integer fetchMoreStatistics(List<Explorable> explorableList) {
        Integer elementAnalyzedCount = 0;
        int elements = explorableList.size();
        int i = 0;
        for (Explorable e : explorableList) {
            statusService.showStatus(i, elements, "Fetchting min/max for more exploration.");

            if (!e.getMetaDataSet().containsKey(MetaData.STATS_PIXEL_MIN)) {
                if (e instanceof ImageRecordIconizer) {
                    ImageRecordIconizer iconizer = (ImageRecordIconizer) e;
                    SummaryStatistics stats = statsService.getStatistics(iconizer.getImageRecord().getFile());
                    iconizer.getMetaDataSet().putGeneric(MetaData.STATS_PIXEL_MIN, stats.getMin());
                    iconizer.getMetaDataSet().putGeneric(MetaData.STATS_PIXEL_MAX, stats.getMax());
                    iconizer.getMetaDataSet().putGeneric(MetaData.STATS_PIXEL_MEAN, stats.getMean());
                    elementAnalyzedCount++;
                }
            }
            i++;
        }
        return elementAnalyzedCount;
    }

    public Project getFolderProject() {
        return null;
    }

    private Project createPlaneProject() {

        return null;
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
                    .map(m -> new PlaneMetaDataSetWrapper(context,m))
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
        new AsyncCallback<>(files)
                .run(this::fetchMoreStatistics)
                .startIn(ImageJFX.getThreadQueue());
                
    }
    
    
    private boolean registered = false;
    
    private void listenToDirectoryChange() {
        
        if(registered) return;
        
        try {
            System.out.println("Listening to "+getPath());
            dirWatchService.register(this, getPath());
            registered = true;
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    @Override
    public void onFileCreate(String filePath) {
        logger.info("File added "+filePath);
        
        File file = new File(getDirectory(),filePath);
        
        if(file.getName().endsWith(OverlayIOService.OVERLAY_FILE_EXTENSION)) {
            File imageFile = overlayIOService.getImageFileFromOverlayFile(file);
            System.out.println(imageFile.getAbsolutePath());
            getObjectList().addAll(loadOverlay(imageFile, file));
        }
        
       
        
    }
    
    @Override
    public void onFileModify(String filePath) {
        logger.info("File was modified : "+filePath);
    }
    
   
    private List<Explorable> loadOverlay(File imageFile, File overlayJsonFile) {
        List<Explorable> collect = overlayIOService.loadOverlays(overlayJsonFile)
                .stream()
                .filter(o->o!=null)
                .map(overlay->new OverlayExplorableWrapper(context, imageFile, overlay))
                .filter(expl->expl.isValid())
                
                .collect(Collectors.toList());
        
        System.out.println("Overlay collecté : "+collect.size());
        return collect;
                
        
    }
    
    
    
    
    
}
