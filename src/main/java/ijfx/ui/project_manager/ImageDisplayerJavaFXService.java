/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.ui.project_manager;

import ijfx.core.project.Project;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.service.thumb.ThumbService;
import ijfx.ui.project_manager.singleimageview.BrokenImageLinkPane;
import ijfx.ui.project_manager.singleimageview.ImageLoadedController;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.scijava.Context;

/**
 *
 * @author Cyril Quinton
 */
public class ImageDisplayerJavaFXService extends Service<Void> {

    private final Project project;
    private final List<ImageLoadedController> imageCtrlList;
    private final Context context;
    
    
    private final ThumbService thumbService;

    public  ImageDisplayerJavaFXService(Project project, List<ImageLoadedController> imageCtrl, Context context) {
        this.project = project;
        this.imageCtrlList = imageCtrl;
        this.context = context;
        thumbService = context.getService(ThumbService.class);
        this.setExecutor(ImageJFX.getThreadPool());
    }
    public ImageDisplayerJavaFXService(Project project, ImageLoadedController imageCtrl, Context context) {
        this(project, Arrays.asList(imageCtrl), context);
    }
   

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                Iterator<ImageLoadedController> itImageCtrl = imageCtrlList.iterator();
                while (itImageCtrl.hasNext() && !this.isCancelled()) {
                    ImageLoadedController imageCtrl = itImageCtrl.next();
                    PlaneDB plane = imageCtrl.getPlane();
                    File imageFile = plane.getFile();
                    long planeIndex = plane.getPlaneIndex();
                    try {
                        int prefWidth = (int) imageCtrl.getPrefImageWidth();
                        int prefHeight = (int) imageCtrl.getPrefImageHeight();
                        Image image = thumbService.getThumb(imageFile, (int) planeIndex, prefWidth, prefHeight);
                        imageCtrl.setImage(image);
                    } catch (IOException ex) {
                        imageCtrl.setLoadingFailed();
                    } 
                }
                return null;
            }

        };

    }
}
