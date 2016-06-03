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
import ijfx.ui.activity.ActivityService;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
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
@Plugin(type = Activity.class,label="performance-activity")
public class PerformanceActivity extends BorderPane implements Activity {

    @FXML
    ListView<Timer> timerListView;

    @FXML
    TableView<TimerEntry> timerEntryTableView;

    @Parameter
    TimerService timerService;

    @FXML
    TableColumn<TimerEntry, String> idColumn;

    @FXML
    TableColumn<TimerEntry, Long> meanColumn;

    @FXML
    TableColumn<TimerEntry, Long> minColumn;

    @FXML
    TableColumn<TimerEntry, Long> maxColumn;

    @FXML
    TableColumn<TimerEntry, Long> stdDevColumn;

    @FXML
    TableColumn<TimerEntry, Long> countColumn;
    
    @Parameter
    ActivityService activityService;
    
    public PerformanceActivity() throws IOException {
        FXUtilities.injectFXML(this);

        timerListView.setCellFactory(this::createListCell);
        timerListView.getSelectionModel().selectedItemProperty().addListener(this::onTimerChanged);
        timerListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        idColumn.setCellValueFactory(this::readOnly);
        meanColumn.setCellValueFactory(this::readOnly);
        stdDevColumn.setCellValueFactory(this::readOnly);
        minColumn.setCellValueFactory(this::readOnly);
        maxColumn.setCellValueFactory(this::readOnly);
        countColumn.setCellValueFactory(this::readOnly);
        

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
        System.out.println("timers "+timers.size());
        if(timers.size() > 0) {
            timerListView.getSelectionModel().select(timers.iterator().next());
        }
        
        return null;

    }
    
    @FXML
    public void reset() {
        timerService.resetTimers();
        activityService.back();
    }

    private ListCell<Timer> createListCell(ListView<Timer> listView) {
        return new TimerListCell();
    }

    private class TimerListCell extends ListCell<Timer> {

        public TimerListCell() {
            itemProperty().addListener(this::onItemChanged);
        }

        public void onItemChanged(Observable obs, Timer oldValue, Timer newvalue) {
            if(newvalue != null)
            setText(newvalue.getName());
        }

    }

    private <T, R> ObservableValue<R> readOnly(TableColumn.CellDataFeatures<T, R> feature) {
        String propertyName = feature.getTableColumn().getId().replace("Column", "");
        System.out.println("Property name : "+propertyName);
        T bean = feature.getValue();
        
        
        
        try {
            //return new SimpleObjectProperty(bean,propertyName);
            
            return new JavaBeanObjectPropertyBuilder<>()
                    
                    .bean(bean)
                    .name(propertyName)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    private void onTimerChanged(Observable obs, Timer oldValue, Timer newValue) {
        
        
        System.out.println(newValue.getName());
        System.out.println(newValue.getStats().size());
        timerEntryTableView.getItems().clear();
        timerEntryTableView.getItems().addAll(
                newValue
                        .getStats()
                        .entrySet()
                        .stream()
                        .map(entry->new TimerEntry(entry.getKey(),entry.getValue()))
                        .collect(Collectors.toList())
         );
        
    }

    public class TimerEntry {
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

        public Long getMean() {
            return Math.round(getStats().getMean());
        }

        public Long getMin() {
            return Math.round(getStats().getMin());
        }

        public Long getMax() {
            return Math.round((getStats().getMax()));
        }

        public Long getStdDev() {
            return Math.round((getStats().getStandardDeviation()));
        }
        
        public Long getCount() {
            System.out.println("gettting n");
            return getStats().getN();
        }
        
       public void setId(String s) {}
       public void setMean(Long d) {}
       public void setMin(Long d) {}
       public void setMax(Long d) {}
       public void setStdDev(Long d) {}
       public void setCount(Long l) {};
       
    }
    
    
}
