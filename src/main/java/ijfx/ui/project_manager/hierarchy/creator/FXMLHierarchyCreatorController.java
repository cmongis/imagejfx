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
package ijfx.ui.project_manager.hierarchy.creator;

import ijfx.core.project.DefaultProjectModifierService;
import ijfx.core.project.Project;
import mongis.utils.FXUtilities;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 * FXML Controller class
 *
 * @author Cyril Quinton
 */
public class FXMLHierarchyCreatorController extends BorderPane implements HierarchyCreator, Initializable {

    public static final int INTERVAL = 2;
    public static final String HIERARCHY_ITEM_DRAG = "hierarchyItemDrag";
    @FXML
    private VBox vbox;
    @FXML
    private AnchorPane scrollAnchorPane;
    @FXML
    private Button addButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button setChangeButton;
    private Pane container;
    private final ObservableList<String> possibleNewKeys;
    private final List<String> hierarchy;
    private final Project project;
    
    @Parameter
    private DefaultProjectModifierService projectModifierService;
    private final ObservableList<HierarchyKey> hierarchyKeyList;

   
    
    public FXMLHierarchyCreatorController(Project project, Context contextService) {
        this.project = project;
        contextService.inject(this);

        List<String> possibleKeys = projectModifierService.getPossibleNewHierarchyKey(project);
        possibleNewKeys = FXCollections.observableArrayList();
        
        
        List<String> sortedKeys = possibleKeys
                .stream()
                .sorted((o1,o2)-> o1.compareTo(o2))
                .collect(Collectors.toList());
        
        
        possibleNewKeys.setAll(sortedKeys);
        
        hierarchyKeyList = FXCollections.observableArrayList();
        //create a copy of the keyHierarchy list
        hierarchy = new ArrayList<>();
        for (String key : project.getHierarchy()) {
            hierarchy.add(key);
        }
        for (int i = 0; i < project.getHierarchy().size(); i++) {
            String key = project.getHierarchy().get(i);
            hierarchyKeyList.add(new HierarchyKey(this).setIndex(i).setKey(key));
        }
        hierarchyKeyList.addListener(this::handleHierarchyKeyListChange);
        FXUtilities.loadView(getClass()
                .getResource("FXMLHierarchyCreator.fxml"), this, true);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        container = vbox;
        cancelButton.setOnAction((ActionEvent event) -> {
            exit();
        });
        setChangeButton.setOnAction((ActionEvent event) -> {
            setNewHierarchy();
            exit();
        });
        addButton.setOnAction((ActionEvent event) -> {
            createNewHierarchyLevel();
        });
        
        FXUtilities.modifyUiThreadSafe(this::createView);
    }

    public void createNewHierarchyLevel() {
        HierarchyKey hierarchyKey = new HierarchyKey(this);
        hierarchyKey.setIndex(hierarchyKeyList.size());
        hierarchyKeyList.add(hierarchyKey);
    }

    public ObservableList<String> getPossibleNewKey() {
        return possibleNewKeys;
    }

    private FXMLHierarchyLineController createLine() {
        return new FXMLHierarchyLineController(this);
    }

    private int getHierarchyIndex(int layoutIndex) {
        return layoutIndex / INTERVAL;
    }

    @FXML
    public void onDownButtonAction() {

    }

    @FXML
    public void onUpButtonAction() {

    }

    @Override
    public List<String> getHierarchy() {
        return hierarchy;
    }

    private void setNewHierarchy() {
        hierarchy.clear();
        for (HierarchyKey hierarchyKey : hierarchyKeyList.sorted(HierarchyKey.getComparator())) {
            if (hierarchyKey.getKey() != null) {
                hierarchy.add(hierarchyKey.getKey());
            }

        }

    }

    private void handleHierarchyKeyListChange(ListChangeListener.Change<? extends HierarchyKey> c) {
        while (c.next()) {
            if (c.wasRemoved()) {
                for (HierarchyKey removed : c.getRemoved()) {
                    shift(removed, false);
                }
            }
            if (c.wasAdded()) {
                for (HierarchyKey added : c.getAddedSubList()) {
                    shift(added, true);
                }
            }
        }
        FXUtilities.modifyUiThreadSafe(this::createView);
    }

    private void shift(HierarchyKey element, boolean increment) {
        int startIndex = element.getIndex();
        for (HierarchyKey hierarchyKey : hierarchyKeyList) {
            if (element != hierarchyKey && hierarchyKey.getIndex() >= startIndex) {
                int newIndex = increment ? hierarchyKey.getIndex() + 1 : hierarchyKey.getIndex() - 1;
                hierarchyKey.setIndex(newIndex);
            }
        }
    }

    private void createView() {
        vbox.getChildren().clear();
        int lastIndex = 0;
        List<HierarchyKey> sortedList = hierarchyKeyList.sorted(HierarchyKey.getComparator());
        for (int i = 0; i < sortedList.size(); i++) {
            HierarchyKey hierarchyKey = sortedList.get(i);
            lastIndex = hierarchyKey.getIndex();
            FXMLHierarchyLineController line = new FXMLHierarchyLineController(this).setIndex(lastIndex);
            if (i == 0) {
                line.setVisibleLine(false);
            }
            vbox.getChildren().add(line);
            vbox.getChildren().add(hierarchyKey);
        }
        vbox.getChildren().add(new FXMLHierarchyLineController(this).setIndex(lastIndex + 1).setVisibleLine(false));
    }

    public ObservableList<HierarchyKey> getHieararchyKeyList() {
        return hierarchyKeyList;
    }

    private void exit() {
        Stage stage = (Stage) this.getScene().getWindow();
        stage.close();
    }

}
