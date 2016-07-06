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
package ijfx.ui.explorer.view;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.listenableSystem.MetaDataSetUtils;
import ijfx.service.cluster.ObjectClusterable;
import ijfx.service.cluster.PCAProcesser;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.ExplorerView;
import ijfx.ui.explorer.view.chartview.DefaultPlotExplorer;
import ijfx.ui.explorer.view.chartview.PlotExplorer;
import ijfx.ui.explorer.view.chartview.TogglePlot;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = ExplorerView.class)
public class PCAChartView extends AnchorPane implements ExplorerView {

    @FXML
    ScatterChart<Number, Number> scatterChart;

    @FXML
    ListView<String> listView;

    PCAProcesser pCAProcesser;

    @Parameter
    ExplorerService explorerService;

    private List<String> metadataList;

    private List<? extends Explorable> currentItems;

    public PCAChartView() {
        super();
        metadataList = new ArrayList<>();
        pCAProcesser = new PCAProcesser();
        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/explorer/view/PCAChartView.fxml");
        } catch (IOException ex) {
            Logger.getLogger(GridIconView.class.getName()).log(Level.SEVERE, null, ex);

        }
        scatterChart.setTitle("PCAChartView");
        scatterChart.getXAxis().labelProperty().bind(pCAProcesser.xAxeProperty());
        scatterChart.getYAxis().labelProperty().bind(pCAProcesser.yAxeProperty());

        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().getSelectedItems().addListener(this::listViewListener);
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public Node getIcon() {
        return new FontAwesomeIconView(FontAwesomeIcon.BED);
    }

    @Override
    public void setItem(List<? extends Explorable> items) {
        currentItems = items;
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        String s = listView.getSelectionModel().getSelectedItem();
        listView.getItems().clear();
        listView.setItems(FXCollections.observableArrayList(this.getMetaDataKey(items)));
        if (metadataList.contains(s)) {
            listView.getSelectionModel().select(s);
        }

    }

    @Override
    public List<? extends Explorable> getSelectedItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSelectedItem(List<? extends Explorable> items) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ArrayList<String> getMetaDataKey(List<? extends Explorable> items) {
        ArrayList<String> keyList = new ArrayList<String>();
        items.forEach(plane -> {
            plane.getMetaDataSet().keySet().forEach(key -> {

                if (!keyList.contains(key)) {
                    keyList.add(key);
                }
            });
        });
        Collections.sort(keyList);
        return keyList;
    }

    public void displayClusterable(List<ObjectClusterable> objectClusterables) {
        List<Data> listExplorers = objectClusterables
                .stream()
                .map(e -> {
                    PlotExplorer plotExplorer = new DefaultPlotExplorer(explorerService, (Explorable) e.getObject(), e.value(0), e.value(1));
                    return plotExplorer.getData();
                })
                .collect(Collectors.toList());

        listExplorers.stream()
                .forEach(e -> System.out.println(e.getXValue()));
        Series series = new XYChart.Series();

        series.getData().addAll(listExplorers);
        scatterChart.getData().add(series);
        series.setName("Tuan anh is awesome");
        bindLegend();
    }

    public void listViewListener(ListChangeListener.Change<? extends String> change) {
        System.out.println(listView.getSelectionModel().getSelectionMode().toString());
//        listView.getSelectionModel().getSelectedItems().stream().forEach(e -> System.out.println(e));
        metadataList.clear();
        metadataList.addAll(change.getList());
        metadataList.stream().forEach(e -> System.out.println(e));
        List<ObjectClusterable> objectClusterables = currentItems
                .stream()
                .map(e -> new ObjectClusterable(e, 1.0, MetaDataSetUtils.getMetadatas(e, metadataList)))
                .collect(Collectors.toList());
        try {
            if (metadataList.size() > 1) {
                scatterChart.getData().clear();
                List<ObjectClusterable> result = pCAProcesser.applyPCA(objectClusterables, metadataList);
                displayClusterable(result);
            }
        } catch (Exception ex) {
            Logger.getLogger(PCAChartView.class.getName()).log(Level.SEVERE, null, ex);
        }
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
