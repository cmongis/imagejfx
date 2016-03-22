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
package ijfx.ui.plugin.panel;

import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import ijfx.service.overlay.OverlaySelectionService;
import ijfx.service.overlay.OverlayStatService;
import ijfx.service.overlay.OverlaySelectedEvent;
import java.io.IOException;
import java.util.HashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import net.imagej.DatasetService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.measure.MeasurementService;
import org.scijava.display.DisplayService;
import org.scijava.event.EventHandler;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import mongis.utils.FXUtilities;
import ijfx.ui.UiPlugin;
import ijfx.ui.UiConfiguration;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import mongis.utils.AsyncCallback;
import net.imagej.event.OverlayUpdatedEvent;
import net.imagej.overlay.LineOverlay;
import net.imagej.overlay.Overlay;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.scijava.display.event.DisplayActivatedEvent;
import ucar.ma2.ArrayBoolean;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "overlayPanel", localization = Localization.RIGHT, context = "imagej+image-open+overlay-selected")
public class OverlayPanel extends BorderPane implements UiPlugin {

    @Parameter
    OverlayService overlayService;

    @Parameter
    OverlaySelectionService overlaySelectionService;

    @Parameter
    OverlayStatService statsService;

    @Parameter
    MeasurementService mSrv;

    @Parameter
    DatasetService datasetService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    DisplayService displayService;

    @FXML
    TableView<MyEntry> tableView;

    @FXML
    TableColumn<MyEntry, String> keyColumn;

    @FXML
    TableColumn<MyEntry, Double> valueColumn;

    ObservableList<MyEntry> entries = FXCollections.observableArrayList();

    @FXML
    TextField overlayNameField;

    @FXML
    LineChart<String,Double> lineChart;

    @FXML
    AreaChart<String,Double> areaChart;

    private final static String EMPTY_FIELD = "Name your overlay here :-)";

    ObjectProperty<Overlay> overlayProperty = new SimpleObjectProperty<>();

    public OverlayPanel() throws IOException {
        super();

        FXUtilities.injectFXML(this);

        keyColumn.setCellValueFactory(new PropertyValueFactory("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory("value"));

        tableView.setItems(entries);

        entries.add(new MyEntry("nothing", 0d));

        overlayNameField.setPromptText(EMPTY_FIELD);
        overlayNameField.textProperty().addListener(this::onOverlayNameChanged);

        overlayProperty.addListener(this::onOverlaySelectionChanged);

    }

    ImageDisplay imageDisplay;

    public void onOverlaySelectionChanged() {

        if (imageDisplay == null) {
            return;
        }

        // check if multiple
        // if multiple, do statistics with all and hide editable fields
        // else do statistic with one
        // update stats entry
        // calculate new statistics
        // update view
        //
    }

    public ImageDisplay currentDisplay() {
        return displayService.getActiveDisplay(ImageDisplay.class);
    }

    @EventHandler
    public void handleEvent(OverlaySelectedEvent event) {

        if (event.getOverlay() == null) {
            return;
        }

        // task calculating the stats in a new thread
        Task<HashMap<String, Double>> task = new Task<HashMap<String, Double>>() {
            @Override
            protected HashMap<String, Double> call() throws Exception {
                return statsService.getStat(event.getDisplay(), event.getOverlay());
            }

            @Override
            protected void succeeded() {
                super.succeeded();

                tableView.getItems().clear();
                this.getValue().forEach((key, value) -> {
                    entries.add(new MyEntry(key, value));
                });

            }
        };
        overlayProperty.setValue(event.getOverlay());

        ImageJFX.getThreadPool().submit(task);

    }

    @EventHandler
    public void onActiveDisplayChanged(DisplayActivatedEvent event) {
        onOverlaySelectionChanged();
    }

    @EventHandler
    public void onOverlayUpdated(OverlayUpdatedEvent event) {
        updateChart(event.getObject());
    }

    private void updateChart(Overlay overlay) {

        boolean isLineOverlay = overlay instanceof LineOverlay;

        lineChart.setVisible(isLineOverlay);
        areaChart.setVisible(!isLineOverlay);

        if (isLineOverlay) {

            updateLineChart((LineOverlay) overlay);
        } else {
            updateAreaChart(overlay);
        }
    }

    private void updateLineChart(LineOverlay overlay) {

    }

    private void updateAreaChart(Overlay overlay) {

        new AsyncCallback<Overlay, XYChart.Series<String, Double>>()
                .setInput(overlay)
                .run(o -> {
                    System.out.println("Starting calculatution");
                    Double[] valueList = statsService.getValueList(currentDisplay(), o);
                    HistogramModel model = new HistogramModel();
                    SummaryStatistics sumup = new SummaryStatistics();
                    for (Double v : valueList) {
                        sumup.addValue(v);
                    }

                    double min = sumup.getMin();
                    double max = sumup.getMax();

                    int bins = new Double(max - min).intValue();
                    
                    System.out.println(String.format("%.0f to %.0f with %d values",min,max,bins));
                    
                    EmpiricalDistribution distribution = new EmpiricalDistribution(bins);
                    
                    /*
                    Double[] values = Arrays.asList(valueList)
                            .stream()
                            .map(v -> v.doubleValue())
                            .sorted()
                            //.toArray();
                            .toArray(size -> new Double[size]);*/
                    distribution.load(ArrayUtils.toPrimitive(valueList));

                    XYChart.Series<String, Double> serie = new XYChart.Series<>();
                    ArrayList<Data<String, Double>> data = new ArrayList<>(bins);
                    for (double cat : distribution.getUpperBounds()) {
                        data.add(new Data<String, Double>(new Double(cat).toString(), distribution.density(cat)));
                    }

                    serie.getData().clear();
                    serie.getData().addAll(data);
                    
                    return serie;

                })
                .then(serie -> {
                   // areaChart.getData().clear();
                   // areaChart.getData().add(serie);

                })
                .start();


    }

    @FXML
    public void deleteOverlay() {

        ImageDisplay display = displayService.getActiveDisplay(ImageDisplay.class);

        overlaySelectionService.getSelectedOverlays(display).forEach(overlay -> {

            overlayService.removeOverlay(display, overlay);
        });
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {
        return this;
    }

    public class MyEntry {

        protected String name;
        protected Double value;

        public MyEntry(java.lang.String name, Double value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    public void onOverlaySelectionChanged(Observable obs, Overlay oldValue, Overlay newValue) {

        overlayNameField.setText(newValue.getName());

    }

    public void onOverlayNameChanged(Observable obs, String oldText, String newText) {
        if (overlayProperty.getValue() != null) {
            overlayProperty.getValue().setName(newText);
        }
    }

}
