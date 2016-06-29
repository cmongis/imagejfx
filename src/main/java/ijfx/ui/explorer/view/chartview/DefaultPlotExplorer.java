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
import javafx.scene.Node;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mongis.utils.CallbackTask;
import org.controlsfx.control.PopOver;

/**
 *
 * @author Tuan anh TRINH
 */
public class DefaultPlotExplorer implements PlotExplorer {

    private Explorable explorable;

    private Data data;

    private double x;

    private double y;

    private PopOver popOver;

    ImageView imageView;

    public DefaultPlotExplorer(double width, double height) {

    }

    public DefaultPlotExplorer(Explorable explorable, double width, double height) {
        this.explorable = explorable;
    }

    public DefaultPlotExplorer(Explorable explorable, String[] metadataKeys, Node node) {
        this.explorable = explorable;
        x = explorable.getMetaDataSet().get(metadataKeys[0]).getDoubleValue();
        y = explorable.getMetaDataSet().get(metadataKeys[1]).getDoubleValue();
        this.data = new Data(x, y);
        this.data.setNode(node);
        setPopOver(this.data.getNode());
        this.data.getNode().setOnMouseClicked(e -> {
            actionPopOver();
            this.explorable.selectedProperty().set(true);
                });

    }

//    public void onAction(Explorable explorable, Data data) {
//        data.getNode().setOnMouseClicked(e -> explorable.getImage());
//    }
    @Override
    public Explorable getExplorable() {
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
            if (imageView == null) setContent();
            popOver.show(this.data.getNode());
        }
    }

   

    protected void setContent() {
        new CallbackTask<Explorable, Image>(explorable)
                .run(( exp,e) -> e.getImage())
                .then(e -> {
                    imageView = ImageViewBuilder.create().image(e).build();
                    popOver.setContentNode(imageView);
                })
                .start();
    }
}
