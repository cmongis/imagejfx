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
package ijfx.ui.datadisplay.image;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.plugins.commands.Isolate;
import ijfx.service.ui.ControlableProperty;
import ijfx.service.ui.ReadOnlySuppliedProperty;
import ijfx.service.usage.Usage;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mongis.utils.BindingsUtils;
import net.imagej.axis.Axes;
import net.imagej.display.DatasetView;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.display.event.LUTsChangedEvent;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.mongis.usage.UsageLocation;
import org.scijava.command.CommandService;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;

/**
 * Button displaying the brightest color of a color table in a rectangle
 *
 * @author cyril
 */
public class TableColorButton extends Button {

    private Property<DatasetView> datasetViewProperty = new SimpleObjectProperty();

    private IntegerProperty channelProperty = new SimpleIntegerProperty();

    private static final double RECTANGLE_SIZE_WHEN_CURRENT_CHANNEL = 16;
    private static final double RECTANGLE_SIZE = 20;

    private Rectangle rectangle = new Rectangle(RECTANGLE_SIZE, RECTANGLE_SIZE);

    private ControlableProperty<DatasetView, Boolean> channelActivatedProperty;

    private ContextMenu contextMenu = new ContextMenu();

    private static final UsageLocation TABLE_COLOR_BUTTON = UsageLocation.get("Channel Rectangle");

    private ReadOnlySuppliedProperty<Boolean> isCurrentChannelProperty = new ReadOnlySuppliedProperty<>(this::isCurrentChannel)
            .bindTo(datasetViewProperty);

    private DoubleBinding rectangleSize = Bindings.createDoubleBinding(this::getRectangleSize, isCurrentChannelProperty);

    @Parameter
    CommandService commandService;

    public TableColorButton() {
        super();

        rectangle.getStyleClass().add("rectangle");
        getStyleClass().add("color-button");
        setGraphic(rectangle);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        Usage.listenButton(this, TABLE_COLOR_BUTTON, "Channel switch");

        channelProperty.addListener(this::onParameterChanged);
        datasetViewProperty.addListener(this::onParameterChanged);

        channelActivatedProperty = new ControlableProperty<DatasetView, Boolean>()
                .bindBeanTo(datasetViewProperty)
                .setCaller(this::isActivated)
                .setBiSetter(this::setActivated);

        setOnAction(this::onClick);

        channelActivatedProperty.setValue(Boolean.TRUE);
        BindingsUtils.bindNodeToPseudoClass(PseudoClass.getPseudoClass("current-channel"), this, isCurrentChannelProperty);
        BindingsUtils.bindNodeToPseudoClass(PseudoClass.getPseudoClass("selected"), this, channelActivatedProperty);
        setContextMenu(contextMenu);
        addAction("Edit this channel", FontAwesomeIcon.COG, this::editThisChannel);
        addAction("Only display this channel", FontAwesomeIcon.EYE, this::displayOnlyThis);
        addAction("Isolate this channel", FontAwesomeIcon.COPY, this::isolateChannel);

        setTooltip(new Tooltip("The first click select the channel, the second click activate or deactivate it."));

    }

    private void onClick(ActionEvent event) {

        if (isActivated(getCurrentView()) && !isCurrentChannel()) {

            toggle();
        } else {
            setAsCurrentChannel();
            toggle();
        }
        updateView(getCurrentView());

    }

    private void toggle() {
        channelActivatedProperty.setValue(!channelActivatedProperty.getValue());
    }

    public Property<DatasetView> datasetViewProperty() {
        return datasetViewProperty;
    }

    public IntegerProperty channelProperty() {
        return channelProperty;
    }

    private Color getCurrentBrightestColor() {

        DatasetView view = datasetViewProperty().getValue();
        int channelId = channelProperty.get();
        if (channelId == -1 || view == null) {
            return Color.BLACK;
        }

        if (channelId >= view.getColorTables().size()) {
            return Color.BLACK;
        }

        return getBrighterColor(view.getColorTables().get(channelId));
    }

