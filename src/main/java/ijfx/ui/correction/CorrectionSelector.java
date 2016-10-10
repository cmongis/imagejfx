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
package ijfx.ui.correction;

import ijfx.ui.widgets.SimpleListCell;
import ijfx.service.thumb.ThumbService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowBuilder;
import ijfx.service.workflow.WorkflowStep;
import ijfx.ui.activity.Activity;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.save.DefaultSaveOptions;
import ijfx.ui.save.SaveOptions;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongis.utils.CallableTask;
import mongis.utils.FXUtilities;
import mongis.utils.FluidWebViewWrapper;
import net.imagej.Dataset;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

/**
 *
 * @author cyril
 */
@Plugin(type = Activity.class, name = "correction-activity-2")
public class CorrectionSelector extends BorderPane implements Activity {

    private List<PluginInfo<CorrectionUiPlugin>> pluginList;

    @FXML
    Accordion accordion;

    @FXML
    MenuButton correctionListButton;

    @FXML
    Button destinationFolderButton;

    @FXML
    Button startCorrectionButton;

    @FXML
    Button testCorrectionButton;

    @FXML
    ListView<Explorable> listView;

    @FXML
    ImageView previewImageView;

    @FXML
    VBox rightVBox;
    
    @Parameter
    Context context;

    @Parameter
    DatasetIOService datasetIoService;

    @Parameter
    PluginService pluginService;

    @Parameter
    CorrectionUiService correctionUiService;

    @Parameter
    ThumbService thumbService;

    @Parameter
    LoadingScreenService loadingScreenService;

    private final ObservableList<CorrectionUiPlugin> addedPlugins = FXCollections.observableArrayList();

    private final BooleanProperty allPluginsValid = new SimpleBooleanProperty(false);

    private final Property<Workflow> workflowProperty = new SimpleObjectProperty();

    private final ObjectProperty<File> destinationFolder = new SimpleObjectProperty<File>();

    private final ObservableList<File> filesToCorrect = FXCollections.observableArrayList();

    private Property<Dataset> exampleDataset = new SimpleObjectProperty();

    private SaveOptions options = new DefaultSaveOptions();
    
    boolean hasInit = false;

    public CorrectionSelector() {
        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/correction/CorrectionSelector.fxml");

            listView.setCellFactory(SimpleListCell.createFactory(Explorable::getTitle));

        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void init() {

        pluginList = pluginService.getPluginsOfType(CorrectionUiPlugin.class);

        correctionListButton.getItems().addAll(pluginList.stream().map(this::createMenuItem).collect(Collectors.toList()));

       setTop(new FluidWebViewWrapper()
               .withHeight(150)
               .forMDFiles()
               .display(this,"CorrectionSelector.md")
       );

        // listening for the list to start or stop listening for the plugins
        addedPlugins.addListener(this::onAddedPluginListChanged);

        // binding the buttons
        startCorrectionButton.disableProperty().bind(allPluginsValid.not().or(destinationFolder.isNull()));
        testCorrectionButton.disableProperty().bind(allPluginsValid.not());

        listView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedItemChanged);
        
        destinationFolder.bind(options.folder());
        
        rightVBox.getChildren().add(0,options.getContent());
         hasInit = true;
    }

    private void onAddedPluginListChanged(ListChangeListener.Change<? extends CorrectionUiPlugin> change) {
        while (change.next()) {
            change.getAddedSubList().forEach(this::listenToPlugin);
            change.getRemoved().forEach(this::stopListeningToPlugin);
        }
    }

    private void listenToPlugin(CorrectionUiPlugin plugin) {
        plugin.workflowProperty().addListener(this::onWorkflowModified);
    }

    private void stopListeningToPlugin(CorrectionUiPlugin plugin) {
        plugin.workflowProperty().addListener(this::onWorkflowModified);
    }

    private void onWorkflowModified(Observable observable, Workflow previous, Workflow newWorkflow) {
        Platform.runLater(this::updateFinalWorkflow);
    }

    private void updateFinalWorkflow() {
        checkValidity();
        compileWorkflow();
    }

    private void compileWorkflow() {
        if (allWorkflowAreValid()) {
            List<WorkflowStep> allSteps = addedPlugins
                    .stream()
                    .flatMap(plugin -> plugin.workflowProperty().getValue().getStepList().stream())
                    .collect(Collectors.toList());
            workflowProperty.setValue(new DefaultWorkflow(allSteps));
        }
    }

    private boolean allWorkflowAreValid() {
        return allPluginsValid.getValue();
    }

    private void checkValidity() {
        allPluginsValid.setValue(addedPlugins.stream().filter(plugin -> plugin.workflowProperty().getValue() != null).count() == addedPlugins.size());
    }

    private MenuItem createMenuItem(PluginInfo<CorrectionUiPlugin> infos) {
        MenuItem item = new MenuItem(infos.getLabel());
        item.setOnAction(event -> addCorrection(infos));
        return item;
    }

    public void addCorrection(PluginInfo<CorrectionUiPlugin> infos) {

        CorrectionUiPlugin createInstance = pluginService.createInstance(infos);
        createInstance.init();
        createInstance.exampleDataset().bind(exampleDataset);
        accordion.getPanes().add(new CorrectionUiPluginWrapper(createInstance).deleteUsing(this::removeCorrection));
        addedPlugins.add(createInstance);

    }

    public void removeCorrection(CorrectionUiPlugin correction) {
        accordion.getPanes().remove(getWrapper(correction));
    }

    public CorrectionUiPluginWrapper getWrapper(CorrectionUiPlugin correctionPlugin) {
        return accordion.getPanes().stream().map(pane -> (CorrectionUiPluginWrapper) pane).filter(pane -> pane.getPlugin() == correctionPlugin).findFirst().orElse(null);
    }

    @Override
    public Node getContent() {
        if (!hasInit) {
            init();
        }
        return this;
    }

    @Override
    public Task updateOnShow() {

        return new CallableTask<List<? extends Explorable>>()
                .setCallable(correctionUiService::getSelectedObjects)
                .then(this::setItems);

    }

    protected void setItems(List<? extends Explorable> list) {

        listView.getItems().clear();
        listView.getItems().addAll(list);

        listView.getSelectionModel().select(0);
    }

    public Void nothing() {
        return null;
    }

    @FXML
    public void startCorrection() {
        new WorkflowBuilder(context)
                .addInputFiles(filesToCorrect)
                .and(input -> input.saveIn(destinationFolder.getValue()))
                .execute(workflowProperty.getValue())
                .startAndShow();

    }

    @FXML
    public void testCorrection() {
        new WorkflowBuilder(context)
                .addInput(exampleDataset.getValue())
                .and(input -> input.display())
                .execute(workflowProperty.getValue())
                .startAndShow();
    }

    private void onSelectedItemChanged(Observable obs, Explorable oldValue, Explorable newValue) {
        if (newValue != null) {
            new CallableTask<Dataset>()
                    .setCallable(newValue::getDataset)
                    .then(this::onDatasetLoaded)
                    .submit(loadingScreenService)
                    .start();

        }

    }

    private void onDatasetLoaded(Dataset dataset) {
        exampleDataset.setValue(dataset);
        Image thumb = thumbService.getThumb(dataset, 150, 150);
        previewImageView.setImage(thumb);
    }

}