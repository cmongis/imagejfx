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
package ijfx.ui.project_manager.singleimageview;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.project.Project;
import ijfx.core.project.imageDBService.PlaneDB;
import mongis.utils.FXUtilities;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import ijfx.ui.project_manager.ProjectManagerUtils;

/**
 *
 * @author Cyril Quinton
 */
public abstract class  ImageLoadedControllerAbs extends BorderPane implements ImageLoadedController {
     protected final IntegerProperty prefWidthProperty = new SimpleIntegerProperty();
    protected final IntegerProperty prefHeightProperty = new SimpleIntegerProperty();
    protected final PlaneDB plane;
    protected final Project project;
    
    @Parameter
    protected Context context;
    
    
      protected final BooleanProperty loadingProperty = new SimpleBooleanProperty(false);
    protected final BooleanProperty loadedProperty = new SimpleBooleanProperty(false);
    public ImageLoadedControllerAbs(PlaneDB plane,Project project, Context context) {
        
        context.inject(this);
        
        this.plane = plane;
        this.project = project;
        
         loadingProperty.addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    showLoadingIcon();
                }
            }
        });
    }
    abstract protected Pane getImagePane();
   
   
    @Override
    public void setImage(Image image) {
         loadingProperty.set(false);
        loadedProperty.set(true);
        final ImageView imageView = new ImageView(image);
        Runnable r = new Runnable() {
            @Override
            public void run() {

                getImagePane().getChildren().clear();
                getImagePane().getChildren().add(imageView);
            }
        };
        FXUtilities.modifyUiThreadSafe(r);
    }

    @Override
    public BooleanProperty loadingProperty() {
        return loadingProperty;
    }

    @Override
    public BooleanProperty loadedProperty() {
        return loadedProperty;
    }

    @Override
    public int getPrefImageWidth() {
        return prefWidthProperty.get();
    }

    @Override
    public int getPrefImageHeight() {
        return prefHeightProperty.get();
    }


     @Override
    public void setLoadingFailed() {

        loadingProperty.set(false);
        BrokenImageLinkPane linkBrokenController
                = new BrokenImageLinkPane(project, plane, context);
        Runnable r = new Runnable() {

            @Override
            public void run() {
                getImagePane().getChildren().clear();
                getImagePane().getChildren().add(linkBrokenController);
            }
        };
        FXUtilities.modifyUiThreadSafe(r);

    }

    @Override
    public PlaneDB getPlane() {
        return plane;
    }
    
    private void showLoadingIcon() {
        FontAwesomeIconView spinnerIcon = ProjectManagerUtils.getSpinnerIcon();
        Runnable r = new Runnable() {

            @Override
            public void run() {
                getImagePane().getChildren().clear();
                getImagePane().getChildren().add(spinnerIcon);
            }
        };
        FXUtilities.modifyUiThreadSafe(r);
    }
    
}
