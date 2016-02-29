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
package ijfx.ui.project_manager.project;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.ProjectModifierService;
import ijfx.core.project.ProjectToImageJService;
import mongis.utils.FXUtilities;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import ijfx.ui.project_manager.ProjectManagerUtils;
import ijfx.ui.service.angular.ProjectServiceBinder;

/**
 *
 * @author Cyril Quinton
 */
public class FolderContextMenu extends ContextMenu {
    
    private TreeItem item;
    
    @Parameter
    ProjectToImageJService imageJService;
    
    @Parameter
    ProjectModifierService projectModifierService;
    
    @Parameter
    ProjectManagerService projectManagerService;
    
    public FolderContextMenu(Context context) {
        ResourceBundle rb = FXUtilities.getResourceBundle();
        FontAwesomeIconView removeIcon = ProjectManagerUtils.getRemoveIcon();
        MenuItem removeImageItem = new MenuItem(rb.getString("removeAllImageInFolder"), removeIcon);
        removeImageItem.setOnAction(this::remove);
        MenuItem openWithImageJ = new MenuItem("Open with ImageJ");
        openWithImageJ.setOnAction(this::open);
        this.getItems().addAll(removeImageItem,openWithImageJ);
        
        context.inject(this);
        
    }
    
    public void open(ActionEvent evnt) {
        imageJService.convert(item);
    }
    
    
    public void remove(ActionEvent event) {
       projectModifierService.removeSubPlanes(projectManagerService.getCurrentProject(), item);
    }
    
    
    
}
