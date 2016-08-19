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
package ijfx.ui.plugin.panel;

import ijfx.service.batch.BatchSingleInput;
import ijfx.service.thumb.ThumbService;
import ijfx.service.workflow.WorkflowBuilder;
import ijfx.service.workflow.WorkflowStep;
import ijfx.ui.main.LoadingIcon;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import mongis.utils.CallbackTask;
import net.imagej.Dataset;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class PreviewImageView extends BorderPane {

    LoadingIcon icon = new LoadingIcon(100);

    @Parameter
    Context context;

    @Parameter
    ThumbService thumbService;

    ImageView imageView = new ImageView();

    public PreviewImageView(Context context) {
        super();
        context.inject(this);
        imageView.prefWidth(150);
        imageView.prefHeight(150);
        imageView.maxWidth(150);
        imageView.maxHeight(150);
        imageView.imageProperty().addListener((obs,old,image)->{
       
            if(image != null) {
                setCenter(imageView);
                icon.stop();
            }
           
        });
        
    }

    public void refresh(Dataset dataset, List<WorkflowStep> steps) {
        
        
        setCenter(icon);
        icon.play();
        //imageView.setImage(null);
        
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);
        new WorkflowBuilder(context)
                .addInput(dataset)
                .execute(steps)
                .then(
                        new CallbackTask<BatchSingleInput, Image>()
                        .run(this::onWorkflowOver)
                        .then(image -> imageView.setImage(image))
                )
                .start();

    }

    public Image onWorkflowOver(BatchSingleInput input) {

        return thumbService.getThumb(input.getDataset(), 512,512);

    }

}
