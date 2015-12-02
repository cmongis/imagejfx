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
import ijfx.core.project.AnnotationRule;
import ijfx.core.project.query.Modifier;
import ijfx.core.project.query.DefaultModifier;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectIoService;
import ijfx.core.project.query.QueryService;
import ijfx.core.project.query.Selector;
import ijfx.core.project.query.DefaultSelector;
import ijfx.ui.project_manager.other.ModifierEditor;
import ijfx.ui.project_manager.other.SelectorEditor;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril Quinton
 * 
 * 
 * Cyril MONGIS : This piece of code is highly deprecated because the Selector API has been changed.
 */
public class RulesController extends BorderPane implements Initializable {

    @FXML
    private TableView rulesTableView;
    @FXML
    private TitledPane infoTitledPane;
    @FXML
    private Pane selectorPane;
    @FXML
    private Pane modifierPane;
    @FXML
    private Button addButton;
    @FXML
    private Button importButton;
    @FXML
    private Button OKButton;
   
    private final Project project;
    private final SelectorEditor selectorEditor;
    private final ModifierEditor modifierEditor;
    private Selector newSelector;
    private Modifier newModifier;
    private final BooleanProperty addEnableProperty;
    
    // Services
    
    @Parameter
    private IOProjectUIService2 ioUIService;
    
    @Parameter
    private ProjectIoService projectIo;
    
    @Parameter
    UiContextService contextService;
    
    @Parameter
     private QueryService queryService;
    
    @Parameter
    Context context;
    
    private ResourceBundle rb;
    private boolean applyRuleWhenClose;

    private TableView.TableViewSelectionModel selectionModel;

    public RulesController(Project project, Context context) {
        this.project = project;
        
        context.inject(this);
        
       /* Await for deletiong
        queryService = contextService.getContext().getService(QueryService.class);
        ioUIService = contextService.getContext().getService(IOProjectUIService2.class);
        projectIo = contextService.getContext().getService(ProjectIoJsonService.class);
               */
        selectorEditor = new SelectorEditor(context);
        modifierEditor = new ModifierEditor(context);
        //initNewSelectorAndNewModifier();
        addEnableProperty = new SimpleBooleanProperty(false);
        applyRuleWhenClose = false;
        FXUtilities.loadView(getClass().getResource("Rules.fxml"), this, true);
    }

    @Override

