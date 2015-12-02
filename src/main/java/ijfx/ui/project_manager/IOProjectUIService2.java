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

import mongis.utils.FXUtilities;
import ijfx.core.project.Project;
import ijfx.core.project.DefaultProjectIoService;
import ijfx.core.project.DefaultProjectManagerService;
import ijfx.core.project.imageDBService.ImageReference;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.zip.DataFormatException;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril Quinton
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class IOProjectUIService2 extends AbstractService {

    private ResourceBundle rb = FXUtilities.getResourceBundle();
    @Parameter
    private DefaultProjectIoService projectIo;
    
    
    @Parameter
    private DefaultProjectManagerService pm;
    @Parameter
    private UiContextService contextService;
    
    public IOProjectUIService2() {

    }
    /**
     *
     * @param project
     * @param ownerWindow
     * @return true if a the saving process was tried on a file. That doesn't
     * mean that the saving process went well. It just mean that if no file was
     * assigned to the project, the user selected a regular file.
     * @throws IOException
     */
    boolean saveProject(Project project, Window ownerWindow) throws IOException {
        File file = project.getFile();
        if (file != null) {
            saveProject(project, file);
            return true;

        } else {
            saveProjectAs(project, ownerWindow);

        }
        return false;
    }

    public boolean saveProjectAs(Project project, Window ownerWindow) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(rb.getString("saveProject"));
        fileChooser.getExtensionFilters().addAll(projectExtentionFilter());
        File file = fileChooser.showSaveDialog(ownerWindow);
        if (file != null) {
            if (file.exists() && !(project.getFile() != null && file.equals(project.getFile()))) {

                boolean overwrite = promptFileAlreadyExists(file, ownerWindow);
                if (!overwrite) {
                    return false;
                }
            }
            saveProject(project, file);
            return true;
        }
        return false;
    }

    public void openProject(Window ownerWindow) {
        File file = openProjectFile(ownerWindow, rb.getString("openProject"));
        if (file != null) {
            try {
                projectIo.load(file);
            } catch (IOException ex) {
                ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            } catch (DataFormatException ex) {
                ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            }
        }
    }

    public File openProjectFile(Window ownerWindow, String fileChooserTitle) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(fileChooserTitle);
        fileChooser.getExtensionFilters().addAll(projectExtentionFilter());
        File file = fileChooser.showOpenDialog(ownerWindow);
        return file;
    }

    public boolean promptFileAlreadyExists(File file, Window ownerWindow) {
        FileAlreadyExistsController overWritePrompt = new FileAlreadyExistsController(file);
        Stage stage = ProjectManagerUtils.createDialogWindow(ownerWindow, overWritePrompt, rb.getString("overwrite?"));
        stage.showAndWait();
        return overWritePrompt.overwrite();
    }

    public void removeProject(Project project, Window ownerWindow) {
        if (!project.hasChanged()) {
            pm.removeProject(project);
        } else {
            Stage stage = ProjectManagerUtils.createDialogWindow(ownerWindow, new ExitWithoutSavingPrompt(project, contextService), rb.getString("saveProject?"));
            stage.showAndWait();
        }
    }
    public void openFindLostImageDialogue(List<ImageReference> lostFiles, Window ownerWindow) {
                Stage findStage = ProjectManagerUtils.createDialogWindow(ownerWindow, new FindImageDialogController(lostFiles, contextService.getContext()), rb.getString("findLostImage"));
        findStage.showAndWait();

    }
     public void openChangedImageDialog(List<ImageReference> changedImages, Window ownerWindow) {
        Stage confirmStage = ProjectManagerUtils.createDialogWindow(ownerWindow, new ValidateChangeImageDialogcontroller(changedImages, contextService.getContext()), rb.getString("confirmChangedImage"));
        confirmStage.showAndWait();

    }

    private List<FileChooser.ExtensionFilter> projectExtentionFilter() {
        List<FileChooser.ExtensionFilter> ls = new ArrayList<>();
        ls.add(new FileChooser.ExtensionFilter(rb.getString("projectFormat"), projectIo.getAcceptedFormat()));
        return ls;
    }

    public void saveProject(Project project, File file) throws IOException {
        projectIo.save(project, file);
    }
}
