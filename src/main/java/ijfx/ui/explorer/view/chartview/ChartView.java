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
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import loci.poi.hssf.record.RecalcIdRecord;
import mongis.utils.FXUtilities;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author tuananh
 */
@Plugin(type = ExplorerView.class)

public class ChartView extends FilterView implements ExplorerView {
    
    @Parameter
    ClustererService clustererService;
    
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
        final NumberAxis xAxis = new NumberAxis(0, 10, 1);
        final NumberAxis yAxis = new NumberAxis(-100, 500, 100);
        
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
            c.getItems().clear();
            c.getItems().addAll(metadataList);
        });
        
    }
    
    @Override
    public List<? extends Explorable> getSelectedItems() {
        return currentItems
                .stream()
                .map(item -> (Explorable) item)
                .filter(item -> item.selectedProperty().getValue())
                .collect(Collectors.toList());
    }
    
    @Override
    public void setSelectedItem(List<? extends Explorable> items) {
    }
    
    private void addDataToChart(List<Explorable> list, Color color) {
        
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
        
//        series.getNode().setOnMouseClicked(e -> {
//            list.stream().forEach(f -> f.selectedProperty().setValue(true));
//        });
        
    }
    
    public Data<Number, Number> transform(Explorable explorable) {
        
        String xKey = xComboBox.getSelectionModel().getSelectedItem();
        String yKey = yComboBox.getSelectionModel().getSelectedItem();
        
        return new Data<>(explorable.getMetaDataSet().get(xKey).getDoubleValue(), explorable.getMetaDataSet().get(yKey).getDoubleValue());
    }

    /**
     * Perform a clustering algorithm and load the series.
     */
    public void computeItems() {

//        
//        if(xComboBox.getSelectionModel().getSelectedItem() == null) return;
//        if(yComboBox.getSelectionModel().getSelectedItem() == null) return;
//        
//        Series<Number,Number> series = new Series<>();
//        
//        series.getData().addAll(
//                
//                currentItems
//                        .parallelStream()
//                        .map(this::transform)
//                        .collect(Collectors.toList())
//        );
//        
//        scatterChart.getData().clear();
//        scatterChart.getData().add(series);
        if (metadatas[0] != null && metadatas[1] != null) {
            
            scatterChart.getData().clear();
            
            List<List<Explorable>> clustersList = clustererService.buildClusterer(currentItems, Arrays.asList(metadatas));
            ColorGenerator colorGenerator = new ColorGenerator(clustersList.size());
            colorGenerator.generateColor();
            List<Color> colors = colorGenerator.getColorList();
            for (int i = 0; i < clustersList.size(); i++) {
                addDataToChart(clustersList.get(i), colors.get(i));
            }

//            Set<Node> items = scatterChart.lookupAll("Label.chart-legend-item");
//            int i = 0;
//            for (Node item : items) {
//                Label label = (Label) item;
//                final Rectangle rectangle = new Rectangle(10, 10, colors.get(i));
//                label.setGraphic(rectangle);
//                i++;
//            }
        }
    }
    
    @Override
    public void initComboBox() {
        xComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> {
            metadatas[0] = n;
            computeItems();
        });
        yComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> {
            metadatas[1] = n;
            computeItems();
        });
    }
    
}
