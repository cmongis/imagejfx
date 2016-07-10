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
package ijfx.ui.explorer.view.chartview;

import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.view.chartview.DefaultPlotExplorer;
import ijfx.ui.explorer.view.chartview.PlotExplorer;
import ijfx.ui.explorer.view.chartview.TogglePlot;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Tuan anh TRINH
 */
public abstract class AbstractChartView extends AnchorPane {

    @Parameter
    ExplorerService explorerService;

    @FXML
    protected ScatterChart<Number, Number> scatterChart;


    protected List<? extends Explorable> currentItems;

    public AbstractChartView() {
        super();
    }
    
    protected abstract void computeItems();

    protected void addDataToChart(List<? extends Explorable> list, List<String> metadataList) {

        XYChart.Series series = new XYChart.Series();
//        series.setNode(new Button("e"));
        List<XYChart.Data> listExplorers = new ArrayList<>();
        list.stream()
                .map(e -> {
                    System.out.println(metadataList.get(0));
                    PlotExplorer plotExplorer = new DefaultPlotExplorer(e, metadataList.toArray(new String[0]), explorerService);
                    return plotExplorer.getData();
                })
                .forEach(e -> listExplorers.add(e));
        series.getData().addAll(listExplorers);
        scatterChart.getData().add(series);
        System.out.println("ijfx.ui.explorer.view.AbstractChartView.addDataToChart()");
        series.setName("Tuan anh is awesome");

    }

    public void bindLegend() {
        for (XYChart.Series series : scatterChart.getData()) {

            Node node = scatterChart.lookup(".series" + scatterChart.getData().indexOf(series));
            Set<Node> legendItems = scatterChart.lookupAll("Label.chart-legend-item");

            for (Node legend : legendItems) {
                Label labelLegend = (Label) legend;
                if (node.getStyleClass().get(1).equals(labelLegend.getGraphic().getStyleClass().get(2))) {
                    TogglePlot togglePlot = new TogglePlot();
                    togglePlot.getStyleClass().clear();
                    togglePlot.getStyleClass().addAll(labelLegend.getGraphic().getStyleClass());
                    labelLegend.setGraphic(togglePlot);
                    series.getData().stream().forEach(e -> {
                        TogglePlot togglePlotData = (TogglePlot) ((XYChart.Data) e).getNode();
                        togglePlot.bind(togglePlotData);
                    });
                    break;
                }
            }
        }
    }

}
