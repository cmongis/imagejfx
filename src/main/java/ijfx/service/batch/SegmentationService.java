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
package ijfx.service.batch;

import com.google.common.collect.Lists;
import ijfx.core.imagedb.ImageRecordService;
import ijfx.core.imagedb.MetaDataExtractionService;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.utils.DimensionUtils;
import ijfx.plugins.commands.BinaryToOverlay;
import ijfx.service.IjfxService;
import ijfx.service.ImagePlaneService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.service.batch.SegmentationService.Output;
import ijfx.service.batch.SegmentationService.Source;
import ijfx.service.batch.input.BatchInputBuilder;
import ijfx.service.log.DefaultLoggingService;
import ijfx.service.overlay.OverlayDrawingService;
import ijfx.service.overlay.OverlayStatService;
import ijfx.service.overlay.OverlayUtilsService;
import ijfx.service.overlay.io.OverlayIOService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.workflow.Workflow;
import ijfx.ui.IjfxEvent;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorationMode;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.FolderManagerService;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.image.Image;
import mongis.utils.ProgressHandler;
import mongis.utils.SilentProgressHandler;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.event.OverlayCreatedEvent;
import net.imagej.overlay.Overlay;
import net.imglib2.RandomAccessibleInterval;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Service.class)
public class SegmentationService extends AbstractService implements IjfxService {

    @Parameter
    EventService eventService;

    @Parameter
    ExplorerService explorerService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    OverlayIOService overlayIoService;

    @Parameter
    ImagePlaneService imagePlaneService;

    @Parameter
    OverlayDrawingService overlayDrawingService;

    @Parameter
    OverlayStatService overlayStatsService;

    @Parameter
    DefaultLoggingService loggerService;

    @Parameter
    MetaDataExtractionService metadataExtractionService;

    @Parameter
    ImageRecordService imageRecordService;

    @Parameter
    OverlayUtilsService overlayUtilsService;

    @Parameter
    TimerService timerService;

    @Parameter
    BatchService batchService;

    @Parameter
    UIService uiService;

    @Parameter
    FolderManagerService folderManagerService;

    public enum Source {
        EXPLORER_SELECTED, EXPLORER_ALL, IMAGEJ
    }

    public enum Output {
        DISPLAY, SAVE_NEXT_TO_FILE
    }

    public enum Option {
        SAVE_NEXT_TO_SOURCE, SAVE_MASK
    }

    private Source inputSource;

    private Output outputFormat;

    public void setInputSource(Source inputSource) {
        this.inputSource = inputSource;
    }

    public Source getInputSource() {
        return inputSource;
    }

    public class SourceChangeEvent extends IjfxEvent<Source> {

    }

    public List<BatchSingleInput> getInputs() {

        List<BatchSingleInput> inputs;

        if (inputSource == Source.EXPLORER_SELECTED) {
            inputs = explorerService
                    .getSelectedItems()
                    .stream()
                    .map(this::prepareExplorable)
                    .map(this::applyOutputConfiguration)
                    .map(builder -> builder.getInput())
                    .collect(Collectors.toList());

        } else if (inputSource == Source.EXPLORER_ALL) {
            inputs = explorerService
                    .getItems()
                    .stream()
                    .map(this::prepareExplorable)
                    .map(this::applyOutputConfiguration)
                    .map(builder -> builder.getInput())
                    .collect(Collectors.toList());
        } else if (inputSource == Source.IMAGEJ) {
            inputs = Arrays.asList(
                    new BatchInputBuilder(getContext())
                    .from(imageDisplayService.getActiveImageDisplay())
                    .display()
                    .getInput());
        } else {
            inputs = new ArrayList<>();
        }
        return inputs;

    }

    private BatchInputBuilder prepareExplorable(Explorable explorable) {

        BatchInputBuilder inputBuilder = new BatchInputBuilder(getContext())
                .from(explorable);
        return inputBuilder;
    }

    private BatchInputBuilder applyOutputConfiguration(BatchInputBuilder builder) {
        if (outputFormat == Output.DISPLAY) {
            builder.display();

        } else if (outputFormat == Output.SAVE_NEXT_TO_FILE) {
            builder.onFinished(this::saveOverlaysNextToSourceFile);
        }
        return builder;
    }

    public Consumer<BatchSingleInput> addObjectToList(List<SegmentedObject> objectList) {
        return input -> {
            Overlay[] overlay = BinaryToOverlay.transform(getContext(), input.getDataset(), true);
            File file = new File(input.getSourceFile());
            List<SegmentedObject> measure = measure(new SilentProgressHandler(), Arrays.asList(overlay), file);
            objectList.addAll(measure);
        };
    }

