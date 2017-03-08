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

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.service.usage.Usage;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.utils.ImageDisplayObserver;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import mongis.utils.FXUtilities;
import mongis.utils.SmartNumberStringConverter;
import mongis.utils.transition.TransitionBinding;
import net.imagej.display.ImageDisplay;
import net.imglib2.display.ColorTable;
import net.mongis.usage.UsageLocation;
import org.controlsfx.control.RangeSlider;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;

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

    @FXML
    private ToolBar toolbar;

    @FXML
    private Label descriptionLabel;

    @FXML
    private MenuButton moreMenuButton;

    private RangeSlider rangeSlider;

    private Property<ImageDisplay> imageDisplayProperty = new SimpleObjectProperty<>();

    private ImageDisplayObserver imageDisplayObserver;

    private BooleanProperty inUseProperty = new SimpleBooleanProperty(false);

    private final static UsageLocation CHANNEL_ADJUSTER = UsageLocation.get("Channel Adjuster");
    
    @Parameter
    CommandService commandService;

    private HoverDescriptionBinding descriptionBinding;

    public CurrentChannelAdjuster() {

        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }

        imageDisplayProperty.addListener(this::onImageDisplayChanged);

        descriptionBinding = new HoverDescriptionBinding(descriptionLabel.textProperty());

        new TransitionBinding<Number>(0, 25)
                .bind(descriptionBinding.isActive(), descriptionLabel.prefHeightProperty())
                .setDuration(Duration.millis(200));

    }

    public Property<ImageDisplay> imageDisplayProperty() {
        return imageDisplayProperty;
    }

    private void onImageDisplayChanged(ObservableValue obs, ImageDisplay oldValue, ImageDisplay imageDisplay) {
        if (imageDisplayObserver == null && imageDisplay != null) {
            imageDisplayObserver = new ImageDisplayObserver(imageDisplay.getContext());
            imageDisplay.getContext().inject(this);
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

        
        Usage.listenSwitch(rangeSlider.lowValueChangingProperty(),"Chaneing Low value",CHANNEL_ADJUSTER);
        Usage.listenSwitch(rangeSlider.highValueChangingProperty(),"Changing high value",CHANNEL_ADJUSTER);
        
        comboBox.setCellFactory(list -> new ColorTableCell());
        comboBox.setButtonCell(new ColorTableCell());

        comboBox.setItems(imageDisplayObserver.availableColorTableProperty());

        comboBox.valueProperty().bindBidirectional(imageDisplayObserver.currentLUTProperty());

        Usage.listenClick(this, CHANNEL_ADJUSTER,"LUT ComboBox");
        
        // making the list of all properties that should account for saying that the 
        // menu is in use
        final ReadOnlyBooleanProperty[] properties = {
            comboBox.showingProperty(),
            hoverProperty(),
            moreMenuButton.showingProperty()

        };
        // creating a binding that depends of all these properties and 
        // and bind it to the property
        inUseProperty.bind(Bindings.createBooleanBinding(() -> {
            return Stream
                    .of(properties)
                    .map(ReadOnlyBooleanProperty::getValue)
                    .filter(v -> v)
                    .count() > 0;
        }, properties));

        Usage.listenProperty(inUseProperty, "Use adjuster", CHANNEL_ADJUSTER);
        
        
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

    public CurrentChannelAdjuster addButton(String name, FontAwesomeIcon icon, String description, EventHandler<ActionEvent> runnable) {

        Button item = new Button(name, GlyphsDude.createIcon(icon));

        descriptionBinding.bind(item, description);

        item.setOnAction(runnable);

        toolbar.getItems().add(item);

        Usage.listenButton(item, CHANNEL_ADJUSTER, name);
        
        
        
        return this;
    }

    public CurrentChannelAdjuster addButton(String name, FontAwesomeIcon icon, String description, Class<? extends Command> module, Object... parameters) {

        return addButton(name, icon, description, event -> {
            commandService.run(module, true, parameters);
        });
    }

    public CurrentChannelAdjuster addAction(String name, FontAwesomeIcon icon, String description, Class<? extends Command> module, Object... parameters) {

        // initializing the menu item
        final MenuItem item = new MenuItem(name, GlyphsDude.createIcon(icon));

        //final HoverListener hoverListener = new HoverListener(item);
        
        // binding the description of the item to the description displayer
        //descriptionBinding.bind(hoverListener, description);
        item.setOnAction(event -> {
            commandService.run(module, true, parameters);
        });
        
        // adidng usage gathering
        Usage.listenClick(item, CHANNEL_ADJUSTER);
       
        
        moreMenuButton.getItems().add(item);
        
        return this;
    }

    // Helper class that indicate when the mouse hovers an menu item
    private class HoverListener extends SimpleBooleanProperty {
        public HoverListener(MenuItem item) {
            super(false);
            //item.addEventHandler(MouseEvent., this::onMouseEntered);
            //item.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, this::onMouseExited);
        }

        private void onMouseEntered(MouseEvent event) {
            setValue(true);
        }

        private void onMouseExited(MouseEvent event) {
            setValue(false);
        }

    }

    private class HoverDescriptionBinding {

        final private StringProperty target;

        final private BooleanProperty isActive = new SimpleBooleanProperty();

        public HoverDescriptionBinding(StringProperty target) {
            this.target = target;
        }

        public HoverDescriptionBinding bind(Node node, String text) {

            return bind(node.hoverProperty(), text);
        }

        public HoverDescriptionBinding bind(ReadOnlyBooleanProperty booleanProperty, String text) {

            booleanProperty.addListener(
                    (obs, oldValue, newValue) -> {
                        if (newValue) {
                            target.setValue(text);
                            isActive.setValue(true);
                        } else {
                            target.setValue(null);
                            isActive.setValue(false);
                        }
                    });
            return this;
        }

        public ReadOnlyBooleanProperty isActive() {
            return isActive;
        }
    }

}