    private void onParameterChanged(Observable obs, Object o1, Object o2) {

        Platform.runLater(this::updateColor);
    }

    private Boolean isSelected() {
        return isActivated(getCurrentView());
    }

    private void updateColor() {
        if (!isSelected()) {
            rectangle.setFill(Color.BLACK);
        } else {
            rectangle.setFill(getCurrentBrightestColor());
        }

    }

    public Boolean isActivated(DatasetView view) {
        if (view == null) {
            return false;
        }
        return view.getProjector().isComposite(channelProperty().get());

    }

    public void setAsCurrentChannel() {
        DatasetView view = getCurrentView();
        if (view != null) {
            view.setPosition(getChannelId(), Axes.CHANNEL);
            view.getProjector().map();;
            view.update();
        }
    }

    private Boolean isCurrentChannel() {
        if (getCurrentView() == null) {
            return false;
        }
        return getCurrentView().getLongPosition(Axes.CHANNEL) == (long) getChannelId();
    }

    private int getChannelId() {
        return channelProperty().get();
    }

    public DatasetView getCurrentView() {
        return datasetViewProperty.getValue();
    }

    public void setActivated(DatasetView view, Boolean activated) {

        if (view == null) {
            return;
        }
        long position = view.getLongPosition(Axes.CHANNEL);
        long channelId = getChannelId();
        /*
        if (!activated && channelId != position) {
            view.setPosition(getChannelId(), Axes.CHANNEL);
            activated = true;
        } else if (activated) {
            view.setPosition(channelProperty().get(), Axes.CHANNEL);
        }*/
        view.getProjector().setComposite(getChannelId(), activated);

    }

    private void updateView(DatasetView view) {
        view.getProjector().map();
        view.update();
    }

    private Color getBrighterColor(ColorTable colorTable) {

        if (colorTable instanceof ColorTable8) {
            return getBrighterColor((ColorTable8) colorTable);
        } else {
            return Color.BLACK;
        }

    }

    private Color getBrighterColor(ColorTable8 colorTable) {

        double red = colorTable.get(ColorTable.RED, 255);
        double green = colorTable.get(ColorTable.GREEN, 255);
        double blue = colorTable.get(ColorTable.BLUE, 255);

        return new Color(red / 255, green / 255, blue / 255, 1.0);

    }

    @EventHandler
    private void onDatasetViewUpdated(DataViewUpdatedEvent event) {

        channelActivatedProperty.checkFromGetter();

        if (event.getView() == datasetViewProperty.getValue()) {
            updateColor();
            isCurrentChannelProperty.updateFromGetter();
        }
    }

    @EventHandler
    public void onLutChangedEvent(LUTsChangedEvent event) {
        if (datasetViewProperty.getValue() == event.getView()) {
            updateColor();
        }
    }

    public void addAction(String text, FontAwesomeIcon icon, Runnable action) {

        MenuItem item = new MenuItem(text, GlyphsDude.createIcon(icon));
        item.setOnAction(event -> action.run());
        contextMenu.getItems().add(item);
    }

    private void displayOnlyThis() {

        DatasetView view = datasetViewProperty.getValue();
        int channel = channelProperty().get();

        for (int i = 0; i != view.getChannelCount(); i++) {

            view.getProjector().setComposite(i, i == channel);
        }
        view.setPosition(channel, Axes.CHANNEL);
        view.getProjector().map();
        view.update();

    }

    private void isolateChannel() {

        if (commandService == null) {
            datasetViewProperty().getValue().context().inject(this);
        }

        commandService.run(Isolate.class, true, "axisType", Axes.CHANNEL, "position", channelProperty().getValue());
    }

    private void editThisChannel() {
        datasetViewProperty().getValue().setPosition(channelProperty().intValue(), Axes.CHANNEL);
        datasetViewProperty().getValue().update();
    }

    private Double getRectangleSize() {
        return isCurrentChannel() && isSelected() ? RECTANGLE_SIZE_WHEN_CURRENT_CHANNEL : RECTANGLE_SIZE;
    }

}
