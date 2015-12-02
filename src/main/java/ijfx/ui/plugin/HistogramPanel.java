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
package ijfx.ui.plugin;

import ijfx.ui.UiPlugin;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import ijfx.ui.plugin.panel.HistogramModel;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import mercury.core.MercuryTimer;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imglib2.histogram.Histogram1d;
import org.scijava.Context;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import ijfx.ui.UiConfiguration;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "histrogram-panel", context = "", localization = Localization.RIGHT, order = 2.0)
public class HistogramPanel extends AbstractContextButton {

    @Parameter
    DisplayService displayService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    Context context;

    VBox vbox;

    BarChart<String, Number> barChart;

    public HistogramPanel() {
        super("Histogram", FontAwesomeIcon.BAR_CHART);

        vbox = new VBox();
        vbox.getStyleClass().add(ImageJFX.VBOX_CLASS);

        vbox.getChildren().add(getButton());

    }

    public Node getUiElement() {
        return vbox;
    }

    @Override
    public void onAction(ActionEvent event) {

        HistogramModel model = new HistogramModel();

        context.inject(model);

        model.setDisplay(displayService.getActiveDisplay(ImageDisplay.class));
        MercuryTimer timer = new MercuryTimer("Chart building");
        timer.start();
        model.build();
        timer.elapsed("Building");

       

        buildChart(model.getHistogram()[0]);
        timer.elapsed("Displaying");
    }

    @Override
    public UiPlugin init() {
        return this;
    }

    public void buildChart(Histogram1d histogram) {

        if (barChart == null) {
            vbox.getChildren().remove(barChart);
            barChart = null;

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis(0, 10000, 1);
            xAxis.setLabel("intensity");
            yAxis.setLabel("count");
            barChart = new BarChart<String, Number>(xAxis, yAxis);

            vbox.getChildren().add(barChart);
        }

        barChart.getXAxis().setTickLength(10);
        barChart.getXAxis().setTickMarkVisible(false);
        while (barChart.getData().size() > 0) {
            barChart.getData().remove(0);
        }
        XYChart.Series<String, Number> serie1 = new XYChart.Series<>();
        String[] pixels = new String[(int) histogram.getBinCount()];

        for (int i = 0; i != pixels.length; i++) {
            pixels[i] = Integer.toString(i);
            serie1.getData().add(new XYChart.Data<String, Number>(pixels[i], histogram.frequency(i)));
        }

        barChart.getData().add(serie1);

    }

}
