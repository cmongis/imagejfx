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
import ijfx.service.cluster.ExplorableClustererService;
import ijfx.service.ui.FxImageService;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.ExplorerView;
import ijfx.ui.explorer.view.GridIconView;
import ijfx.ui.plugin.LUTComboBox;
import ijfx.ui.plugin.LUTView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import mongis.utils.ColorTableUtil;
import mongis.utils.FXUtilities;
import net.imglib2.converter.RealLUTConverter;
import net.imglib2.display.ColorTable;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = ExplorerView.class)
public class ChartViewLUT<T extends RealType<T>> extends AbstractChartView implements ExplorerView {

    @Parameter
    ExplorableClustererService explorableClustererService;

    @Parameter
    ExplorerService explorerService;

    String[] metadatas;
    @FXML
    ComboBox<String> xComboBox;

    @FXML
    ComboBox<String> yComboBox;

    @FXML
    ComboBox<String> thirdComboBox;

    @FXML
    LUTComboBox lutComboBox;

    List<ComboBox<String>> comboBoxList;
    double min = 0;
    double max = 0;

    public ChartViewLUT() {
        super();
        lutComboBox = new LUTComboBox();

        comboBoxList = new ArrayList<>();
        metadatas = new String[3];

        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/explorer/view/ChartViewLUT.fxml");
        } catch (IOException ex) {
            Logger.getLogger(GridIconView.class.getName()).log(Level.SEVERE, null, ex);
        }
        comboBoxList.add(xComboBox);
        comboBoxList.add(yComboBox);
        comboBoxList.add(thirdComboBox);
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
        lutComboBox.getItems().addAll(FxImageService.getLUTViewMap().values());
        List<String> metadatas = explorerService.getMetaDataKey(currentItems);

        comboBoxList.stream().forEach(c -> {
            String s = c.getSelectionModel().getSelectedItem();
            c.getItems().clear();
            c.getItems().addAll(metadatas);
            if (metadatas.contains(s)) {
                c.getSelectionModel().select(s);
            }

        });
//        computeItems();
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
        explorerService.context().inject(lutComboBox);

        currentItems
                .stream()
                .forEach(e -> e.selectedProperty().setValue(true));
    }

    @Override
    public void computeItems() {

        scatterChart.getData().clear();
        addDataToChart(currentItems, Arrays.asList(metadatas));
        bindLegend();

    }

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
        thirdComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> {
            metadatas[2] = n;
            deselecItems();
            computeItems();
            setMinMax(n);
        });
        lutComboBox.getSelectionModel().selectedItemProperty().addListener(this::onComboBoxChanged);
    }

    public void deselecItems() {
        currentItems.stream()
                .forEach(e -> e.selectedProperty().setValue(false));
    }

    public void onComboBoxChanged(Observable observable, LUTView oldValue, LUTView newValue) {
        ColorTable colorTable = newValue.getColorTable();
        RealLUTConverter<T> realLUTConverter = new RealLUTConverter<>(min, max, colorTable);
        scatterChart.getData().get(0).getData().stream()
                .forEach((Data e) -> {
                    double extraValue = (double) e.getExtraValue();
                    ARGBType argbType = new ARGBType();
                    realLUTConverter.convert((T) new DoubleType(extraValue), argbType);
                    int value = argbType.get();

                    final int red = (value >> 16) & 0xff;
                    final int green = (value >> 8) & 0xff;
                    final int blue = value & 0xff;
//                    e.getNode().getStyleClass().clear();
                    TogglePlot t = (TogglePlot) e.getNode();
                    t.setStyle("-fx-background-color: rgb("+red+","+green+","+blue+")");
            System.out.println(t.getStyle());
                });
//        realLUTConverter.convert(1, output);
    }

    private void setMinMax(String newValue) {
        currentItems.stream()
                .forEach(e -> {
                    double value = e.getMetaDataSet().get(newValue).getDoubleValue();
                    min = min > value ? value : min;
                    max = max < value ? value : max;
                });
    }
}