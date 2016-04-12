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
package ijfx.ui.project_manager.projectdisplay;

import static com.squareup.okhttp.internal.Internal.logger;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.query.QueryService;
import ijfx.core.project.query.Selector;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import mongis.utils.ConditionList;
import mongis.utils.FXUtilities;
import org.reactfx.EventStreams;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class TablePlaneSetView extends BorderPane implements PlaneSetView {

    @FXML
    TextField filterTextField;

    @FXML
    TableView<PlaneDB> tableView;

    ObservableList<PlaneDB> planeList;

    ObservableList<PlaneDB> filteredList = FXCollections.observableArrayList();

    TreeItem<PlaneOrMetaData> currentItem;

    PlaneSet planeSet;

    @Parameter
    PlaneSelectionService planeSelectionService;
    
    
    @Parameter
    QueryService queryService;
    
    
   
    
    public TablePlaneSetView(Context context) {
        super();
        try {
            FXUtilities.injectFXML(this);
            context.inject(this);
            tableView.setItems(filteredList);
            tableView.setEditable(true);
            EventStreams.valuesOf(filterTextField.textProperty()).successionEnds(Duration.ofSeconds(1))
                    .subscribe(this::onFilterTypingEnded);

        } catch (IOException ex) {
            Logger.getLogger(TablePlaneSetView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void onFilterTypingEnded(String value) {
        if (value == null) {
            return;
        }
        
        
        
        
        
        filteredList.clear();
        if (planeList == null) {
            logger.warning("planeList is null");
            return;
        }
        if (value.equals((""))) {

            filteredList.addAll(planeList);

        } else {
            Predicate<PlaneDB> filter = new PlaneFilterFactory().create(value);
            filteredList.addAll(
                    planeList.filtered(filter));
        }

    }

    @Override
    public void setCurrentItem(TreeItem<PlaneOrMetaData> item) {
        this.currentItem = item;
    }

    @Override
    public TreeItem<PlaneOrMetaData> getCurrentItem() {
        return currentItem;
    }

    @Override
    public PlaneSet getCurrentPlaneSet() {
        if(true) return null;
        return planeSet;
    }

    @Override
    public Node getNode() {
        return this;
    }

    private class PlaneFilter implements Predicate<PlaneDB> {

        private final String filter;

        public PlaneFilter(String filter) {
            this.filter = filter;
        }

        @Override
        public boolean test(PlaneDB t) {

            if (t.metaDataSetProperty() == null) {
                return false;
            }
            boolean onMatch = false;
            final ConditionList conditionList = new ConditionList(t.metaDataSetProperty().size());

            t.metaDataSetProperty().forEach((key, value) -> {
                if (value.getStringValue().toLowerCase().contains(filter.toLowerCase())) {
                    conditionList.add(true);
                }
            });

            return conditionList.isOneTrue();

        }
    }

    private class MetaDataPlaneFilter implements Predicate<PlaneDB> {

        private final String key;
        private final String value;

        public MetaDataPlaneFilter(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String findKey(Map<String, ? extends Object> metadata, String key) {

            return metadata.keySet().stream().filter(mapKey -> mapKey.toLowerCase().equals(key.toLowerCase())).findFirst().orElse(null);

        }

        @Override
        public boolean test(PlaneDB t) {

            if (t.metaDataSetProperty() == null) {
                return false;
            }
            String key = findKey(t.metaDataSetProperty(), this.key);
            if (key == null) {
                return false;
            }
            System.out.println(key);
            return t.metaDataSetProperty().get(key).getStringValue().toLowerCase().contains(value.toLowerCase());

        }
    }
    
    

    private class PlaneFilterFactory {

        Selector selector;
        
        public Predicate<PlaneDB> create(String query) {
            
            selector = queryService.getSelector(query);
            
            if(selector.canParse(query)) {
                return plane->selector.matches(plane, null);
            }
            
            
            if (query.contains(("="))) {
                System.out.println("key and value !");
                String[] keyAndValue = query.split("=");
                
                return new MetaDataPlaneFilter(keyAndValue[0], keyAndValue[1]);
                
                
            }
            else {
                return  new PlaneFilter(query);
            }
        }

    }

    @Override
    public void setCurrentPlaneSet(PlaneSet planeSet) {
        
        if(this.planeSet != null) {
            this.planeSet.getPlaneList().removeListener(this::onListChanged);
        }
        
        
        this.planeSet = planeSet;
        
        this.planeSet.getPlaneList().addListener(this::onListChanged);
        
        logger.info("" + planeSet);
        if (planeSet == null) {
            return;
        }
        logger.info("Changing the planeSet");

        if (planeSet.getPlaneList() == null) {
            logger.warning("There is no PlaneList associated to the PlaneSet " + planeSet.getName());
        }

        planeList = planeSet.getPlaneList();
        updateFiltered();

    }

    public void onListChanged(ListChangeListener.Change<? extends PlaneDB> change) {
        while(change.next()) {
            updateFiltered();
        }
    }
    
    
    
    
    protected void updateFiltered() {
        logger.info("Updating the filtered");
        if (planeList == null) {
            return;
        }
        onFilterTypingEnded(filterTextField.getText());
    }

    @Override
    public Node getIcon() {
        return new FontAwesomeIconView(FontAwesomeIcon.TABLE);
    }
    
    
    @FXML
    public void selectForProcessing() {
        planeSelectionService.selectPlanes(planeSet.getProjectDisplay().getProject(), filteredList);
    }

    public void addFirstColumn() {

        
        TableColumn<PlaneDB,Boolean> selected = new TableColumn<>();
        
        selected.setEditable(true);
        selected.setCellFactory(p->new CheckBoxTableCell());
        //selected.setCellValueFactory(p->p.getValue().selectedProperty());
        selected.setCellValueFactory(p-> new PlaneSelectionProperty(planeSelectionService, planeSet.getProjectDisplay().getProject(), p.getValue()));
        selected.setPrefWidth(USE_COMPUTED_SIZE+30);
        
        TableColumn<PlaneDB, String> column = new TableColumn<>();
        
        
        
        
        column.setText("Object");
        column.setCellValueFactory(this::getFirstColumnData);

        
        
        
        tableView.getColumns().addAll(selected,column);

    }

    private ObservableValue<String> getFirstColumnData(CellDataFeatures<PlaneDB, String> features) {
        return new SimpleStringProperty(features.getValue().getImageID());
    }
    
    @FXML
    private void unselectAll() {
        planeSelectionService.setPlaneSelection(planeSet.getProjectDisplay().getProject(), planeList, false);
    }

    @Override
    public void setHirarchy(List<String> hierarchy) {

        tableView.getColumns().clear();
        logger.info("Setting hierarchy");
        addFirstColumn();
        for (String keyName : hierarchy) {

            logger.info("Adding column " + keyName);
            TableColumn<PlaneDB, String> column = new TableColumn<>();
            column.setId(keyName);
            column.setText(keyName);
            column.setCellValueFactory(this::getCellData);

            tableView.getColumns().add(column);

        }

    }

    public ObservableValue<String> getCellData(CellDataFeatures<PlaneDB, String> features) {
        try {
            return new SimpleStringProperty(features.getValue().metaDataSetProperty().get(features.getTableColumn().getId()).getStringValue());
        } catch (Exception e) {
            //logger.log(Level.SEVERE, "Damn", e);

            return null;
        }
    }

}