    public void initialize(URL location, ResourceBundle resources) {
        rb = resources;
        selectionModel = rulesTableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        TableColumn<AnnotationRule, String> selectorCol = new TableColumn<>(resources.getString("selector"));

        selectorCol.setCellValueFactory((TableColumn.CellDataFeatures<AnnotationRule, String> param) -> new ReadOnlyObjectWrapper<>(param.getValue().getSelector().getQueryString()));

        TableColumn<AnnotationRule, String> modifierCol = new TableColumn<>(resources.getString("modifier"));

        modifierCol.setCellValueFactory((TableColumn.CellDataFeatures<AnnotationRule, String> param) -> new ReadOnlyObjectWrapper<>(param.getValue().getModifier().getNonParsedString()));
        Predicate<String> editChecker = (String t) -> new DefaultSelector(t).validSyntaxProperty().get();
        selectorCol.setCellFactory((TableColumn<AnnotationRule, String> param)
                -> new EditingQueryCell(contextService, new SelectorEditor(context), editChecker));
        selectorCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<AnnotationRule, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<AnnotationRule, String> event) {
                Selector newSelector = new DefaultSelector(event.getNewValue());
               // Selector API modified
                //handleCellEdit(event.getRowValue().getSelector(), event.getNewValue());
            }
        });
        modifierCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<AnnotationRule, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<AnnotationRule, String> event) {
                // selector API Modified
               // handleCellEdit(event.getRowValue().getModifier(), event.getNewValue());

            }
        });
        Predicate<String> modifierEditChecker = (String t) -> new DefaultModifier(t).validSyntaxProperty().get();
        modifierCol.setCellFactory((TableColumn<AnnotationRule, String> param) -> new EditingQueryCell(contextService, new ModifierEditor(context), modifierEditChecker));
        TableColumn<AnnotationRule, AnnotationRule> enableCol = new TableColumn<>(resources.getString("enable"));
        enableCol.setCellFactory(new Callback<TableColumn<AnnotationRule, AnnotationRule>, TableCell<AnnotationRule, AnnotationRule>>() {

            @Override
            public TableCell<AnnotationRule, AnnotationRule> call(TableColumn<AnnotationRule, AnnotationRule> param) {
                return new TableCell<AnnotationRule, AnnotationRule>() {
                    @Override
                    protected void updateItem(AnnotationRule item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(new CheckBoxInCellController(queryService, project, item));
                        }
                    }
                };
            }
        });
        TableColumn<AnnotationRule, AnnotationRule> enableCol2
                = new TableColumn<>(resources.getString("enable"));
        enableCol2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AnnotationRule, AnnotationRule>, ObservableValue<AnnotationRule>>() {

            @Override
            public ObservableValue<AnnotationRule> call(TableColumn.CellDataFeatures<AnnotationRule, AnnotationRule> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue());
            }
        });
        enableCol2.setCellFactory(new Callback<TableColumn<AnnotationRule, AnnotationRule>, TableCell<AnnotationRule, AnnotationRule>>() {

            @Override
            public TableCell<AnnotationRule, AnnotationRule> call(TableColumn<AnnotationRule, AnnotationRule> param) {
                return new CheckBoxTableCell<>(new Callback<Integer, ObservableValue<Boolean>>() {

                    @Override
                    public ObservableValue<Boolean> call(Integer param) {
                        return enableCol2.getCellData(param).unableProperty();
                    }
                });
            }
        });
        rulesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        rulesTableView.getColumns().addAll(selectorCol, modifierCol, enableCol2);
        rulesTableView.setItems(project.getAnnotationRules());
        rulesTableView.setEditable(true);
        addButton.disableProperty().bind(addEnableProperty.not());
        OKButton.setOnAction((ActionEvent event) -> {
            if (applyRuleWhenClose) {
                applyRules();
            }
            close();
        });
        addButton.setOnAction((ActionEvent event) -> {
            addRuleAction();
        });
        importButton.setOnAction((ActionEvent event) -> {
            importRuleAction();
        });
        selectorPane.getChildren().add(selectorEditor);
        modifierPane.getChildren().add(modifierEditor);

        rulesTableView.setOnKeyReleased(this::handleKeyReleased);
        //******** Setting up tooltips *************
        importButton.setTooltip(new Tooltip(resources.getString("importRuleDirection")));
    }

    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            List<AnnotationRule> rmRules = selectionModel.getSelectedItems();
            queryService.removeAnnotationRule(project, rmRules);

        }
    }

    /* Commented because the Selector / Modifier API has been modified
        
    private void handleCellEdit(Selector query, String newNonParsedVal) {
        //queryService.modifyQueryObject(project, query, newNonParsedVal);
    }*/

    private void evalAddEnableProperty() {
       /*
        if (newSelector.validSyntaxProperty().get() && newModifier.validSyntaxProperty().get()) {
            addEnableProperty.set(true);
        } else {
            addEnableProperty.set(false);
        }*/
    }

    private void addRuleAction() {
        //if (newSelector.validSyntaxProperty().get() && newModifier.validSyntaxProperty().get()) {
            queryService.addAnnotationRule(project, newSelector, newModifier);
            applyRuleWhenClose = true;
            //initNewSelectorAndNewModifier();
        //}
    }
    /*
    private void initNewSelectorAndNewModifier() {
        newSelector = new DefaultSelector();
        selectorEditor.setSelector(newSelector);
        newModifier = new DefaultModifier();
        modifierEditor.setModifier(newModifier);
        ChangeListener<Boolean> cl = new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                evalAddEnableProperty();
            }
        };
        
        /*
        for (Selector codeSyntax : Arrays.asList(newSelector, newModifier)) {
            codeSyntax.validSyntaxProperty().addListener(cl);
        }
    }*/

    private void importRuleAction() {
        File projectFile = ioUIService.openProjectFile(this.getScene().getWindow(), rb.getString("importRulesFromProject"));
        if (projectFile != null) {
            try {
                List<AnnotationRule> ruleList = projectIo.loadRules(projectFile);
                RulePickerController rulePicker = new RulePickerController(ruleList);
                Stage stage = ProjectManagerUtils.createDialogWindow(getScene().getWindow(), rulePicker, rb.getString("rulePicker"));
                stage.showAndWait();
                List<AnnotationRule> importedRuleList = rulePicker.getPickedRules();
                if (!importedRuleList.isEmpty()) {
                    queryService.addAnnotationRule(project, ruleList);
                    applyRuleWhenClose = true;
                }

            } catch (IOException ex) {
                ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            } catch (DataFormatException ex) {
               ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            }
        }

    }

    private void close() {
        FXUtilities.close(this);
    }

    private void applyRules() {
        queryService.applyAnnotationRules(project);
    }

}

class CheckBoxInCellController extends Pane {

    private final ReadOnlyBooleanProperty boolProp;
    private final CheckBox cb;
    private final QueryService queryService;
    private final Project project;
    private final AnnotationRule rule;

    public CheckBoxInCellController(QueryService queryService, Project project, AnnotationRule rule) {
        this.queryService = queryService;
        this.project = project;
        this.rule = rule;
        boolProp = rule.unableProperty();
        cb = new CheckBox();
        cb.selectedProperty().bind(boolProp);
        cb.setOnAction((ActionEvent event) -> {
            queryService.enableAnnotationRule(project, rule, !cb.isSelected());
            event.consume();
        });
        this.getChildren().add(cb);
    }

}
