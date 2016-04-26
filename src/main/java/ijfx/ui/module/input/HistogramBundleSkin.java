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
package ijfx.ui.module.input;

import ijfx.ui.module.InputSkinPlugin;
import ijfx.ui.module.input.Input;
import ijfx.ui.module.skin.AbstractInputSkinPlugin;
import java.util.ArrayList;
import java.util.stream.IntStream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import mercury.core.MercuryTimer;
import net.imagej.widget.HistogramBundle;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */

@Plugin(type = InputSkinPlugin.class)
public class HistogramBundleSkin extends AbstractInputSkinPlugin<HistogramBundle> {

    LineChart<Number, Number> lineChart;

    ObjectProperty<HistogramBundle> histogramProperty = new SimpleObjectProperty();

    final NumberAxis xAxis = new NumberAxis();
    final NumberAxis yAxis = new NumberAxis();

    public HistogramBundleSkin() {
        super();

       
        
    }

    public void onClick(MouseEvent event) {
        updateChart();
    }

    @Override
    public Property<HistogramBundle> valueProperty() {
        return histogramProperty;

    }

  

    @Override
    public Node getNode() {
        return lineChart;
    }

    @Override
    public void dispose() {
    }

    public void updateChart() {
        MercuryTimer timer = new MercuryTimer("Chart");
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
        
        lineChart.getData().clear();
        
        timer.elapsed("Clearing");
        
        if(getBundle() == null) return;
        
        for (int h = 0; h < getBundle().getHistogramCount(); h++) {
            System.out.printf("h = %d\n",h);
            final XYChart.Series series = new XYChart.Series();
            long[] histogram = getBundle().getHistogram(h).toLongArray();
            double[] values = new double[histogram.length];
            
            IntStream.range(0,values.length).forEach(i->{
                values[i] = histogram[i];
            });
            double epsilon = 10;
            
            final double[] sampledValues = values;//filter.filter(values);
            //sampledValues = values;

            series.setName("Data "+h+1);
            
            ArrayList<XYChart.Data<Number,Number>> list = new ArrayList<>();
            //for(int i = 0;i<sampledValues.length;i+=1) {
            
            final int histogramId = h;
            
            IntStream.range(0,values.length).forEach(i->{    
                list.add(new XYChart.Data(getBundle().getMinBin()+i,sampledValues[i]));
            });
            //}
            lineChart.setCreateSymbols(false);
            series.getData().addAll(list);
            timer.elapsed("creating dataset");
              
            lineChart.getData().add(h,series);
          
            timer.elapsed("rendering dataset");
        }
        
    }

    public HistogramBundle getBundle() {
        return histogramProperty.getValue();
    }
    
    public boolean canHandle(Class<?> clazz) {
        return HistogramBundle.class.isAssignableFrom(clazz);
    }

    @Override
    public void init(Input<HistogramBundle> input) {
        valueProperty().setValue(input.getValue());
         lineChart = new LineChart<>(xAxis, yAxis);
         
        lineChart.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onClick);
        lineChart.setPrefWidth(300);
        lineChart.setPrefHeight(300);
        lineChart.setAnimated(false);
        
        updateChart();
        
        
    }

}
