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
package ijfx.ui.filter;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.controlsfx.control.RangeSlider;

/**
 *
 * @author cyril
 */
public class DefaultNumberFilter extends BorderPane implements NumberFilter {

    RangeSlider rangeSlider = new RangeSlider();

    ObjectProperty<Predicate<Double>> predicateProperty = new SimpleObjectProperty<>();

    Collection< ? extends Number> possibleValues;

    @FXML
    NumberAxis categoryAxis;

    @FXML
    NumberAxis numberAxis;

    @FXML
    AreaChart<Double, Double> areaChart;

    @FXML
    BorderPane borderPane;
    
    @FXML
    TextField highTextField;
    
    @FXML
    TextField lowTextField;
    
    @FXML
    Label valueCountLabel;
    
    
    
    
    public DefaultNumberFilter() {
        try {
            FXUtilities.injectFXML(this);

            borderPane.setTop(rangeSlider);

//            areaChart.setCategoryGap(0);
            rangeSlider.setShowTickLabels(true);
            areaChart.setOnMouseClicked(event -> update());
            
            rangeSlider.lowValueChangingProperty().addListener(this::onLowHighValueChanged);
            rangeSlider.highValueChangingProperty().addListener(this::onLowHighValueChanged);
            
            rangeSlider.lowValueProperty().addListener(this::onLowHighValueChanged);
            rangeSlider.highValueProperty().addListener(this::onLowHighValueChanged);
            
             categoryAxis.upperBoundProperty().bind(rangeSlider.maxProperty());
             categoryAxis.lowerBoundProperty().bind(rangeSlider.minProperty());
             categoryAxis.minorTickCountProperty().bind(rangeSlider.minorTickCountProperty());
             categoryAxis.tickUnitProperty().bind(rangeSlider.majorTickUnitProperty());
            NumberStringConverter converter = new NumberStringConverter(NumberFormat.getIntegerInstance());
            
            Bindings.bindBidirectional(lowTextField.textProperty(), rangeSlider.lowValueProperty(), converter);
            Bindings.bindBidirectional(highTextField.textProperty(), rangeSlider.highValueProperty(), converter);
            
            rangeSlider.getStyleClass().add("range-slider");
            
            
        } catch (IOException ex) {
            Logger.getLogger(DefaultNumberFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public DoubleProperty maxProperty() {

        return rangeSlider.highValueProperty();
    }

    @Override
    public DoubleProperty minProperty() {
        return rangeSlider.lowValueProperty();
    }

    @Override
    public void setAllPossibleValue(Collection<? extends Number> values) {
        possibleValues = values;

        updateChart();

    }

    @Override
    public Property<Predicate<Double>> predicateProperty() {
        return predicateProperty;
    }

    public void updatePredicate() {
        final double min = minProperty().getValue();
        final double max = minProperty().getValue();
        predicateProperty.setValue(number -> number >= min && number <= max);
    }

    public void update() {
        updateChart();

    }

    public void updateChart() {

        double min; // minimum value
        double max; // maximum value
        double range; // max - min
        double binSize;
        int binNumber = 20;
        int differentValuesCount = possibleValues
                
                .stream()
                .filter(n->Double.isFinite(n.doubleValue()))
                .collect(Collectors.toSet()).size();
        if (differentValuesCount < binNumber) {
            binNumber = differentValuesCount;
        }

        EmpiricalDistribution distribution = new EmpiricalDistribution(binNumber);

        Double[] values = possibleValues
                .stream()
                .filter(n->Double.isFinite(n.doubleValue()))
                .map(v -> v.doubleValue())
                .sorted()
                //.toArray();
                .toArray(size -> new Double[size]);
        distribution.load(ArrayUtils.toPrimitive(values));

        min = values[0];
        max = values[values.length - 1];
        range = max - min;
        binSize = range / (binNumber - 1);

        System.out.println(String.format("min = %.0f, max = %.0f, range = %.0f, bin size = %.0f, bin number = %d", min, max, range, binSize, binNumber));

        XYChart.Series<Double, Double> serie = new XYChart.Series<>();
        ArrayList<Data<Double, Double>> data = new ArrayList<>();
        double k = min;
        for (SummaryStatistics st : distribution.getBinStats()) {
            data.add(new Data<>(k, new Double(st.getN())));
            System.out.println(k);
            System.out.println(st.getN());
            k += binSize;
        }

        serie.getData().addAll(data);
        areaChart.getData().clear();

       
        areaChart.getData().add(serie);
        
        
        updateSlider(min, max, binNumber);

    }

    public void updateSlider(double min, double max, int bins) {

        rangeSlider.setPadding(new Insets(0, 20, 0, 20));
        double range = Math.abs(max - min);
        double majorTick = range / bins;
        int minorTick = 1;
        if(range < 5) {
            
            majorTick = 0.5;
        }
        
        if(range <= 1) {
            majorTick = 0.1;
            minorTick = 10;
        }
        
        rangeSlider.setMin(min);
        rangeSlider.setMax(max);
        rangeSlider.setLowValue(min);
        rangeSlider.setHighValue(max);

        rangeSlider.setMajorTickUnit(majorTick);
        rangeSlider.setMinorTickCount(minorTick);
        rangeSlider.setLabelFormatter(new Converter());
        rangeSlider.setSnapToTicks(true);
        
       
        
    }

    private class Converter extends StringConverter<Number> {

        @Override
        public String toString(Number object) {
            
            if(object.doubleValue() <= 1.0) {
                return String.format("%.1f",object.doubleValue());
            }
            return "" + object.doubleValue();
        }

        @Override
        public Number fromString(String string) {
            return new Double(string);
        }

    }

    private void onLowHighValueChanged(Observable value, Boolean oldValue, Boolean newValue) {

        System.out.println("changing predicate !");
        
        // if it's currently changing we don't want to update the predicate
        if(newValue) return;
        
        final double min = minProperty().getValue();
        final double max = maxProperty().getValue();
        System.out.printf("Min : %.3f, Max : %.3f\n",min,max);
        // no predicate is necessary if there the range is full
        if (min == rangeSlider.getMin() && max == rangeSlider.getMax()) {

            predicateProperty.setValue(null);
            return;
        } else {
            predicateProperty.setValue(new IntervalPredicate(min, max));
        }

    }

    private class IntervalPredicate implements Predicate<Double> {

        private final double min;
        private final double max;

        public IntervalPredicate(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean test(Double t) {
            return t >= min && t <= max;
        }

    }
    
    // when the low and high of the range change, we count the number of elements
    // inside the range
    private void onLowHighValueChanged(Observable obs, Number oldValue, Number newValue) {
        new CallbackTask<Collection<? extends Number>,Long>()
                .setInput(possibleValues)
                .run(this::countElementsInRange)
                .then(this::updateCountLabel)
                .start();        
    }
    
    // 
    private Long countElementsInRange(Collection<? extends Number> possibleValues) {
        return possibleValues
                .parallelStream()
                .filter(n-> n.doubleValue() >= rangeSlider.getLowValue() && n.doubleValue() <= rangeSlider.getHighValue())
                .count();
    }
    private void updateCountLabel(long count) {
        if(count == possibleValues.size()) {
            valueCountLabel.setText("All elements selected.");
            
        }
        else {
            valueCountLabel.setText(String.format("%d elements (%.0f%%)",count,1.0 * count / possibleValues.size()*100));
        }
    }

}
