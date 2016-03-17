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
package ijfx.ui.batch;

import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.service.batch.BatchService;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.PlaneDBBatchInput;
import ijfx.ui.main.ImageJFX;
import ijfx.service.uicontext.UiContextService;
import ijfx.service.workflow.MyWorkflowService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.service.workflow.DefaultWorkflowStep;
import ijfx.service.workflow.WorkflowStep;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import org.controlsfx.control.textfield.TextFields;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.menu.MenuService;
import org.scijava.menu.ShadowMenu;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import mongis.utils.FXUtilities;
import ijfx.ui.UiPlugin;
import ijfx.ui.UiContexts;
import ijfx.ui.activity.Activity;
import ijfx.ui.project_manager.projectdisplay.PlaneSet;
import ijfx.ui.project_manager.projectdisplay.ProjectDisplay;
import ijfx.ui.project_manager.projectdisplay.ProjectDisplayActived;
import ijfx.ui.project_manager.projectdisplay.ProjectDisplayService;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.ComboBox;
import ijfx.ui.context.animated.Animations;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Activity.class,name=UiContexts.PROJECT_BATCH_PROCESSING)
//@UiConfiguration(id = "batch-processor-configurator", context = UiContexts.PROJECT_BATCH_PROCESSING, localization = Localization.CENTER)
public class BatchProcessorConfigurator extends BorderPane implements Activity, ActionHandler<WorkflowStep> {

    /*
     Services
     */
    @Parameter
    private UiContextService contextService;

    @Parameter
    private ModuleService moduleService;

    @Parameter
    private Context context;

    @Parameter
    private CommandService commandService;

    @Parameter
    private MenuService menuService;

    @Parameter
    private BatchService batchService;

    @Parameter
   private MyWorkflowService myWorkflowService;
    
    @Parameter
    private ProjectManagerService projectService;

    @Parameter
    private ProjectDisplayService projectDisplayService;
    
    @Parameter
    private UiContextService uiContextService;
    
    
    /*
     JavaFX Components
     */
    @FXML
    private GridPane gridPane;

   

    @FXML
    private Button saveFolderButton;

    @FXML
    private Label saveFolderLabel;
    
    @FXML
    private ListView<WorkflowStep> stepListView;

    @FXML
    private TextField moduleSearchTextField;

    @FXML
    private Button addButton;

    @FXML
    private MenuButton menuButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

    @FXML
    private Button startButton;

    @FXML
    private ComboBox<PlaneSet> planeSetComboBox;
    
    
    /*
     Properties
     */
    private ObservableList<WorkflowStep> steps = FXCollections.observableArrayList();

    private BooleanProperty isModuleNameValidProperty = new SimpleBooleanProperty(false);

    private ObjectProperty<File> saveFolderProperty = new SimpleObjectProperty<>(null);

    private final String BUTTON_TEXT_DEFAULT_TEXT = "Choose a directory ...";
    private StringProperty buttonText = new SimpleStringProperty("Choose a directory...");

    private ObjectProperty<Task> currentTaskProperty = new SimpleObjectProperty(null);

    IntegerProperty selectedCountProperty = new SimpleIntegerProperty();
    
    private HashMap<String, Runnable> modules = new HashMap<>();

    private SimpleListProperty listProperty = new SimpleListProperty(steps);

    
    
    
   private StringBinding startButtonText = Bindings.createStringBinding(()->{
       if(currentTaskProperty.getValue() == null) return "Start processing";
       else return "Processing...";
   }, currentTaskProperty);

    private BooleanBinding cannotStart = Bindings.createBooleanBinding(()->{
        return (listProperty.getValue() == null 
                || saveFolderProperty.getValue() == null
                || currentTaskProperty.getValue() != null
                );
    }, listProperty,saveFolderProperty,currentTaskProperty);
    
    public BatchProcessorConfigurator() throws IOException {
        super();
       
            FXUtilities.injectFXML(this);
            //setCenter(new Label("Now let's work a bit !"));
       

        saveFolderProperty.addListener(this::onSaveFolderChange);

        saveFolderButton.textProperty().bind(buttonText);

        // the add button is disable if the isValid property is set to false
        addButton.disableProperty().bind(isModuleNameValidProperty.not());

        // bind the start button text to the start button property
        startButton.textProperty().bind(startButtonText);
        
        // the start button can only be used if the canStart property return valid
        // and if there is no task in the currentTaskProperty.
        startButton.disableProperty().bind(cannotStart);


        progressLabel.visibleProperty().bind(currentTaskProperty.isNotNull());
        progressBar.visibleProperty().bind(currentTaskProperty.isNotNull());
        
        currentTaskProperty.addListener(this::onCurrentTaskChanged);
        
        
       
        
        
        
    } 

    public class FadeBinding {

        private final Node node;
        
        
        FadeBinding(Node node, Property<Boolean> property) {

            this.node = node;
            
            if (property.getValue() == false) {
                node.setOpacity(0.0);
            }
            
            property.addListener(this::onPropertyChanged);
            

        }
        
        FadeBinding(Node node, Binding<Boolean> binding) {
            this.node = node;
            node.setOpacity(binding.getValue() ? 1.0 : 0.0);
            binding.addListener(this::onPropertyChanged);
        }
        
