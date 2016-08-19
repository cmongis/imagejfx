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
import ijfx.core.metadata.MetaDataSetUtils;
import ijfx.service.cluster.ClustererService;
import ijfx.service.cluster.DefaultObjectClusterable;
import ijfx.service.cluster.ObjectClusterable;
import ijfx.service.cluster.PCAProcesser;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.ExplorerView;
import ijfx.ui.explorer.view.GridIconView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = ExplorerView.class,priority = 0.4)
public class PCAChartView extends AbstractChartView implements ExplorerView {
    
    @FXML
    ListView<String> listView;
    
    PCAProcesser pCAProcesser;
    
    List<String> metadataList;
    @Parameter
    ExplorerService explorerService;
    
    @Parameter
    LoadingScreenService loadingScreenService;
    
    @Parameter
    ClustererService clustererService;
    
    public PCAChartView() {
        super();
        metadataList = new ArrayList<>();
        pCAProcesser = new PCAProcesser();
        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/explorer/view/chartview/PCAChartView.fxml");
        } catch (IOException ex) {
            Logger.getLogger(GridIconView.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        scatterChart.setTitle("PCAChartView");
        scatterChart.getXAxis().labelProperty().bind(pCAProcesser.xAxeProperty());
        scatterChart.getYAxis().labelProperty().bind(pCAProcesser.yAxeProperty());
        
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().getSelectedItems().addListener(this::listViewListener);
        setGraphicSnapshot();
    }
    
    @Override
    public Node getNode() {
        return this;
    }
    
    @Override
    public Node getIcon() {
        return new FontAwesomeIconView(FontAwesomeIcon.CALCULATOR);
    }
    
    @Override
    public void setItem(List<? extends Explorable> items) {
        currentItems = items;
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        String s = listView.getSelectionModel().getSelectedItem();
        listView.getItems().clear();
        listView.setItems(FXCollections.observableArrayList(explorerService.getMetaDataKey(items)));
        if (metadataList.contains(s)) {
            listView.getSelectionModel().select(s);
        }
        
    }
    
    @Override
    public List<? extends Explorable> getSelectedItems() {
        return currentItems.stream()
                .filter(e -> e.selectedProperty().get())
                .collect(Collectors.toList());
    }
    
    @Override
    public void setSelectedItem(List<? extends Explorable> items) {
        currentItems
                .stream()
                .forEach(e -> e.selectedProperty().setValue(true));
    }

//    public void displayCluster(List<? extends Explorable> objectClusterables) {
//        List<Data> listExplorers = objectClusterables
//                .stream()
//                .map(e -> {
//                    PlotExplorer plotExplorer = new DefaultPlotExplorer(explorerService, (Explorable) e.getObject(), e.value(0), e.value(1));
//                    return plotExplorer.getData();
//                })
//                .collect(Collectors.toList());
//
//        listExplorers.stream()
//                .forEach(e -> System.out.println(e.getXValue()));
//        Series series = new XYChart.Series();
//
//        series.getData().addAll(listExplorers);
//        scatterChart.getData().add(series);
//        series.setName("Tuan anh is awesome");
//        bindLegend();
//    }
    @Override
    protected void computeItems() {
        List<ObjectClusterable> objectClusterables = currentItems
                .stream()
                .map(e -> new DefaultObjectClusterable(e, 1.0, MetaDataSetUtils.getMetadatas(e, metadataList)))
                .collect(Collectors.toList());
        if (metadataList.size() > 1) {
            scatterChart.getData().clear();
            
            new CallbackTask<Void, Void>()
                    .then((f) -> {
                        try {
                            List<ObjectClusterable> result;
                            result = pCAProcesser.applyPCA(objectClusterables, metadataList);
                            List<List<Explorable>> explorables = clustererService.buildClusterer(result, metadataList);
                            explorables.stream().forEach(e -> addDataToChart(e, metadataList));
                            bindLegend();
                        } catch (Exception ex) {
                            Logger.getLogger(PCAChartView.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    })
                    .submit(loadingScreenService)
                    .start();
            
        }
    }
    
    public void listViewListener(ListChangeListener.Change<? extends String> change) {
//        listView.getSelectionModel().getSelectedItems().stream().forEach(e -> System.out.println(e));
        metadataList.clear();
        metadataList.addAll(change.getList());
        computeItems();
        
    }
    
    @Override
    @FXML
    protected void help() {
        hintService.displayHints(this.getClass(), true);
    }
    
}
