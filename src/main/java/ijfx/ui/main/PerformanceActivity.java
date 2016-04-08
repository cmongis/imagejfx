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
package ijfx.ui.main;

import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.ui.activity.Activity;
import java.io.IOException;
import java.util.Collection;
import javafx.beans.Observable;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Activity.class)
public class PerformanceActivity extends BorderPane implements Activity {

    @FXML
    ListView<Timer> timerListView;

    @FXML
    TableView<SummaryStatistics> summaryStatistics;

    @Parameter
    TimerService timerService;

    @FXML
    TableColumn<TimerEntry, String> idColumn;

    @FXML
    TableColumn<TimerEntry, Double> meanColumn;

    @FXML
    TableColumn<TimerEntry, Double> minColumn;

    @FXML
    TableColumn<TimerEntry, Double> maxColumn;

    @FXML
    TableColumn<TimerEntry, Double> stdDevColumn;

    public PerformanceActivity() throws IOException {
        FXUtilities.injectFXML(this);

        timerListView.setCellFactory(this::createListCell);

        idColumn.setCellValueFactory(this::readOnly);

    }

   
  

    @Override
    public Node getContent() {
        return this;

    }

    @Override
    public Task updateOnShow() {
        
        
        Collection<? extends Timer> timers = timerService.getTimers();
        timerListView.getItems().clear();
        timerListView.getItems().addAll(timers);
        
        if(timers.size() > 0 && timerListView.getSelectionModel().getSelectedItem() == null) {
            timerListView.getSelectionModel().select(timers.iterator().next());
        }
        
        
        return null;

    }

    private ListCell<Timer> createListCell(ListView<Timer> listView) {
        return new TimerListCell();
    }

    private class TimerListCell extends ListCell<Timer> {

        public TimerListCell() {
            itemProperty().addListener(this::onItemChanged);
        }

        public void onItemChanged(Observable obs, Timer oldValue, Timer newvalue) {
            setText(newvalue.getName());
        }

    }

    private <T, R> ObservableValue<R> readOnly(TableColumn.CellDataFeatures<T, R> feature) {
        String propertyName = feature.getTableColumn().getId().replace("Column", "");
        T bean = feature.getValue();
        try {
            return new JavaBeanObjectPropertyBuilder<>()
                    .bean(bean)
                    .name(propertyName)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private class TimerEntry {

        private final String id;
        private final SummaryStatistics stats;

        public TimerEntry(String id, SummaryStatistics stats) {
            this.id = id;

            this.stats = stats;
        }

        public String getId() {
            return id;
        }

        public SummaryStatistics getStats() {
            return stats;
        }

        public Double getMean() {
            return getStats().getMean();
        }

        public Double getMin() {
            return getStats().getMin();
        }

        public Double getMax() {
            return getStats().getMax();
        }

        public Double getStdDev() {
            return getStats().getMean();
        }

    }
}