        public void onPropertyChanged(Observable observable, Boolean oldValue, Boolean newValue) {
            
            if(newValue) {

                Animations.FADEIN.configure(node, ImageJFX.getAnimationDurationAsDouble()).play();
            }
            else {
                Animations.FADEOUT.configure(node, ImageJFX.getAnimationDurationAsDouble()).play();
            }
            
            
        }

    }

    
    public void onObjectChanged(Observable obs, Object oldValue, Object newValue) {

    }
    
    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public Task updateOnShow() {
       
        
        stepListView.setCellFactory(newCell -> new DraggableStepCell(context, s -> execute(s)));

        stepListView.setItems(steps);

       
        stepListView.setEditable(true);

        registerAction(menuService.getMenu());

        TextFields.bindAutoCompletion(moduleSearchTextField, modules.keySet());

        moduleSearchTextField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {


            if (event.getCode() == KeyCode.ENTER) {
                add();
            }
        });

        moduleSearchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
            // set true if a module with this name exists
            isModuleNameValidProperty.setValue(modules.containsKey(newValue));
        });

        menuService.createMenus(new ChoiceBoxMenuCreator(this::onAddMenuButtonClicked), menuButton);

         //projectService.currentProjectProperty().addListener(this::onProjectChanged);
        
        
        
        
        updatePlaneSet();
        
        
        return null;
    }

    @FXML
    public void add() {
        modules.get(moduleSearchTextField.getText()).run();
        moduleSearchTextField.setText("");
    } 
    
    @FXML
    public void backToDatabase() {
        uiContextService
                .leave("batch-processor-configurator");
        uiContextService
                .enter(UiContexts.PROJECT_MANAGER);
        uiContextService.update();
    }
    
    
    public void onAddMenuButtonClicked(ShadowMenu shadowMenu) {
        addStep(shadowMenu.getModuleInfo());
        moduleSearchTextField.setText("");
    }
    
    public void onSaveFolderChange(Observable obs, File oldValue, File newValue) {

        saveFolderLabel.setText(newValue.getParentFile().getName() + " / " + newValue.getName());
    }

    @FXML
    public void changeSaveFolder(ActionEvent event) { 
        DirectoryChooser chooser = new DirectoryChooser();

        File saveFolder = chooser.showDialog(null);

        if (saveFolder != null) {
            saveFolderProperty.setValue(saveFolder);
        }

    }
    
    @org.scijava.event.EventHandler
    public void onProjectDisplayChanged(ProjectDisplayActived event) {
        
        updatePlaneSet();
        System.out.println("plane set changed");
        
    }
            
    
    
    
    private void updatePlaneSet() {
        
        if(projectDisplayService.getActiveProjectDisplay() == ProjectDisplay.NO_DISPLAY) return;
        
        planeSetComboBox.setItems(projectDisplayService.getActiveProjectDisplay().getPlaneSetList());
        planeSetComboBox.getSelectionModel().select(projectDisplayService.getActiveProjectDisplay().getCurrentPlaneSet());
        
        //selectedImageCountProperty.unbind();
        
        
        
       // ListProperty listProperty = new SimpleListProperty(projectDisplayService.getActiveProjectDisplay().getCurrentPlaneSet().getPlaneList());
        selectedCountProperty.bind(Bindings.createIntegerBinding(
                ()->projectDisplayService.getActiveProjectDisplay().getCurrentPlaneSet().getPlaneList().size(),
                projectDisplayService.getActiveProjectDisplay().currentPlaneSetProperty())
                
        );
        
    }
    
    

    @Override
    public void execute(WorkflowStep t) {

        stepListView.getItems().remove(t);

    }


    public void addStep(ModuleInfo moduleInfo) {

        steps.add(new DefaultWorkflowStep(moduleInfo.getDelegateClassName()).createModule(commandService, moduleService));
    }

    @FXML
    public void startBatchProcessing() {

        List<BatchSingleInput> inputs = new ArrayList<>();
        PlaneSet<PlaneDB> planeSet = planeSetComboBox.getSelectionModel().getSelectedItem();
        for (PlaneDB plane : planeSet.getPlaneList()) {

            PlaneDBBatchInput input = new PlaneDBBatchInput(planeSet.getProjectDisplay().getProject(),plane,PlaneDBBatchInput.SaveMode.REPLACE_ENTRY);

            //context.inject(input);

            input.setSaveDirectory(saveFolderProperty.getValue().getAbsolutePath());
            inputs.add(input);
        }

        
        Task fakeTask = batchService.applyWorkflow(inputs, new DefaultWorkflow(steps));
        
        EventHandler<WorkerStateEvent> onFinished = event->{
            currentTaskProperty.setValue(null);
        };
                
                
        
        currentTaskProperty.setValue(fakeTask);
        fakeTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, onFinished);
        fakeTask.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED,onFinished);
        fakeTask.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, onFinished);
        //currentTaskProperty.setValue(batchService.applyWorkflow(inputs, new DefaultWorkflow(steps))) ;
        ImageJFX.getThreadPool().submit(currentTaskProperty.getValue());
        
    }

    public void onCurrentTaskChanged(Observable obs, Task oldValue, Task newValue) {

        if (newValue != null) {
            progressBar.progressProperty().bind(newValue.progressProperty());
            progressLabel.textProperty().bind(newValue.messageProperty());
        }

    }

    private void registerAction(ShadowMenu menu) {
        if (menu.isLeaf()) {
            modules.put(menu.getName(), () -> {
                addStep(menu.getModuleInfo());
            });
        } else {
            menu.getChildren().forEach(child -> registerAction(child));
        }
    }


    @FXML
    public void saveWorkflow() {
        myWorkflowService.addWorkflow(new DefaultWorkflow(steps));
    }
    
}
