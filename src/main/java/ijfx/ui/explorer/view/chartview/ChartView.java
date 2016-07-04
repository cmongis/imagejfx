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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.service.cluster.ClustererService;
import ijfx.service.cluster.ExplorableClustererService;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.ExplorerView;
import ijfx.ui.explorer.view.FilterView;
import ijfx.ui.explorer.view.GridIconView;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import mongis.utils.FXUtilities;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = ExplorerView.class)

public class ChartView extends FilterView implements ExplorerView {

    @Parameter
    ExplorableClustererService explorableClustererService;

    @Parameter
    ExplorerService explorerService;

    @FXML
    ScatterChart<Number, Number> scatterChart;

    @FXML
    ComboBox<String> xComboBox;

    @FXML
    ComboBox<String> yComboBox;

    String[] metadatas;
    private List<? extends Explorable> currentItems;

    public ChartView() {
        super();
        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/explorer/view/ChartView.fxml");
        } catch (IOException ex) {
            Logger.getLogger(GridIconView.class.getName()).log(Level.SEVERE, null, ex);
        }

        comboBoxList.add(xComboBox);
        comboBoxList.add(yComboBox);
        metadatas = new String[2];

        scatterChart.setTitle("ChartView");
        scatterChart.getXAxis().labelProperty().bind(xComboBox.getSelectionModel().selectedItemProperty());
        scatterChart.getYAxis().labelProperty().bind(yComboBox.getSelectionModel().selectedItemProperty());
        initComboBox();
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public Node getIcon() {
        return new FontAwesomeIconView(FontAwesomeIcon.MUSIC);
    }

    @Override
    public void setItem(List<? extends Explorable> items) {
        currentItems = items;
        computeItems();

        List<String> metadataList = this.getMetaDataKey(items);

        comboBoxList.stream().forEach(c -> {
            String s = c.getSelectionModel().getSelectedItem();
            c.getItems().clear();
            c.getItems().addAll(metadataList);
            if (metadataList.contains(s)) {
                c.getSelectionModel().select(s);
            }
        });
    }

    @Override
    public List<? extends Explorable> getSelectedItems() {
        return currentItems
                .stream()
                .map(item -> (Explorable) item)
                .filter(item -> item.selectedProperty().getValue() == true)
                .collect(Collectors.toList());
    }

    @Override
    public void setSelectedItem(List<? extends Explorable> items) {
    }

    private void addDataToChart(List<? extends Explorable> list, Color color) {

        Series series = new Series();
        series.setNode(new Button("e"));
        List<Data> listExplorers = list
                .stream()
                .map(e -> {
                    PlotExplorer plotExplorer = new DefaultPlotExplorer(e, metadatas, explorerService);
                    return plotExplorer.getData();
                })
                .collect(Collectors.toList());
        series.getData().addAll(listExplorers);
        scatterChart.getData().add(series);
        series.setName("Tuan anh is awesome");

    }

//   
    /**
     * Perform a clustering algorithm and load the series.
     */
    public void computeItems() {

        if (metadatas[0] != null && metadatas[1] != null) {

            scatterChart.getData().clear();

            List<List<? extends Explorable>> clustersList = explorableClustererService.clusterExplorable(currentItems, Arrays.asList(metadatas));
            ColorGenerator colorGenerator = new ColorGenerator(clustersList.size());
            colorGenerator.generateColor();
            List<Color> colors = colorGenerator.getColorList();
            for (int i = 0; i < clustersList.size(); i++) {
                addDataToChart(clustersList.get(i), colors.get(i));
            }

            bindLegend();
        }
    }

    @Override
    public void initComboBox() {
        xComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> {
            metadatas[0] = n;
            deselecItems();
            computeItems();
        });
        yComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> {
            metadatas[1] = n;
            deselecItems();
            computeItems();
        });
    }

    public void deselecItems() {
        currentItems.stream()
                .forEach(e -> e.selectedProperty().set(false));
    }

    public void bindLegend() {
        for (Series series : scatterChart.getData()) {

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
                        TogglePlot togglePlotData = (TogglePlot) ((Data) e).getNode();
                        togglePlot.bind(togglePlotData);
                    });
                    break;
                }
            }
        }
    }

}
