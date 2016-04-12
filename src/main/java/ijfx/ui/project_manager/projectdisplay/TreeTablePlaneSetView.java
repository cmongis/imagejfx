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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;

/**
 *
 * @author cyril
 */
public class TreeTablePlaneSetView extends BorderPane implements PlaneSetView {

    @FXML
    TreeTableView<PlaneOrMetaData> treeTableView;

    PlaneSet currentPlaneSet;
    
    TreeItem<PlaneOrMetaData> currentItem;
    

    Logger logger = ImageJFX.getLogger();

    public TreeTablePlaneSetView() {
        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            Logger.getLogger(TreeTablePlaneSetView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setCurrentItem(TreeItem<PlaneOrMetaData> item) {
        treeTableView.getSelectionModel().select(item);
    }

    @Override
    public TreeItem<PlaneOrMetaData> getCurrentItem() {
        return currentItem;
    }

    @Override
    public void setCurrentPlaneSet(PlaneSet planeSet) {
        currentPlaneSet = planeSet;
        treeTableView.setRoot(planeSet.getRoot());
    }

    @Override
    public PlaneSet getCurrentPlaneSet() {
        return currentPlaneSet;
    }

  

    @Override
    public Node getIcon() {
        return new FontAwesomeIconView(FontAwesomeIcon.BARS);
    }

    @Override
    public Node getNode() {
        return this;
    }

    public void addFirstColumn() {
        
        TreeTableColumn<PlaneOrMetaData,String> column = new TreeTableColumn<>();
        column.setText("Object");
        column.setCellValueFactory(this::getFirstColumnData);
        
        treeTableView.getColumns().add(column);
        
    }
    
    
    
    @Override
    public void setHirarchy(List<String> hierarchy) {

        treeTableView.getColumns().clear();
        logger.info("Setting hierarchy");
        addFirstColumn();
        for (String keyName : hierarchy) {

            logger.info("Adding column " + keyName);
            TreeTableColumn<PlaneOrMetaData, String> column = new TreeTableColumn<>();
            column.setId(keyName);
            column.setText(keyName);
            column.setCellValueFactory(this::getCellData);

            treeTableView.getColumns().add(column);

        }

    }

    public ObservableValue<String> getFirstColumnData(CellDataFeatures<PlaneOrMetaData,String> obs) {
        PlaneOrMetaData planeOrMetaData = obs.getValue().getValue();
        
        if(planeOrMetaData == null) return new SimpleStringProperty("ROOT");
        
        if(planeOrMetaData.isMetaData()) {
            return new SimpleStringProperty("Folder");
        }
        else {
            return new SimpleStringProperty("Plane");
        }
        
        
        
    }
    
    public ObservableValue<String> getCellData(CellDataFeatures<PlaneOrMetaData, String> obs) {

       // logger.info("Handling things");
        
        PlaneOrMetaData data = obs.getValue().getValue();

        if (data == null) {
            return new SimpleStringProperty("Root");
        }
        
        if (data.isMetaData()) {
            if (obs.getTreeTableColumn().getId().equals(data.getMetaData().getName()) == false) {
                return null;
            } else {
                return new SimpleStringProperty(data.getMetaData().getStringValue());
            }
        } else {
            return new SimpleStringProperty(data.getPlaneDB().metaDataSetProperty().get(obs.getTreeTableColumn().getId()).getStringValue());
        }

    }
    
    

}
