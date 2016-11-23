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
package ijfx.service.overlay;

import com.google.common.collect.Lists;
import ijfx.bridge.ImageJContainer;
import ijfx.service.IjfxService;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.UiContexts;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.main.ImageJFX;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.util.Callback;
import net.imagej.display.DataView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.display.OverlayView;
import net.imagej.event.OverlayCreatedEvent;
import net.imagej.event.OverlayDeletedEvent;
import net.imagej.overlay.BinaryMaskOverlay;
import net.imagej.overlay.Overlay;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.type.logic.BitType;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugins.commands.io.OpenFile;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Service.class)
public class OverlayUtilsService extends AbstractService implements IjfxService {

    @Parameter
    CommandService commandService;

    @Parameter
    DatasetIOService datasetIoService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    OverlayService overlayService;

    @Parameter
    EventService eventService;

    @Parameter
    OverlaySelectionService overlaySelectionSrv;

    @Parameter
    ActivityService activityService;

    @Parameter
    UiContextService uiContextService;
    
    Logger logger = ImageJFX.getLogger();

    public void openOverlay(File file, Overlay selected) {

        ImageDisplay display;

        display = findDisplay(file);

        //if we couldn't find the display, we open the image
        if (display == null) {

            Future<CommandModule> run = commandService.run(OpenFile.class, true, "inputFile", file);

            try {
                run.get();

                display = findDisplay(file);

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Couldn't open " + file.getAbsolutePath(), e);
                return;
            }
        }

        // now we check that it's still not null so we can
        if (display != null) {
            if (findOverlay(selected, display) == null) {
                display.display(selected);
            } else {
                overlaySelectionSrv.selectOnlyOneOverlay(display, selected);
            }
            uiContextService.enter(UiContexts.VISUALIZE);
            activityService.openByType(ImageJContainer.class);

        } else {
            logger.severe("Couldn't find open file nor overlay");

        }
    }

    public Overlay findOverlay(Overlay overlay, ImageDisplay imageDisplay) {
        return imageDisplay
                .stream()
                .filter(dataview -> dataview instanceof OverlayView)
                .map(overlayView -> (Overlay) overlayView.getData())
                .filter(o -> o == overlay)
                .findFirst()
                .orElse(null);
    }

    public <T extends Overlay> T findOverlayOfType(ImageDisplay imageDisplay, Class<T> clazz) {
        return (T) imageDisplay.
                stream()
                .filter(dataview -> dataview instanceof OverlayView)
                .map(overlayView -> (Overlay) overlayView.getData())
                .filter(o -> o.getClass().isAssignableFrom(clazz))
                .findFirst()
                .orElse(null);
    }

    public <T extends Overlay> List<T> findOverlaysOfType(ImageDisplay imageDisplay, Class<T> clazz) {
        return imageDisplay.
                stream()
                .filter(dataview -> dataview instanceof OverlayView)
                .map(overlayView -> (Overlay) overlayView.getData())
                .filter(o -> o.getClass().isAssignableFrom(clazz))
                .map(o -> (T) o)
                .collect(Collectors.toList());
    }

    public <T extends Overlay> T findOverlayOfType(ImageDisplay imageDisplay, Class<T> clazz, Callback<ImageDisplay, T> factory) {
        return (T) imageDisplay.
                stream()
                .filter(dataview -> dataview instanceof OverlayView)
                .map(overlayView -> (Overlay) overlayView.getData())
                .filter(o -> o.getClass().isAssignableFrom(clazz))
                .findFirst()
                .orElseGet(() -> factory.call(imageDisplay));
    }

    public void addOverlay(ImageDisplay imageDisplay, List<? extends Overlay> overlays) {

        imageDisplay.addAll(overlays.stream().map(o -> imageDisplayService.createDataView(o)).collect(Collectors.toList()));
        imageDisplay.update();
        overlays.stream().map(o -> new OverlayCreatedEvent(o)).forEach(eventService::publish);

    }

    public void addOverlay(ImageDisplay imageDisplay, Overlay[] overlayArray) {
        addOverlay(imageDisplay, Lists.newArrayList(overlayArray));
    }

    public void removeAllOverlay(ImageDisplay imageDisplay) {

        removeOverlay(imageDisplay, overlayService.getOverlays(imageDisplay));

    }

    public void removeOverlay(ImageDisplay imageDisplay, List<? extends Overlay> overlays) {

        List<DataView> dataviewList = overlays.stream()
                .parallel()
                .map(o -> imageDisplay.stream().filter(view -> view.getData() == o).findFirst().orElse(null))
                .filter(o -> o != null)
                .collect(Collectors.toList());

        imageDisplay.removeAll(dataviewList);
        overlays.forEach(o -> eventService.publish(new OverlayDeletedEvent(o)));
        imageDisplay.update();

    }

    private ImageDisplay findDisplay(File file) {

        return imageDisplayService.getImageDisplays().stream()
                .filter(display -> imageDisplayService.getActiveDataset(display).getSource().equals(file.getAbsolutePath()))
                .findFirst()
                .orElse(null);

    }

    /*
        Binary mask related operation
     */
    public Img<BitType> extractBinaryMask(ImageDisplay imageDisplay) {
        BinaryMaskOverlay maskOverlay = findOverlayOfType(imageDisplay, BinaryMaskOverlay.class);
        return extractBinaryMask(maskOverlay);
    }

    public Img<BitType> extractBinaryMask(BinaryMaskOverlay overlay) {
        return ((BinaryMaskRegionOfInterest<BitType, Img<BitType>>) overlay.getRegionOfInterest()).getImg();
    }

    public void updateBinaryMask(ImageDisplay imageDisplay, Img<BitType> mask) {

        if (mask == null) {
            return;
        }
        BinaryMaskOverlay overlay = findOverlayOfType(imageDisplay, BinaryMaskOverlay.class);

        if (overlay == null) {
            overlay = createBinaryMaskOverlay(imageDisplay, mask);
        } else {

            BinaryMaskRegionOfInterest regionOfInterest = (BinaryMaskRegionOfInterest) overlay.getRegionOfInterest();

            RandomAccessibleInterval<BitType> img = regionOfInterest.getImg();
            RandomAccess<BitType> randomAccess = img.randomAccess();
            Cursor<BitType> cursor = mask.cursor();

            cursor.reset();
            while (cursor.hasNext()) {
                cursor.fwd();
                randomAccess.setPosition(cursor);
                randomAccess.get().set(cursor.get());
            }

        }
    }

    public BinaryMaskOverlay createBinaryMaskOverlay(ImageDisplay imageDisplay, Img<BitType> mask) {

        BinaryMaskOverlay overlay = new BinaryMaskOverlay(getContext(), new BinaryMaskRegionOfInterest<>(mask));
        overlayService.addOverlays(imageDisplay, Arrays.asList(overlay));
        return overlay;

    }

}
