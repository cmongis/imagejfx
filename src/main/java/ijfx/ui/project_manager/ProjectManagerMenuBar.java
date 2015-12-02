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

import ijfx.ui.project_manager.project.BrowserUIService;
import ijfx.ui.main.Localization;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import mongis.utils.FXUtilities;
import ijfx.ui.UiPlugin;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiContexts;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type=UiPlugin.class)
@UiConfiguration(id="project-manager-menubar",localization = Localization.TOP_CENTER, context=UiContexts.PROJECT_MANAGER)
public class ProjectManagerMenuBar extends MenuBar implements UiPlugin{
    
    @Parameter
    BrowserUIService projectUIService;

    public ProjectManagerMenuBar() throws IOException {
        super();
        FXUtilities.injectFXML(this);
    
    }
    
    
    
    
    @FXML
    public void newProject() {
        projectUIService
                .newProject();
    }
    
    @FXML
    public void addImagesFromFiles() {
        projectUIService.addImageAction();
    }
    
    @FXML
    public void addImagesFromFolder() {
        projectUIService.addImageFromDirectoryAction();
    }
    
    @FXML
    public void openProject() {
        projectUIService.openProject();
    }
    
    @FXML
    public void undo() {
        projectUIService.undo();
    }
    
    @FXML
    public void redo() {
        projectUIService.redo();
    }
    
    @FXML
    public void saveProject() {
        projectUIService.saveProject();
    }
    
    @FXML
    public void saveProjectAs() {
        projectUIService.saveProjectAs();
    }
    
    @FXML
    public void checkReferences() {
        projectUIService.checkFileReference();
    }
    
    @FXML void editAnnotationRules() {
        System.err.println("Nothing implemented !");
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {
        return this;
    }
    
    
}
