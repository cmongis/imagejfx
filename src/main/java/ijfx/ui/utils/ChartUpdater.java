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
package ijfx.ui.utils;

import ijfx.service.DefaultTimer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import mongis.utils.CallbackTask;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 *
 * @author cyril
 */
public class ChartUpdater {

    final AreaChart<Double, Double> areaChart;

    List<Double> possibleValues;

    int maximumBinNumber = 40;
    
    public ChartUpdater(AreaChart<Double, Double> areaChart) {
        this.areaChart = areaChart;
    }

    public void setPossibleValue(List<Double> values) {
        possibleValues = values;
    }

    public void setMaximumBinNumber(int maximumBinNumber) {
        this.maximumBinNumber = maximumBinNumber;
    }

    
    
    
    public void updateChart() {

       
        final double min; // minimum value
        final double max; // maximum value
        double range; // max - min
        final double binSize;
       // int maximumBinNumber = 30;
        int finalBinNumber;

        int differentValuesCount = possibleValues
                .stream()
                .filter(n -> Double.isFinite(n.doubleValue()))
                .collect(Collectors.toSet()).size();
        if (differentValuesCount < maximumBinNumber) {
            finalBinNumber = differentValuesCount;
        } else {
            finalBinNumber = maximumBinNumber;
        }

        EmpiricalDistribution distribution = new EmpiricalDistribution(finalBinNumber);

        Double[] values = possibleValues
                .parallelStream()
                .filter(n -> Double.isFinite(n.doubleValue()))
                .map(v -> v.doubleValue())
                .sorted()
                //.toArray();
                .toArray(size -> new Double[size]);
        distribution.load(ArrayUtils.toPrimitive(values));

        min = values[0];
        max = values[values.length - 1];
        range = max - min;
        binSize = range / (finalBinNumber - 1);

        //System.out.println(String.format("min = %.0f, max = %.0f, range = %.0f, bin size = %.0f, bin number = %d", min, max, range, binSize, finalBinNumber));

        XYChart.Series<Double, Double> serie = new XYChart.Series<>();
        ArrayList<XYChart.Data<Double, Double>> data = new ArrayList<>();
        double k = min;
        for (SummaryStatistics st : distribution.getBinStats()) {
            data.add(new XYChart.Data<>(k, new Double(st.getN())));
            k += binSize;
        }

        Platform.runLater(() -> {
            serie.getData().addAll(data);
            areaChart.getData().clear();
            areaChart.getData().add(serie);
        });

    }
}
