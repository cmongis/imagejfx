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
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import mongis.utils.FXUtilities;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.controlsfx.control.RangeSlider;

/**
 *
 * @author cyril
 */
public class DefaultNumberFilter extends BorderPane implements NumberFilter{

    
    
    RangeSlider rangeSlider = new RangeSlider();

    ObjectProperty<Predicate<Double>> predicateProperty = new SimpleObjectProperty<>();
    
    
    Collection< ? extends Number> possibleValues;
    
    @FXML
    CategoryAxis categoryAxis;
    
    @FXML
    NumberAxis numberAxis;
    
    @FXML
    BarChart<String,Double> barChart;
    
    
    
    public DefaultNumberFilter() {
        try {
            FXUtilities.injectFXML(this);
           
            
            setBottom(rangeSlider);
            
            barChart.setBarGap(0);
            rangeSlider.setShowTickLabels(true);
            
            
            barChart.setOnMouseClicked(event->update());
            
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
        updateSlider();
    }

    @Override
    public Property<Predicate<Double>> predicateProperty() {
        return predicateProperty;
    }
    
    public void updatePredicate() {
        final double min = minProperty().getValue();
        final double max = minProperty().getValue();
        predicateProperty.setValue(number->number >= min && number <= max);
    }
    
    public void update() {
        updateChart();
        updateSlider();
    }
    
    public void updateChart() {
       
        
        double min; // minimum value
        double max; // maximum value
        double range; // max - min
        double binSize;
        int binNumber = 20;
        
         EmpiricalDistribution distribution = new EmpiricalDistribution(binNumber);
        
        Double[] values = possibleValues
                .stream()
                .map(v->v.doubleValue())
                .sorted()
            
                //.toArray();
                .toArray(size->new Double[size]);
        distribution.load(ArrayUtils.toPrimitive(values));
        
         min = values[0];
         max = values[values.length-1];
        range = max - min;
        binSize = range/binNumber;
        
        
        XYChart.Series<String,Double> serie = new XYChart.Series<>();
        ArrayList<Data<String,Double>> data = new ArrayList<>(); 
        double k = min;
        for (SummaryStatistics st : distribution.getBinStats()) {
            data.add(new Data<String,Double>(new Double(k).toString(), new Double(st.getN())));
            k += binSize;
        }
            
          
        
        serie.getData().addAll(data);
        barChart.getData().clear();
        barChart.getData().add(serie);
        
    }
    
    public void updateSlider() {
        
        SummaryStatistics stats = new SummaryStatistics();
        
        possibleValues.forEach(n->stats.addValue(n.doubleValue()));
        
        double min = stats.getMin();
        double max = stats.getMax();
        double range = max - min;
        double minorTick = range / 40;
        double majorTick = range / 10;
        rangeSlider.setMin(min);
        rangeSlider.setMax(max);
        rangeSlider.setLowValue(min);
        rangeSlider.setHighValue(max);
        
        rangeSlider.setMajorTickUnit(majorTick);
        rangeSlider.setMinorTickCount(2);
        rangeSlider.setLabelFormatter(new Converter());
        
    }
    
    private class Converter extends StringConverter<Number> {

        @Override
        public String toString(Number object) {
            return ""+object.intValue();
        }

        @Override
        public Number fromString(String string) {
            return new Double(string);
        }
        
    }
}

