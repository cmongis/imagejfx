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

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectModifierService;
import ijfx.core.project.imageDBService.PlaneDB;
import mongis.utils.FXUtilities;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import org.scijava.Context;
import ijfx.ui.project_manager.ProjectManagerUtils;

/**
 *
 * @author Cyril Quinton
 */
public class ImageContextMenu extends ContextMenu {

    private final PlaneDB plane;
    
    private final Project project;
    private final Context context;

    public ImageContextMenu(PlaneDB plane,Project project,Context context) {
        this.plane = plane;
        this.project = project;
        this.context = context;
        ResourceBundle rb = FXUtilities.getResourceBundle();
        FontAwesomeIconView folderIcon = new FontAwesomeIconView();
        folderIcon.setGlyphName("FOLDER");
        MenuItem showImageInFolderItem = new MenuItem(rb.getString("showImageInFolder"), folderIcon);
        showImageInFolderItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                showFolderAction();
            }
        });
        FontAwesomeIconView removeIcon = ProjectManagerUtils.getRemoveIcon();
        MenuItem removeImageItem = new MenuItem(rb.getString("removeImage"), removeIcon);
        removeImageItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
           removeImageAction(); }
        });
        this.getItems().addAll(showImageInFolderItem,removeImageItem);
    }

   

    private void showFolderAction()  {
       
        File file = plane.getFile();
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(file.getParentFile());
        fc.setInitialFileName(file.getName());
        fc.showOpenDialog(null);
       
    }
    private void removeImageAction() {
        ProjectModifierService service = context.getService(ProjectModifierService.class);
        service.removePlaneFromProject(project, plane);
    }

}