    public void saveOverlaysNextToSourceFile(BatchSingleInput input) {
        Overlay[] overlay = BinaryToOverlay.transform(getContext(), input.getDataset(), true);

        File file = new File(input.getSourceFile());

        List<SegmentedObject> measure = measure(new SilentProgressHandler(), Arrays.asList(overlay), file);
        eventService.publish(new ObjectSegmentedEvent(file, measure));
        /*
        try {
            //overlayIoService.saveOverlaysNextToFile(Arrays.asList(overlay), new File(input.getSourceFile()));
        } catch (IOException ex) {
            Logger.getLogger(SegmentationService.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    public void setOutputFormat(Output outputFormat) {
        this.outputFormat = outputFormat;
    }

    protected Image findThumb(Explorable explorable) {
        return explorable.getImage();
    }

    @Parameter
    LoadingScreenService loadingService;

    public List<Overlay> segmentAndAddToDisplay(Dataset mask, ImageDisplay inputDisplay, boolean objectsAreWhite) {

        Timer timer = timerService.getTimer(getClass());

        timer.start();
        overlayUtilsService.removeAllOverlay(inputDisplay);
        timer.elapsed("remove all overlay");

        Overlay[] overlay = BinaryToOverlay.transform(getContext(), (RandomAccessibleInterval) mask.getImgPlus(), objectsAreWhite);
        timer.elapsed("BinaryToOverlay.transform(...)");
        // giving a random color to each overlay
        overlayStatsService.setRandomColor(Arrays.asList(overlay));
        timer.elapsed("overlayStatsSrv.setRandomColor(overlays)");
        // deleting the overlay owned previously by the input
        inputDisplay.addAll(Stream.of(overlay).parallel().map(o -> imageDisplayService.createDataView(o)).collect(Collectors.toList()));
        timer.elapsed("inputDisplay.addAll()");
        Stream.of(overlay).parallel().map(o -> new OverlayCreatedEvent(o)).forEach(eventService::publish);
        timer.elapsed("inputDisplay.update()");

        return Lists.newArrayList(overlay);
    }

    public List<SegmentedObject> measure(List<Overlay> overlays, MetaDataSet planeMetaData, Dataset dataset) {
        // reading the planar position
        final long[] position = DimensionUtils.planarToAbsolute(DimensionUtils.readLongArray(planeMetaData.get(MetaData.PLANE_NON_PLANAR_POSITION).getStringValue()));

        Timer globalTimer = timerService.getTimer(getClass());

        globalTimer.start();
        List<SegmentedObject> segmentedObjectList = overlays
                .stream()
                .map(overlay -> {
                    try {
                        Timer timer = timerService.getTimer(this.getClass());
                        timer.start();
                        DefaultSegmentedObject segmentedObject = new DefaultSegmentedObject(overlay, overlayStatsService.getStatistics(overlay, dataset, position));
                        timer.elapsed("new SegmentedObject()");
                        segmentedObject.getMetaDataSet().merge(planeMetaData);
                        timer.elapsed("segmentedObject.merge()");
                        return segmentedObject;
                    } catch (Exception e) {
                        loggerService.warn(e);
                    }
                    return null;
                })
                .filter(o -> o != null)
                .collect(Collectors.toList());

        globalTimer.elapsed("measure(List<Overlay>)");
        return segmentedObjectList;
    }

    public List<SegmentedObject> measure(ProgressHandler handler, List<Overlay> overlays, File file) {

        try {
            Dataset dataset = imagePlaneService.openVirtualDataset(file);
            MetaDataSet m = imageRecordService.getRecord(file).getMetaDataSet();
            List<MetaDataSet> extractPlaneMetaData = metadataExtractionService.extractPlaneMetaData(m);
            handler.setTotal(extractPlaneMetaData.size());
            return extractPlaneMetaData
                    .stream()
                    .map(set -> {
                        handler.increment(1);
                        return measure(overlays, set, dataset);
                    })
                    .flatMap(obj -> obj.stream())
                    .filter(obj -> obj != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            loggerService.error(e);
            return new ArrayList<>();
        }
    }

    public Boolean measureFromExplorer(ProgressHandler progress, Workflow workflow) {

        final List<SegmentedObject> objectFound = new ArrayList<>();
        List<BatchSingleInput> input = explorerService.getSelectedItems()
                .stream()
                .map(explorable -> new BatchInputBuilder(getContext())
                        .from(explorable)
                        .onFinished(addObjectToList(objectFound))
                        .getInput()
                )
                .collect(Collectors.toList());

        boolean result = batchService.applyWorkflow(progress, input, workflow);
        if (result == true) {
            uiService.showDialog(String.format("%d objects where segmented", objectFound.size()), "Segmentation over");
            folderManagerService.getCurrentFolder().addObjects(objectFound);
            folderManagerService.setExplorationMode(ExplorationMode.OBJECT);
            return result;
        } else {
            uiService.showDialog(String.format("Error when segmenting the objects"));
            return false;
        }

    }

}
