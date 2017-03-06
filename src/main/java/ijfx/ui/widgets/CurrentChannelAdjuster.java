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
package ijfx.ui.widgets;

import ijfx.ui.main.ImageJFX;
import ijfx.ui.utils.ImageDisplayObserver;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;
import mongis.utils.SmartNumberStringConverter;
import net.imagej.display.ImageDisplay;
import net.imglib2.display.ColorTable;
import org.controlsfx.control.RangeSlider;
import org.reactfx.EventStreams;

/**
 *
 * @author cyril
 */
public class CurrentChannelAdjuster extends BorderPane {

    @FXML
    private TextField minValueTextField;

    @FXML
    private TextField maxValueTextField;

    @FXML
    private ComboBox<ColorTable> comboBox;

    private RangeSlider rangeSlider;

    private Property<ImageDisplay> imageDisplayProperty = new SimpleObjectProperty<>();

    private ImageDisplayObserver imageDisplayObserver;

    private BooleanProperty inUseProperty = new SimpleBooleanProperty(false);

    public CurrentChannelAdjuster() {

        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }

        imageDisplayProperty.addListener(this::onImageDisplayChanged);

    }

    public Property<ImageDisplay> imageDisplayProperty() {
        return imageDisplayProperty;
    }

    private void onImageDisplayChanged(ObservableValue obs, ImageDisplay oldValue, ImageDisplay imageDisplay) {
        if (imageDisplayObserver == null && imageDisplay != null) {
            imageDisplayObserver = new ImageDisplayObserver(imageDisplay.getContext());
            // imageDisplayObserver.currentLUTProperty().addListener(this::onLUTChanged);
            rangeSlider = new RangeSlider();
            setTop(rangeSlider);
            init();
        }
    }

    private void init() {

        rangeSlider.minProperty().bind(imageDisplayObserver.currentDatasetMinimumValue());
        rangeSlider.maxProperty().bind(imageDisplayObserver.currentDatasetMaximumValue());
        rangeSlider.lowValueProperty().bindBidirectional(imageDisplayObserver.currentLUTMinProperty());
        rangeSlider.highValueProperty().bindBidirectional(imageDisplayObserver.currentLUTMaxProperty());

        // comboBox.setPromptText("Change LUT");
        comboBox.setCellFactory(list -> new ColorTableCell());
        comboBox.setButtonCell(new ColorTableCell());

        //minValueTextField.textProperty().bind(rangeSlider.lowValueProperty().asString());
        //maxValueTextField.textProperty().bind(rangeSlider.highValueProperty().asString());

        comboBox.setItems(imageDisplayObserver.availableColorTableProperty());
        //comboBox.setValue(imageDisplayObserver.getCurrentLUT());

        comboBox.valueProperty().bindBidirectional(imageDisplayObserver.currentLUTProperty());
        // choiceBox.setButtonCell(new StaticLabelCell<>("Change LUT"));

        EventStreams.valuesOf(comboBox.showingProperty()).feedTo(inUseProperty);
        EventStreams.valuesOf(hoverProperty()).feedTo(inUseProperty);

        // Initializing the text field bindings
        SmartNumberStringConverter smartNumberStringConverter = new SmartNumberStringConverter();

        Bindings.bindBidirectional(minValueTextField.textProperty(), rangeSlider.lowValueProperty(), smartNumberStringConverter);
        Bindings.bindBidirectional(maxValueTextField.textProperty(), rangeSlider.highValueProperty(), smartNumberStringConverter);

    }

    private List<LUTView> generateLUTViews() {

        return imageDisplayObserver.availableColorTableProperty()
                .stream()
                .map(LUTView::new)
                .collect(Collectors.toList());

    }

    private class StaticLabelCell<T> extends ListCell<T> {

        final String text;

        final Label label;

        public StaticLabelCell(String text) {
            this.text = text;
            label = new Label(text);
            setGraphic(label);

            itemProperty().addListener(this::onItemChanged);

            setContentDisplay(ContentDisplay.TEXT_ONLY);

        }

        private void onItemChanged(Observable obs, T oldValue, T newValue) {
            setGraphic(label);
        }

    }

    private int getLUTWidth() {
        return new Double(comboBox.getWidth() - 30).intValue();
    }

    private class ColorTableCell extends ListCell<ColorTable> {

        public ColorTableCell() {

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            itemProperty().addListener(this::onItemChanged);

        }

        private void onItemChanged(Observable obs, ColorTable oldValue, ColorTable newValue) {
            if (newValue == null) {
                setGraphic(null);
                return;
            }
            setGraphic(new ImageView(FXUtilities.colorTableToImage(newValue, getLUTWidth(), 24)));
        }

    }

    private class LUTView {

        private final ColorTable colorTable;

        public LUTView(ColorTable colorTable) {
            this.colorTable = colorTable;
        }

        public ColorTable getColorTable() {
            return colorTable;
        }

        @Override
        public boolean equals(Object table) {

            if (table instanceof ColorTable == false) {
                return false;
            }

            ColorTable table2 = (ColorTable) table;

            return compare(getColorTable(), table2);

        }

        private boolean compare(ColorTable table1, ColorTable table2) {

            if (table1.getLength() != table2.getLength()) {
                return false;
            }

            for (int i = 0; i != table1.getLength(); i++) {
                for (int c = 0; c != 3; c++) {

                    if (table1.get(c, i) != table2.get(c, i)) {
                        return false;
                    }

                }
            }
            return true;

        }

    }

    public ReadOnlyBooleanProperty inUseProperty() {
        return inUseProperty;
    }

}
