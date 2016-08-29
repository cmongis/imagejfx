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
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import mongis.utils.CallbackTask;
import org.controlsfx.control.PopOver;

/**
 *
 * @author Tuan anh TRINH
 */
public class DefaultPlotExplorer implements PlotExplorer {

    ExplorerService explorerService;

    private Explorable explorable;

    private Data data;

    private double x;

    private double y;

    private PopOver popOver;

    ImageView imageView;

    public DefaultPlotExplorer(ExplorerService explorerService, Explorable explorable, double x, double y) {
        this.explorerService = explorerService;
        this.explorable = explorable;
        this.x = x;
        this.y = y;
        this.data = new Data(x, y);
        addNode();
    }

    public DefaultPlotExplorer(ExplorerService explorerService, Explorable explorable, double x, double y, double extraValue) {
        this(explorerService, explorable, x, y);
        this.data.setExtraValue(extraValue);
        addNode();
    }

    public DefaultPlotExplorer(Explorable explorable, String[] metadataKeys, ExplorerService explorerService) {
        this.explorerService = explorerService;
        this.explorable = explorable;
        x = explorable.getMetaDataSet().get(metadataKeys[0]).getDoubleValue();
        y = explorable.getMetaDataSet().get(metadataKeys[1]).getDoubleValue();
        if (metadataKeys.length > 2) {
            double extra = explorable.getMetaDataSet().get(metadataKeys[2]).getDoubleValue();

            this.data = new Data(x, y, extra);

        } else {

            this.data = new Data(x, y);
        }
        addNode();
    }

    public void addNode() {
        TogglePlot togglePlot = new TogglePlot();
        togglePlot.setPrefSize(10, 10);
        togglePlot.selectedProperty().bindBidirectional(this.explorable.selectedProperty());
        togglePlot.selectedProperty().addListener((obs, old, n) -> {
            String style;
//            System.out.println(togglePlot.getStyle());
            style = (n) ? TogglePlot.DEFAULT_COLOR : togglePlot.getOriginStyle();
            togglePlot.setStyle(style);
        });
        this.data.setNode(togglePlot);
        setPopOver(this.data.getNode());
        this.data.getNode().addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    actionPopOver();
                }
            }
        });

    }

    @Override
    public Object getObject() {
        return explorable;
    }

    @Override
    public Data getData() {
        return data;
    }

    public void setPopOver(Node target) {
        popOver = new PopOver();
        popOver.setDetachable(false);
        popOver.setDetached(false);
        popOver.setCornerRadius(4);

    }

    public void actionPopOver() {
        if (popOver.isShowing()) {
            popOver.hide();
        } else {
            if (imageView == null) {
                setContent();
            }
            popOver.show(this.data.getNode());
        }
    }

    protected void setContent() {
        new CallbackTask<Explorable, Image>(explorable)
                .run((exp, e) -> e.getImage())
                .then(e -> {
                    imageView = ImageViewBuilder.create().image(e).build();
                    BorderPane borderPane = new BorderPane(imageView);
                    borderPane.setPrefWidth(imageView.getFitWidth());
                    borderPane.setTop(new Label(explorable.getTitle()));
                    borderPane.setCenter(imageView);
                    imageView.setOnMouseClicked(c -> {
                        explorerService.open(explorable);
                        popOver.hide();
                    });
                    popOver.setContentNode(borderPane);
                })
                .start();
    }
}
