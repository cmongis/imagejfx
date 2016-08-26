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
package ijfx.ui.datadisplay.table;

import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import net.imagej.table.Table;
import net.imagej.table.TableDisplay;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class TableDisplayView extends BorderPane {

    @FXML
    TableView tableView;

    TableDisplay tableDisplay;

    FlexibleColumnModel model;

    final Logger logger = ImageJFX.getLogger();

    public TableDisplayView() {

        logger.info("Injecting FXML");
        try {
            // inject TableDisplayView.fxml from the class name
            FXUtilities.injectFXML(this, "/ijfx/ui/table/TableDisplayView.fxml");
            logger.info("FXML injected");
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);;
        }
        logger.info("Creating table model");
        model = new FlexibleColumnModel(tableView);
        logger.info("Model created");

    }

    public TableDisplayView(TableDisplay tableDisplay) {
        this();
        display(tableDisplay);
    }

    public void display(TableDisplay display) {
        this.tableDisplay = display;
        ImageJFX.getThreadPool().submit(this::renderTable);
        logger.info("table rendered");
    }

    public void renderTable() {
        final Table table = tableDisplay.get(0);
        model.display(table);
    }

    public TableDisplay getTableDisplay() {
        return tableDisplay;
    }

    public class FlexibleColumnModel {

        private final IntegerProperty columnCount = new SimpleIntegerProperty(0);
        private final ObservableList<RowModel> rows = FXCollections.observableArrayList();

        TableView tableView;

        public FlexibleColumnModel(TableView tableView) {
            this.tableView = tableView;
            tableView.setItems(rows);
        }

        public void display(Table table) {
            rows.clear();
            ArrayList<RowModel> rows = new ArrayList<>();

            for (int row = 0; row < table.getRowCount(); row++) {
                RowModel rowModel = new RowModel();
                logger.info("Adding row " + row);
                for (int col = 0; col < table.getColumnCount(); col++) {
                    rowModel.add(table.get(col).get(row));
                }
                rows.add(rowModel);
                Platform.runLater(() -> updateColumns(rowModel));
            }
            addRows(rows);
            Platform.runLater(() -> {
                for (int i = 0; i != table.getColumnCount(); i++) {
                    TableColumn column = (TableColumn) tableView.getColumns().get(i);
                    column.setText(table.get(i).getHeader());
                }

            });

        }

        public void addRow(RowModel row) {
            rows.add(row);
            Platform.runLater(() -> updateColumns(row));
        }

        public void addRows(List<RowModel> rows) {
            this.rows.addAll(rows);
        }

        public synchronized void updateColumns(RowModel row) {

            if (row.size() > getColumnCount()) {

                for (int i = getColumnCount(); i < row.size(); i++) {
                    tableView.getColumns().add(generateTableColumn(i));
                    logger.info("Adding column " + i);

                }

                setColumnCount(row.size());

            }

        }

        public TableColumn generateTableColumn(final int number) {
            TableColumn<RowModel, String> column = new TableColumn<RowModel, String>();
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RowModel, String>, ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<RowModel, String> param) {
                    try {
                        return new SimpleObjectProperty<>(param.getValue().get(number).toString());
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            return column;
        }

        public int getColumnCount() {
            return columnCount.get();
        }

        public void setColumnCount(int value) {
            columnCount.set(value);
        }

        public IntegerProperty columnCountProperty() {
            return columnCount;
        }

        public ObservableList<RowModel> getRows() {
            return rows;
        }

        public void setRows(List<RowModel> rows) {
            this.rows.clear();
            this.rows.addAll(rows);
            // this.rows = rows;
        }

    }

    public class RowModel extends ArrayList<Object> {

        public RowModel() {
            super();
        }

        public RowModel(Object[] object) {
            super();
            Collections.addAll(this, object);
        }
    }

}
