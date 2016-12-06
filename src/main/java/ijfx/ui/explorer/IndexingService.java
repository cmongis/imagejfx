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

import ijfx.core.imagedb.ImageRecord;
import ijfx.core.imagedb.ImageRecordService;
import ijfx.core.metadata.MetaData;
import ijfx.service.IjfxService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import java.io.File;
import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import mongis.utils.ProgressHandler;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class IndexingService extends AbstractService implements IjfxService {

    @Parameter
    ImageRecordService imageRecordService;

    @Parameter
    TimerService timerService;

    @Parameter
    Context context;

    public Stream<Explorable> indexDirectory(ProgressHandler origProgress, File directory) {

        //if(progress == null) progress = new SilentProgressHandler();
        final ProgressHandler progress = ProgressHandler.check(origProgress);

        Timer timer = timerService.getTimer(this.getClass());
        timer.start();
        Collection<? extends ImageRecord> records = imageRecordService.getRecordsFromDirectory(progress, directory);
        timer.elapsed("record fetching");
        progress.setStatus("Reading folder...");

        progress.setTotal(records.size());

        return records
                .stream()
                .parallel()
                .map(record -> {
                    progress.increment(1);
                    return getSeries(record);
                })
                .flatMap(self -> self);

    }

    public Stream<Explorable> getSeries(ImageRecord record) {
        if (record.getMetaDataSet().containsKey(MetaData.SERIE_COUNT) && record.getMetaDataSet().get(MetaData.SERIE_COUNT).getIntegerValue() > 1) {

            int serieCount = record.getMetaDataSet().get(MetaData.SERIE_COUNT).getIntegerValue();

            return IntStream
                    .range(0, serieCount)
                    .mapToObj(i -> new ImageRecordIconizer(context, record, i));

        } else {
            return Stream.of(new ImageRecordIconizer(context, record));
        }
    }

}
