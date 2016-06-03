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
package ijfx.core.imagedb;

import ijfx.core.metadata.MetaDataSet;
import ijfx.service.ui.JsonPreferenceService;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.notification.DefaultNotification;
import ijfx.ui.notification.NotificationService;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import mercury.core.MercuryTimer;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.scijava.Priority;
import org.scijava.app.StatusService;
import org.scijava.event.EventHandler;
import org.scijava.module.event.ModuleFinishedEvent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugins.commands.io.OpenFile;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class, priority = Priority.VERY_LOW_PRIORITY)
public class DefaultImageRecordService extends AbstractService implements ImageRecordService {

    Executor executor = Executors.newFixedThreadPool(1);

    HashMap<File, DefaultImageRecord> recordMap;

    Queue<File> fileQueue = new LinkedList<>();

    @Parameter
    MetaDataExtractionService metadataExtractorService;

    @Parameter
    ImageLoaderService imageLoaderService;

    @Parameter
    JsonPreferenceService jsonPreferenceService;
    
    Logger logger = ImageJFX.getLogger();

    @Parameter
    NotificationService notificationService;

    @Parameter
    StatusService statusService;
    
    private static String FILE_ADDED = "%s images where analyzed.";

    private static String JSON_FILE = "image_record.json";
    
    @Override
    public boolean isPresent(File file) {
        return getRecordMap().containsKey(file);
    }

    
    private Map<File,DefaultImageRecord> getRecordMap() {
        if(recordMap == null) {
            recordMap = new HashMap<>();
            load().forEach(record->recordMap.put(record.getFile(),record));
        }
        return recordMap;
    }
    
    @Override
    public void addRecord(ImageRecord imageRecord) {
        
        recordMap.put(imageRecord.getFile(), (DefaultImageRecord) imageRecord);

    }

    @Override
    public void addRecord(File file) {
        fileQueue.add(file);
        executor.execute(this::analyseQueue);
    }

    @Override
    public void addRecord(File file, MetaDataSet metaDataSet) {
        addRecord(new DefaultImageRecord(file, metaDataSet));
    }

    @Override
    public Collection<? extends ImageRecord> getRecords() {
        return getRecordMap().values();
    }

    @Override
    public Collection<? extends ImageRecord> queryRecords(Predicate<ImageRecord> query) {
        return getRecordMap().values().parallelStream().filter(query).collect(Collectors.toList());
    }

    public ImageRecord getRecord(File file) {
        
        ImageRecord imageRecord;
        if(getRecordMap().containsKey(file)==false) {
            imageRecord = new DefaultImageRecord(file, metadataExtractorService.extractMetaData(file));
            addRecord(imageRecord);
        }
        else {
            imageRecord = getRecordMap().get(file);
        }
        
        return imageRecord;
    }
    
    private void analyseQueue() {

        ArrayList<File> fileAdded = new ArrayList<>();
        IOFileFilter ioFileFilter = imageLoaderService.getIOFileFilter();

        while (fileQueue.size() > 0) {

            File f = fileQueue.poll();

            logger.info(String.format("IO filter result : %s : %s", f.getName(), ioFileFilter.accept(f, f.getName())));

            if (isPresent(f)) {
                logger.info(String.format("%s is already present.", f.getName()));
            }
            if (f.isDirectory()) {

                for (File child : f.listFiles()) {
                    logger.info("Checking child " + child.getName());
                    if (ioFileFilter.accept(child, child.getName())) {
                        fileQueue.add(child);
                        // logger.info("Adding child to the queue : "+child.getName());
                    }
                }
            } else if (isPresent(f) == false && ioFileFilter.accept(f, f.getName())) {

                logger.info("Reading metadata of " + f.getName());

                MercuryTimer mercuryTimer = new MercuryTimer("Metadata reading");
                try {
                    MetaDataSet metadataSet = metadataExtractorService.extractMetaData(f);

                    addRecord(f, metadataSet);
                    mercuryTimer.elapsed("Metadata reading");
                    fileAdded.add(f);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Couldn't read metadata for file " + f.getName(), e);
                }
            }

            //logger.info(String.format(" %d files left in the queue",fileAdded.size()));
        }

        if (fileAdded.size() > 0) {
            notificationService.publish(new DefaultNotification("Auto indexation", String.format(FILE_ADDED, fileAdded.size())));
            save();
        }
    }

    
    
    @EventHandler
    public void onFileOpened(ModuleFinishedEvent event) {
        logger.info("Module event !\n" + event.toString());
        if (event.getModule().getDelegateObject().getClass().equals(OpenFile.class)) {
            logger.info("A file was opened :-D");
            File f = (File) event.getModule().getInput("inputFile");
            logger.info("Checking " + f.getParentFile().getAbsolutePath());

            addRecord(f.getParentFile());
        }
    }
    
    
    private void save() {
        jsonPreferenceService.savePreference(getRecords(), JSON_FILE);
    }
    
    private List<DefaultImageRecord> load() {
        return jsonPreferenceService.loadListFromJson(JSON_FILE, DefaultImageRecord.class);
    }

    @Override
    public synchronized Collection<? extends ImageRecord> getRecordsFromDirectory(File directory) {
        
        List<ImageRecord> records = new ArrayList<>();
        
        statusService.showStatus(1, 10, "Analyzing folder : "+directory.getName());
        Collection<File> files = imageLoaderService.getAllImagesFromDirectory(directory);
        int count = 0;
        int total = files.size();
        for(File f : files) {
            if(isPresent(f)) {
                records.add(getRecordMap().get(f));
            }
            else {
                MetaDataSet m = metadataExtractorService.extractMetaData(f);
                addRecord(f, m);
                records.add(recordMap.get(f));
            }
            statusService.showProgress(count++, total);
        }
        
        
        return records;
    }

    public void forceSave() {
        ImageJFX.getThreadPool().execute(this::save);
    }
    
}
