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
import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.ui.explorer.event.FolderUpdatedEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import mongis.utils.AsyncCallback;
import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class DefaultFolder implements Folder {

    private File file;
    private String name;
    private List<Explorable> items;

    @Parameter
    ImageRecordService imageRecordService;

    @Parameter
    EventService eventService;
    
    @Parameter
    Context context;

    @Parameter
    TimerService timerService;
    
    public DefaultFolder() {

    }

    public DefaultFolder(File file) {
        this.file = file;
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
    public List<Explorable> getItemList() {

        if (items == null) {

            items = new ArrayList<>();
            
            new AsyncCallback<Void,List<Explorable>>()
                    .run(this::fetchItems)
                    .then(this::addItems)
                    .start();
                    
            
        }
        return items;
    }

    private List<Explorable> fetchItems(Void v) {
        
        Timer timer = timerService.getTimer(this.getClass());
        timer.start();
        Collection<? extends ImageRecord> records = imageRecordService.getRecordsFromDirectory(file);
        timer.elapsed("record fetching");
        List<Explorable> explorables = records
                .stream()
                .map(record -> new ImageRecordIconizer(context, record))
                .collect(Collectors
                        .toList());
        
        System.out.println(String.format("%d records fetched",records.size()));
        return explorables;
    }

    @JsonSetter("path")
    public void setPath(String path) {
        file = new File(path);
        items = null;
    }

    @JsonGetter("path")
    public String getPath() {
        return file.getAbsolutePath();
    }

    private void addItems(List<Explorable> explorables) {
        items.addAll(explorables);
        System.out.println("Added "+explorables.size());
        eventService.publish(new FolderUpdatedEvent().setObject(this));
    }

}