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
package ijfx.service.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.service.batch.SegmentationService;
import ijfx.service.ui.RichTextDialog;
import ijfx.service.ui.UIExtraService;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.main.ImageJFX;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowIOService;
import ijfx.service.workflow.WorkflowService;
import ijfx.service.workflow.WorkflowStep;
import ijfx.service.workflow.json.WorkflowMapperModule;
import ijfx.ui.UiContexts;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.explorer.ExplorerActivity;
import ijfx.ui.explorer.FolderManagerService;
import ijfx.ui.plugin.panel.WorkflowRequest;
import ijfx.ui.widgets.FXRichTextDialog;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplayService;
import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.command.CommandService;
import org.scijava.event.EventService;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * TODO : move load and save operation to the WorkflowIOService
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class, priority = Priority.VERY_LOW_PRIORITY, description = "Service storing history of modules ran the user. Allow to play, replay, load or save. Uses JavaFX components.")
public class HistoryService extends AbstractService implements ImageJService {

    Workflow currentWorkflow = new DefaultWorkflow();

    final protected ObservableList<WorkflowStep> stepList = FXCollections.observableArrayList();

    final protected ObservableList<WorkflowStep> favoriteList = FXCollections.observableArrayList();

    @Parameter
    WorkflowService workflowService;

    @Parameter
    ModuleService moduleService;

    @Parameter
    CommandService commandService;

    @Parameter
    Context context;

    @Parameter
    WorkflowIOService workflowIOService;

    @Parameter
    UIExtraService uiExtraService;

    @Parameter
    SegmentationService segmentationService;

    @Parameter
    ActivityService activityService;

    @Parameter
    UiContextService uiContextService;

    @Parameter
    EventService eventService;

    @Parameter
    FolderManagerService folderManagerService;

    @Parameter
    ImageDisplayService imageDisplayService;

    boolean enabled;

    public HistoryService() {
        super();

        currentWorkflow.setStepList(stepList);

        try {
            loadFavorite();
        } catch (Exception e) {
            ImageJFX.getLogger().warning("Error when loading favorites");
        }

    }

    public Workflow getCurrentWorkflow() {
        return currentWorkflow;
    }

    public void setCurrentWorkflow(Workflow currentWorkflow) {
        this.currentWorkflow = currentWorkflow;
        stepList.addAll(currentWorkflow.getStepList());
    }

    public ObservableList<WorkflowStep> getStepList() {
        return stepList;
    }

    public void playFrom(WorkflowStep step) {
    }

    public void playTo(WorkflowStep step) {
    }

    public void duplicate(WorkflowStep step) {

        // taking the index
        int index = stepList.indexOf(step);

        // duplicating the parameters
        WorkflowStep duplicata = workflowService.duplicate(step);

        // generating a new id
        duplicata.setId(workflowService.generateStepName(stepList, step));

        // adding to the list
        stepList.add(index, duplicata);
    }

    public void loadWorkflow(Workflow workflow) {

        Platform.runLater(() -> {
            stepList.clear();
            stepList.addAll(workflow.getStepList());
        });

        currentWorkflow.setName(workflow.getName());
        currentWorkflow.setDescription(workflow.getDescription());
    }

    public Workflow loadWorkflow(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new WorkflowMapperModule());
            DefaultWorkflow workflow = mapper.readValue(json, DefaultWorkflow.class);
            workflow.getStepList().forEach(step -> context.inject(step));
            loadWorkflow(workflow);

            return workflow;
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, "Error when loading workflow", ex);;
        }
        return null;
    }

    public Workflow loadWorkflow(File file) {
        try {
            return loadWorkflow(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, "File not found.", ex);;
        }
        return null;
    }

    public boolean saveWorkflow(String path) {
        return saveWorkflow(path, currentWorkflow);
    }

    public boolean saveWorkflow(String path, Workflow workflow) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new WorkflowMapperModule());
        try {
            mapper.writeValue(new File(path), workflow);
            return true;
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, "Error when saving worflow. It's possible you don't have the permission.", ex);;
        }
        return false;
    }

    public ObservableList<WorkflowStep> getFavoriteList() {
        return favoriteList;
    }

    public void saveFavorite() {
        saveWorkflow(ImageJFX.getConfigFile(ImageJFX.FILE_FAVORITES), new DefaultWorkflow(getFavoriteList()));

    }

    public void loadFavorite() {
        favoriteList.clear();
        favoriteList.addAll(loadWorkflow(ImageJFX.getConfigFile(ImageJFX.FILE_FAVORITES)).getStepList());
    }

    public void repeatAll() {

        List<WorkflowStep> toExecute = uiExtraService
                .promptChoice(stepList)
                .selectAll()
                .setTitle("Select the steps to repeat")
                .showAndWait();

        new Thread(() -> workflowService.executeWorkflow(new DefaultWorkflow(toExecute))).start();
    }

    public void useWorkflow() throws IOException {

        List<WorkflowStep> toExecute = uiExtraService
                .promptChoice(filter(stepList))
                .selectAll()
                .setTitle("Select the steps to use")
                
                .showAndWait();

        RichTextDialog.Answer answer = new FXRichTextDialog()
                .setContentType(RichTextDialog.ContentType.HTML)
                .setDialogTitle("What would you like to do with your workflow ?")
                .loadContent(this.getClass(), "/WorkflowExplanations.md")
                .addAnswerButton(RichTextDialog.AnswerType.VALIDATE, "Segment objects")
                .addAnswerButton(RichTextDialog.AnswerType.VALIDATE, "Process files")
                .addAnswerButton(RichTextDialog.AnswerType.CANCEL, "Cancel")
                .showDialog();

        if (answer.contains("segment")) {
            uiContextService.enter(UiContexts.SEGMENT);
            activityService.openByType(ExplorerActivity.class);

        } else if (answer.contains("process")) {
            uiContextService.enter(UiContexts.BATCH);
            activityService.openByType(ExplorerActivity.class);
            eventService.publish(
                    new WorkflowRequest()
                            .setGoal(WorkflowRequest.WorkflowGoal.PROCESSING)
                            .setObject(new DefaultWorkflow(toExecute))
            );
            folderManagerService.openImageFolder(imageDisplayService.getActiveImageDisplay());
        }

    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Filter for WorkflowStep that contain Dataset as input and output
     *
     * @param step
     * @return true if the step input and output a dataset
     */
    private List<WorkflowStep> filter(List<WorkflowStep> stepList) {
        return stepList.
                stream()
                .filter(this::isCompatible)
                .collect(Collectors.toList());
    }

    public boolean isCompatible(WorkflowStep step) {

        return moduleService.getSingleInput(step.getModule(), Dataset.class) != null
                && moduleService.getSingleOutput(step.getModule(), Dataset.class) != null;

    }

}
