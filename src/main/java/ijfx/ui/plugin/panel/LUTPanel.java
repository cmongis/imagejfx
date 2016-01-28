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
package ijfx.ui.plugin.panel;

import ijfx.plugins.commands.ApplyLUT;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import ijfx.ui.plugin.LUTComboBox;
import ijfx.ui.plugin.LUTView;
import ijfx.service.display.DisplayRangeService;
import ijfx.service.ui.FxImageService;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.lut.LUTService;
import net.imglib2.display.ColorTable;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.RangeSlider;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayActivatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;
import ijfx.ui.UiPlugin;
import ijfx.ui.UiConfiguration;
import ijfx.ui.project_manager.search.PopoverToggleButton;
import javafx.beans.Observable;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS
 *
 * The LUT Panel display the minimum and maximum pixel value of the current
 * image. It also proposes a Range slider allowing the user to change the
 * minimum and maximum displayed values, along with the LUT.
 *
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "lutPanel", context = "imagej+image-open -overlay-selected", localization = Localization.RIGHT, order = 0)
public class LUTPanel extends TitledPane implements UiPlugin {

    LUTComboBox lutComboBox;

    /*
     ImageJ Services (automatically injected)
     */
    @Parameter
    Context context;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    DisplayService displayService;

    @Parameter
    EventService eventService;

    @Parameter
    LUTService lutService;

    @Parameter
    ThreadService threadService;

    @Parameter
    DisplayRangeService displayRangeServ;

    @Parameter
    CommandService commandService;

    @Parameter
    FxImageService fxImageService;
    @Parameter
    DatasetService datasetService;

    Node lutPanelCtrl;

    @FXML
    VBox vbox;

    
    @FXML
    ToggleButton minMaxButton;

    RangeSlider rangeSlider = new RangeSlider();

   // PopOver popOver;

    Logger logger = ImageJFX.getLogger();

    GridPane gridPane = new GridPane();

    Label minLabel = new Label();
    Label maxLabel = new Label();

    public static final String LUT_COMBOBOX_CSS_ID = "lut-panel-lut-combobox";
    
    
    protected DoubleProperty minValue = new SimpleDoubleProperty(0);
    protected DoubleProperty maxValue = new SimpleDoubleProperty(255);

    public LUTPanel() {

        logger.info("Loading FXML");

        try {
            FXUtilities.injectFXML(this);

            logger.info("FXML loaded");

            
            // creating the panel that will be hold by the popover
            VBox vboxp = new VBox();
            vboxp.getChildren().addAll(rangeSlider, gridPane);
           
            // adding the vbox class to the vbox;
            
            
            // associating min and max value to properties
            minValue = rangeSlider.lowValueProperty();
            maxValue = rangeSlider.highValueProperty();
            
            // taking care of the range slider configuration
            rangeSlider.setShowTickLabels(true);
            rangeSlider.setPrefWidth(255);
            minMaxButton.setPrefWidth(200);

           

            
            
            gridPane.add(new Label("Min : "), 0, 0);
            gridPane.add(new Label("Max : "), 0, 1);
            gridPane.add(minLabel, 1, 0);
            gridPane.add(maxLabel, 1, 1);

            

            gridPane.setHgap(15);
            gridPane.setVgap(15);

            // Adding listeners
            logger.info("Adding listeners");

            rangeSlider.highValueChangingProperty().addListener(event -> onHighValueChanging());
            rangeSlider.lowValueChangingProperty().addListener(event -> onLowValueChanging());

            minValue.addListener((obs, oldValue, newValue) -> {
                minLabel.setText(newValue.intValue() + "");

            });
            maxValue.addListener((obs, oldValue, newValue) -> {
                maxLabel.setText(newValue.intValue() + "");

            });
            // setting some insets... should be done int the FXML
            vboxp.setPadding(new Insets(15));
            
            
            PopoverToggleButton.bind(minMaxButton, vboxp, PopOver.ArrowLocation.RIGHT_TOP);
            
            
            
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        // System.out.println(label);
    }

    public void buildComboBox() {

        /*
        lutComboBox.setOnAction(event -> {
            applyLUT(lutComboBox.getSelectionModel().getSelectedItem().getColorTable());
        });*/
        
        lutComboBox.setId("lutPanel");
        
        lutComboBox.getSelectionModel().selectedItemProperty().addListener(this::onComboBoxChanged);
       
        
     
        
        lutComboBox.getItems().addAll(FxImageService.getLUTViewMap().values());

    }
    
    
    public void onComboBoxChanged(Observable observable, LUTView oldValue, LUTView newValue) {
        if(newValue != null && newValue.getColorTable() != null) {
            applyLUT(newValue.getColorTable());
        }
    }

    public void applyLUT(ColorTable table) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("colorTable", table);
        commandService.run(ApplyLUT.class, true, params);

        //lutService.applyLUT(table, displayService.getActiveDisplay(ImageDisplay.class));
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {

        lutComboBox = new LUTComboBox();
        context.inject(lutComboBox);
        lutComboBox.init();
        lutComboBox.setPrefWidth(200);
        lutComboBox.setMinWidth(200);
        buildComboBox();
        vbox.getChildren().add(lutComboBox);

        return this;
    }

    

    public void updateModelRangeFromView() {
        //System.out.println(minValue);
        //System.out.println(maxValue);
        if (rangeSlider.isLowValueChanging() || rangeSlider.isHighValueChanging()) {
            return;
        }
        threadService.queue(() -> {

            displayRangeServ.updateCurrentDisplayRange(minValue.doubleValue(), maxValue.doubleValue());
        });

        updateLabel();

    }

    public void updateViewRangeFromModel() {

        double min = displayRangeServ.getCurrentViewMinimum();
        double max = displayRangeServ.getCurrentViewMaximum();

    
        rangeSlider.setMin(displayRangeServ.getCurrentDatasetMinimum());
        rangeSlider.setMax(displayRangeServ.getCurrentDatasetMaximum());

        rangeSlider.setMajorTickUnit(displayRangeServ.getCurrentDatasetMaximum() - displayRangeServ.getCurrentDatasetMinimum());
        minValue.set(min);
        maxValue.set(max);

        updateLabel();

    }

    public void updateLabel() {

        double min = displayRangeServ.getCurrentViewMinimum();
        double max = displayRangeServ.getCurrentViewMaximum();

        Platform.runLater(() -> {
            minMaxButton.setText(String.format("Min/Max : %.0f - %.0f", min, max));
        });
    }

    @EventHandler
    public void handleEvent(DisplayActivatedEvent event) {

        updateViewRangeFromModel();
        updateLabel();
    }

    @EventHandler
    public void handleEvent(DataViewUpdatedEvent event) {

        updateLabel();
        updateViewRangeFromModel();

    }

    private void onHighValueChanging() {
        //System.out.println("state changed !");
        updateModelRangeFromView();
        updateLabel();
    }

    private void onLowValueChanging() {
        //System.out.println("state changed");
        updateModelRangeFromView();
        updateLabel();
    }

    @FXML
    private void autoRange(ActionEvent event) {

        displayRangeServ.autoRange();
        updateViewRangeFromModel();
        updateLabel();
    }

}
