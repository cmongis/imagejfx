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

import ijfx.bridge.ImageJContainer;
import ijfx.core.metadata.MetaData;
import ijfx.service.ImagePlaneService;
import ijfx.service.batch.input.BatchInputBuilder;
import ijfx.ui.widgets.SimpleListCell;
import ijfx.service.thumb.ThumbService;
import ijfx.service.ui.HintService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.service.workflow.MyWorkflowService;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowBuilder;
import ijfx.service.workflow.WorkflowStep;
import ijfx.ui.activity.Activity;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.save.DefaultSaveOptions;
import ijfx.ui.save.SaveOptions;
import ijfx.ui.save.SaveType;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
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
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongis.utils.CallableTask;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import mongis.utils.FluidWebViewWrapper;
import net.imagej.Dataset;
import org.scijava.Context;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS, 2016
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
    BorderPane centerTopBorderPane;

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

    @Parameter
    ActivityService activityService;

    @Parameter
    UIService uiService;

    @Parameter
    HintService hintService;

    @Parameter
    DisplayService displayService;

    @Parameter
    ImagePlaneService imagePlaneService;

    @Parameter
    MyWorkflowService myWorkflowService;
    
    private final ObservableList<CorrectionUiPlugin> addedPlugins = FXCollections.observableArrayList();

    private final BooleanProperty allPluginsValid = new SimpleBooleanProperty(false);

    private final Property<Workflow> workflowProperty = new SimpleObjectProperty();

    private final ObjectProperty<File> destinationFolder = new SimpleObjectProperty<File>();

    private final ObservableList<File> filesToCorrect = FXCollections.observableArrayList();

    private Binding<CorrectionUiPluginWrapper> expendedWrapper;

    private Property<Dataset> exampleDataset = new SimpleObjectProperty();

    private SaveOptions options = new DefaultSaveOptions();

    boolean hasInit = false;

    public static final String DELETE = "delete";
    public static final String MOVE_UP = "move-up";
    public static final String MOVE_DOWN = "move-down";

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

        centerTopBorderPane.setCenter(new FluidWebViewWrapper()
                .withHeight(90)
                .withNoOverflow()
                .forMDFiles()
                .display(this, "CorrectionSelector.md")
        );

        // listening for the list to start or stop listening for the plugins
        addedPlugins.addListener(this::onAddedPluginListChanged);

        // binding the buttons
        startCorrectionButton.disableProperty().bind(allPluginsValid.not().or(destinationFolder.isNull()));
        testCorrectionButton.disableProperty().bind(allPluginsValid.not());

        listView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedItemChanged);

        destinationFolder.bind(options.folder());

        rightVBox.getChildren().add(0, options.getContent());
        hasInit = true;

        expendedWrapper = Bindings.createObjectBinding(this::getExpendedWrapper, accordion.expandedPaneProperty());

        expendedWrapper.addListener(this::onExpendedPaneChanged);

    }

    private void onAddedPluginListChanged(ListChangeListener.Change<? extends CorrectionUiPlugin> change) {
        while (change.next()) {
            change.getAddedSubList().forEach(this::listenToPlugin);
            change.getRemoved().forEach(this::stopListeningToPlugin);
        }
        Platform.runLater(this::updateFinalWorkflow);
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

    private void onExpendedPaneChanged(Observable observable, CorrectionUiPluginWrapper pane, CorrectionUiPluginWrapper newlySelected) {
        if (newlySelected == null) {
            return;
        }

        hintService.displayHints(newlySelected.getPlugin().getClass(), false);

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
        int addedPluginSize = addedPlugins.size();
        long validPlugins = addedPlugins.stream().filter(plugin -> plugin.workflowProperty().getValue() != null).count();

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
        accordion.getPanes().add(new CorrectionUiPluginWrapper(createInstance).setActionHandler(this::handleAction));
        addedPlugins.add(createInstance);

    }

    public void handleAction(String action, CorrectionUiPlugin correction) {

        if (DELETE.equals(action)) {
            addedPlugins.remove(correction);
            accordion.getPanes().remove(getWrapper(correction));

        }
        if (MOVE_UP.equals(action)) {
            move(correction, -1);
        }
        if (MOVE_DOWN.equals(context)) {
            move(correction, 1);
        }

    }

    private void move(CorrectionUiPlugin plugin, int factor) {
        CorrectionUiPluginWrapper wrapper = getWrapper(plugin);
        int i = accordion.getPanes().indexOf(wrapper);
        Collections.swap(accordion.getPanes(), i, i + factor);
        Collections.swap(addedPlugins, i, i + factor);

    }

    private CorrectionUiPluginWrapper getWrapper(CorrectionUiPlugin correctionPlugin) {
        return accordion.getPanes().stream().map(pane -> (CorrectionUiPluginWrapper) pane).filter(pane -> pane.getPlugin() == correctionPlugin).findFirst().orElse(null);
    }

    private CorrectionUiPluginWrapper getWrapper(TitledPane titledPane) {
        return (CorrectionUiPluginWrapper) titledPane;
    }

    private CorrectionUiPluginWrapper getExpendedWrapper() {
        return getWrapper(accordion.getExpandedPane());
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
                .addInputFiles(correctionUiService.getSelectedFiles())
                .and(this::applySaveParameter)
                .execute(workflowProperty.getValue())
                .startAndShow()
                .then(this::onWorkflowOver)
                .error(this::onError);

    }

    public void applySaveParameter(BatchInputBuilder builder) {

        if (options.saveType().getValue() == SaveType.NEW) {
            builder.saveIn(options.folder().getValue(), options.suffix().getValue());
        } else {
            builder.saveIn(new File(builder.getInput().getSourceFile()));
        }
    }

    private Explorable getSelectedItem() {
        return listView.getSelectionModel().getSelectedItem();
    }
    
    @FXML
    public void testCorrection() {
        new WorkflowBuilder(context)
                .addInput(new File(getSelectedItem().getMetaDataSet().get(MetaData.ABSOLUTE_PATH).getStringValue()))
                .and(input -> input.displayWithSuffix("corrected"))
                .execute(workflowProperty.getValue())
                .startAndShow()
                .then(this::onSuccessTest)
                .error(this::onError);

    }

    public void onError(Throwable e) {
        uiService.showDialog("Error when executing test correction", DialogPrompt.MessageType.ERROR_MESSAGE);
        uiService.show(e);
    }

    public void onSuccessTest(Boolean result) {
        if (result) {

            activityService.openByType(ImageJContainer.class);
            uiService.show(exampleDataset.getValue());
        }
    }

    @FXML
    public void back() {
        activityService.openByType(FolderSelection.class);
    }

    private void onSelectedItemChanged(Observable obs, Explorable oldValue, Explorable newValue) {
        if (newValue != null) {
            new CallbackTask<Explorable, Dataset>()
                    .setInput(newValue)
                    .run(this::loadDataset)
                    .submit(loadingScreenService)
                    .then(this::onDatasetLoaded)
                    .start();

        }

    }

    private Dataset loadDataset(Explorable explorable) {
        String path = explorable.getMetaDataSet().getOrDefault(MetaData.ABSOLUTE_PATH, MetaData.NULL).getStringValue();
        if ("null".equals(path) || path == null) {
            return explorable.getDataset();
        } else {
            try {
                return imagePlaneService.openVirtualDataset(new File(path));
            } catch (IOException ioe) {
                return null;
            }
        }
    }

    private void onDatasetLoaded(Dataset dataset) {
        exampleDataset.setValue(dataset);

        new CallableTask<Image>()
                .setCallable(() -> thumbService.getThumb(dataset, 150, 150))    
                .then(previewImageView::setImage)
                .start();

        // return thumb;// previewImageView.setImage(thumb);
    }
    
    public void onWorkflowOver(Object result) {
        if(Boolean.TRUE.equals(result)) {
            DialogPrompt.Result answer = uiService.showDialog("Your worklow was successfully applied to each file. Do you want to save the worklow for later use ?",DELETE, DialogPrompt.MessageType.ERROR_MESSAGE, DialogPrompt.OptionType.YES_NO_OPTION);
            if(answer == DialogPrompt.Result.YES_OPTION) {
                
                myWorkflowService.exportWorkflow(workflowProperty.getValue());
                uiService.showDialog("Workflow saved.");

            }
            
        }
    }
    
    
    

}
