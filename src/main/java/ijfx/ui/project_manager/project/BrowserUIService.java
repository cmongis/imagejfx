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

import ijfx.core.project.FileReferenceStatus;
import ijfx.core.project.ImageLoaderService;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectIoService;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.DefaultProjectModifierService;
import ijfx.core.project.command.Invoker;
import ijfx.ui.project_manager.ProjectManager;
import ijfx.ui.project_manager.IOProjectUIService2;
import ijfx.ui.main.ImageJFX;
import ijfx.service.ui.LoadingScreenService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import net.imagej.ImageJService;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import mongis.utils.FXUtilities;

/**
 * Service that hold the UI actions for browser
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class, priority = Priority.VERY_LOW_PRIORITY - 10)
public class BrowserUIService extends AbstractService implements ImageJService {

    /* Services */
    @Parameter
    private ProjectIoService projectIo;

    @Parameter
    private ImageLoaderService imageLoader;

    @Parameter
    private ProjectManagerService projectManager;

    @Parameter
    private LoadingScreenService loadingScreenService;

    @Parameter
    private IOProjectUIService2 saveProjectService;

    @Parameter
    private DefaultProjectModifierService projectModifierService;

    private final ResourceBundle rb = ImageJFX.getResourceBundle();

    ChangeListener<String> undoCmdNameListener = (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
        setUndoCmdName(newValue);
    };
    ChangeListener<String> redoCmdNameListener = (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
        setRedoCmdName(newValue);
    };

    @Override
    public void initialize() {
        super.initialize();

        projectManager.currentProjectProperty().addListener(this::updateListenedProject);

    }

    // creates a new project
    public void newProject() {
        projectIo.createProject();
    }

    private final BooleanProperty undoDisableProperty = new SimpleBooleanProperty();
    private final BooleanProperty redoDisableProperty = new SimpleBooleanProperty();
    private final StringProperty undoMessageProperty = new SimpleStringProperty();
    private final StringProperty redoMessageProperty = new SimpleStringProperty();

    /**
     * shows a file chooser with the correct file filter with the multiple
     * selection available, retrieve the list of picked files by the user and
     * launch a new task in a separated thread to import those images in the
     * current project. The status of the task is tracked by the
     * loadingScreenService.
     */
    public void addImageAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(rb.getString("chooseImage"));
        List<String> formatList = imageLoader.getAcceptedFormat();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(rb.getString("allImageFiles"), formatList));
        List<File> files = fileChooser.showOpenMultipleDialog(null);
        if (files != null) {
            try {
                
                if(projectManager.getCurrentProject() == null) {
                    newProject();
                }
                
                Task task = imageLoader.loadImageFromFile(files, projectManager.currentProjectProperty().get());
                runLoadingImageTask(task);
            } catch (IOException ex) {
                ImageJFX.getLogger().log(Level.SEVERE,null,ex);;
            }
        }
    }

    /**
     * Shows a directory chooser, get the directory picked by the user and run a
     * task in a separated thread that import every image file found in this
     * directory recursively in the current project. The status of the task is
     * tracked by the loadingScreenService.
     */
    public void addImageFromDirectoryAction() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle(rb.getString("selectRootFolder"));
        File dir = dirChooser.showDialog(null);
        if (dir != null) {
            try {
                Task task = imageLoader.loadImageFromDirectory(dir, projectManager.currentProjectProperty().get());
                runLoadingImageTask(task);
            } catch (IOException ex) {
                ImageJFX.getLogger().log(Level.SEVERE,null,ex);;
            }
        }
    }

    private void runLoadingImageTask(Task task) {
        loadingScreenService.submitTask(task);
        runTask(task);
    }

    private void runBackgroundTask(Task task) {
        loadingScreenService.backgroundTask(task, true);
        runTask(task);
    }

    
    private void runTask(Task task) {
        ImageJFX.getThreadPool().submit(task);

    }

    // open a project
    public void openProject() {
        saveProjectService.openProject(null);

    }

    // save the current project
    public void saveProject() {
        if (projectManager.currentProjectProperty() != null) {
            Project project = projectManager.currentProjectProperty().get();
            try {
                saveProjectService.saveProject(projectManager.currentProjectProperty().get(), project.getFile());
            } catch (IOException ex) {
                ImageJFX.getLogger();
            }

        }
    }

    // saves the current project as
    public void saveProjectAs() {

        try {
            saveProjectService.saveProjectAs(projectManager.currentProjectProperty().get(), null);
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE,null,ex);;
        }

    }

    // undo the last action of the current project
    public void undo() {
        projectManager.currentProjectProperty().get().getInvoker().undo();
    }

    // redo the last action of the current project
    public void redo() {
        projectManager.currentProjectProperty().get().getInvoker().redo();
    }

    // selected all the images of the current project
    public void selectAll() {
        projectModifierService.selectAll(projectManager.currentProjectProperty().get());
    }

    // deselect all the images of the current project
    public void deselectAll() {
        projectModifierService.deselectAll(projectManager.currentProjectProperty().get());
    }

    // start a task that check if the images in the project exists on the disk of not
    public void checkFileReference() {
        Task<FileReferenceStatus> task = imageLoader.checkFileReferenceTask(projectManager.currentProjectProperty().get());
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {
                dealWithNewFileReferenceStatus(task.getValue());
            }
        });
        runBackgroundTask(task);

    }

    // launch a dialog that allows user to indicate where the files have possibly been moved to
    private void dealWithNewFileReferenceStatus(FileReferenceStatus status) {
        if (!status.isOK()) {
            Project project = status.getProject();
            if (projectManager.getProjects().contains(project)) {
                //if the current project has chande during the execution of the thread
                //the current project is setted back to the project related to the FileReferenceStatus object
                projectManager.setCurrentProject(project);
                if (!status.getWrongPathList().isEmpty()) {
                    saveProjectService.openFindLostImageDialogue(status.getWrongPathList(), null);
                }
                if (!status.getIncorrectIDList().isEmpty()) {
                    saveProjectService.openChangedImageDialog(status.getIncorrectIDList(), null);
                }
            } else {
                //do nothing, the project was closed by the user
                ImageJFX.getLogger().log(Level.WARNING, "a project was closed before the "
                        + "file reference checking ended");
            }
        } else {
            ImageJFX.getLogger().info("File refererenced ok");
        }
    }

    //  change which project is listened by the service
    private void updateListenedProject(Observable obs, Project oldVal, Project newVal) {
        if (oldVal != null) {
            undoDisableProperty.unbind();
            redoDisableProperty.unbind();
            Invoker invoker = oldVal.getInvoker();
            invoker.undoNameProperty().removeListener(undoCmdNameListener);
            invoker.redoNameProperty().removeListener(redoCmdNameListener);
        }
        if (newVal != null) {
            Invoker invoker = newVal.getInvoker();
            undoDisableProperty.bind(invoker.undoDisableProperty());
            redoDisableProperty.bind(invoker.redoDisableProperty());
            invoker.undoNameProperty().addListener(undoCmdNameListener);
            invoker.redoNameProperty().addListener(redoCmdNameListener);
        }
    }

    private void setUndoCmdName(String name) {
        undoMessageProperty.set(name == null ? "" : rb.getString("undo") + " " + name);
    }

    private void setRedoCmdName(String name) {
        redoMessageProperty.set(name == null ? "" : rb.getString("redo") + " " + name);
    }
}
