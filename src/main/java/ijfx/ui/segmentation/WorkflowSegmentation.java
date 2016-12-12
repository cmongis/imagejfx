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
package ijfx.ui.segmentation;

import ijfx.service.ui.LoadingScreenService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowBuilder;
import ijfx.service.workflow.WorkflowStep;
import ijfx.ui.segmentation.threshold.AbstractSegmentation;
import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Task;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
public class WorkflowSegmentation extends AbstractSegmentation {

    @Parameter
    Context context;

    @Parameter
    LoadingScreenService loadingService;

    @Parameter
    UIService uiService;

    public List<WorkflowStep> stepList = new ArrayList<>();

    public WorkflowSegmentation(ImageDisplay imageDisplay) {
        imageDisplay.getContext().inject(this);
        setImageDisplay(imageDisplay);
       
        
        
    }

    @Override
    public void preview(ImageDisplay imageDisplay) {

    }

    @Override
    public Workflow getWorkflow() {
        return new DefaultWorkflow(stepList);
    }

    public void reprocess(List<WorkflowStep> steps) {
        Task task = new WorkflowBuilder(context)
                .addInput(getImageDisplay().get())
                .execute(steps)
                .then(output -> {
                    Dataset dataset = output.getDataset();
                    if (dataset.getType().getBitsPerPixel() == 1) {
                        setMask((Img<BitType>) dataset.getImgPlus().getImg());
                    } else {
                        uiService.showDialog("Your workflow should result into a binary image.");

                    }
                })
                .start();
        loadingService.frontEndTask(task, true);
    }

}
