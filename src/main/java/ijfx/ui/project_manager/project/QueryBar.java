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

import ijfx.core.project.Project;
import ijfx.core.project.DefaultProjectManagerService;
import ijfx.core.project.DefaultProjectModifierService;
import ijfx.core.project.query.QueryService;
import ijfx.core.project.query.Selector;
import ijfx.core.project.query.DefaultSelector;
import ijfx.core.project.command.Invoker.Operation;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class QueryBar extends HBox {

    @FXML
    private ComboBox<String> metadataComboBox;

    @FXML
    private TextField inputField;

    @FXML
    private CheckBox strictSearchCheckBox;

    @FXML
    private Button searchButton;

    private Property<Boolean> strictSearch;

    private final SimpleBooleanProperty validInputProperty = new SimpleBooleanProperty();

    private static final Logger logger = ImageJFX.getLogger();

    @Parameter
    private DefaultProjectModifierService modifierService;

    @Parameter
    private DefaultProjectManagerService managerService;

    @Parameter
    private QueryService queryService;

    private static final ObservableList<String> keyList = FXCollections.observableArrayList();

    public static final String FORMAT_STRICT_SEARCH = "\"%s\" = \"%s\"";
    public static final String FORMAT_PERMISSIVE_SEARCH = "\"%s\" = \"/%s/\"";

    public QueryBar(Context context) {
        super();

        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            ImageJFX.getLogger();
        }

        context.inject(this);

        strictSearch = strictSearchCheckBox.selectedProperty();

        searchButton.disableProperty().bind(validInputProperty.not());

        metadataComboBox.valueProperty().addListener(this::onComboBoxValueChanged);

        logger.info("Creation complete");

        setId("browser-query-bar");

        managerService.currentProjectProperty().addListener(this::onCurrentProjectChanged);

    }

    public void onCurrentProjectChanged(Observable obs, Project oldValue, Project newValue) {
        logger.info("current project changed");
        if (newValue != null) {
            updateKeys(newValue);
            
        }
        if (oldValue != null) {
            oldValue.getInvoker().onOperationProperty().removeListener(this::onOperationAdded);
            oldValue.getImages().removeListener(this::onImageAdded);
        }
        if (newValue != null) {
            //newValue.getImages().addListener(this::onImageAdded);
            newValue.getInvoker().onOperationProperty().addListener(this::onOperationAdded);
        }

    }

    public void onComboBoxValueChanged(Observable obs, String oldValue, String newValue) {
        // it's valid is there something inside the combobox
        validInputProperty.set(newValue != null && !newValue.equals(""));
    }

    public void onImageAdded(Observable obs, List<PlaneDB> oldOne, List<PlaneDB> project) {
        updateKeys(getCurrentProject());
    }

    public void onOperationAdded(Observable obs, Operation oldValue, Operation newValue) {
        logger.info("Updating key after operation executed");
        updateKeys(getCurrentProject());
    }

    // updates the key list
    public void updateKeys(Project project) {

        metadataComboBox.setItems(keyList);

        List<String> toRemove = new ArrayList<>(keyList.size());
        List<String> toAdd = new ArrayList<>(keyList.size());
        
        logger.info("updating key list");
        List<String> newKeys = modifierService
                .getPossibleNewHierarchyKey(project)
                .stream().sorted((a, b) -> a.compareTo(b))
                .collect(Collectors.toList());

        
                
        
            for (int i = 0; i != keyList.size(); i++) {
                String key = keyList.get(i);
                if (newKeys.contains(key) == false) {

                    toRemove.add(key);
                }
            }

            for (String key : newKeys) {
                if (keyList.contains(key) == false) {

                    toAdd.add(key);
                    
                }
            }
            
            keyList.removeAll(toRemove);
            keyList.addAll(toAdd);

            Collections.sort(keyList);
        

    }

    public void search() {

        // the format of the request depending if it's
        // stricted or not
        String requestFormat = strictSearch.getValue() ? FORMAT_STRICT_SEARCH : FORMAT_PERMISSIVE_SEARCH;

        // the final request
        String request = String.format(requestFormat, metadataComboBox.getValue(), inputField.getText());

        logger.info(request);

        // the generated selector
        Selector selector = new DefaultSelector(request);

        // the query
        new Thread(() -> {
            List<PlaneDB> result = queryService.query(getCurrentProject(), selector, false);
            logger.info(String.format("%d planes found", result.size()));
            modifierService.selectPlane(getCurrentProject(), result, true);
        }).start();

    }

    public Project getCurrentProject() {
        return managerService.currentProjectProperty().getValue();
    }

}
