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

import ijfx.plugins.commands.BinaryToOverlay;
import ijfx.service.IjfxService;
import ijfx.service.batch.SegmentationService.Output;
import ijfx.service.batch.SegmentationService.Source;
import ijfx.service.batch.input.BatchInputBuilder;
import ijfx.service.overlay.io.OverlayIOService;
import ijfx.ui.IjfxEvent;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.imagej.display.ImageDisplayService;
import net.imagej.overlay.Overlay;
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
public class SegmentationService extends AbstractService implements IjfxService{

    @Parameter
    EventService eventService;

    @Parameter
    ExplorerService explorerService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    OverlayIOService overlayIoService;
    
    
    
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
                    .map(builder->builder.getInput())
                    .collect(Collectors.toList());

        } else if (inputSource == Source.EXPLORER_ALL) {
            inputs = explorerService
                    .getItems()
                    .stream()
                    .map(this::prepareExplorable)
                    .map(this::applyOutputConfiguration)
                    .map(builder->builder.getInput())
                    .collect(Collectors.toList());
        } else if (inputSource == Source.IMAGEJ) {
            inputs = Arrays.asList(
                    new BatchInputBuilder(getContext())
                    .from(imageDisplayService.getActiveImageDisplay())
                    .display()
                    .getInput());
        }
        else {
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

    public void saveOverlaysNextToSourceFile(BatchSingleInput input) {
        Overlay[] overlay = BinaryToOverlay.transform(getContext(), input.getDataset(), false);
        try {
            overlayIoService.saveOverlaysNextToFile(Arrays.asList(overlay), new File(input.getSourceFile()));
        } catch (IOException ex) {
            Logger.getLogger(SegmentationService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setOutputFormat(Output outputFormat) {
        this.outputFormat = outputFormat;
    }
    
    
    
    
}
