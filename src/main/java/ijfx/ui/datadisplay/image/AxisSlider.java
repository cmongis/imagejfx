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

import com.google.common.collect.ImmutableMap;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.plugins.commands.DeleteDataFX;
import ijfx.plugins.commands.ExtractSlice;
import ijfx.plugins.commands.Isolate;
import ijfx.ui.main.ImageJFX;
import ijfx.service.ui.ControlableProperty;
import ijfx.service.usage.Usage;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.mongis.usage.UsageLocation;
import org.reactfx.EventStreams;
import org.scijava.command.CommandService;
import org.scijava.event.EventHandler;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import rx.subjects.PublishSubject;

/*
Helper Classes
 */
public class AxisSlider extends BorderPane {

    private final CalibratedAxis axis;
    private final ImageDisplay display;
    private final int axisId;
    private final ControlableProperty<ImageDisplay, Number> position;

    @FXML
    Slider slider;

    @FXML
    Label axisNameLabel;

    @FXML
    Label axisPositionLabel;

    @FXML
    MenuButton menuButton;

    @Parameter
    CommandService commandService;

    @Parameter
    ModuleService moduleService;

    @Parameter
    ImageDisplayService imageDisplayService;

    PublishSubject<Runnable> updateRequest = PublishSubject.create();

    ControlableProperty<ImageDisplay, Number> minProperty = new ControlableProperty()
            .setGetter(this::getMinSlider);

    ControlableProperty<ImageDisplay, Number> maxProperty = new ControlableProperty()
            .setGetter(this::getMaxSlider);

    public AxisSlider(ImageDisplay display, int id) {

        super();
        FXUtilities.injectFXMLUnsafe(this);

        display.getContext().inject(this);

        this.axis = display.axis(id);
        this.display = display;
        axisId = id;

        slider.minProperty().bind(minProperty);
        slider.maxProperty().bind(maxProperty);

        slider.setValue(display.getLongPosition(id));
        slider.setMajorTickUnit(1.0);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(1.0f);
        slider.setShowTickMarks(true);
        position = new ControlableProperty<ImageDisplay, Number>()
                .setGetter(this::getPosition)
                .setSetter(this::setPosition);

        slider.valueProperty().bindBidirectional(position);
        axisNameLabel.setText(axis.type().getLabel());
        axisPositionLabel.textProperty().bind(Bindings.createStringBinding(this::getAxisPosition, slider.valueProperty(), minProperty, maxProperty));

        addActions();

        Usage.listenProperty(slider.valueProperty(), "Current " + axis.type().getLabel(), UsageLocation.get("Axis Slider"), 2);

    }

    private Number getPosition() {
        try {
            return imageDisplayService.getActiveDatasetView(display).getLongPosition(axisId);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    private Long getMinSlider() {
        if (display == null || display.size() == 0) {
            return new Long(0);
        }
        return display.min(axisId);
    }

    private Long getMaxSlider() {
        if (display == null) {
            return new Long(1);
        }
        return display.max(axisId);
    }

    private int getActivatedChannelNumber() {
        DatasetView datasetView = imageDisplayService.getActiveDatasetView(display);

        int count = 0;
        for (int i = 0; i != datasetView.getChannelCount(); i++) {
            if (datasetView.getProjector().isComposite(i)) {
                count++;
            }

        }

        return count;

    }

    private void setPosition(Number position) {

        int p = position.intValue();

        DatasetView datasetView = imageDisplayService.getActiveDatasetView(display);

        long oldValue = getPosition().longValue();
        long newValue = position.longValue();

        if (oldValue == newValue) {
            return;
        }

        if (axis.type().getLabel().toLowerCase().contains("channel") && datasetView.getProjector().isComposite(position.intValue()) == false) {

            int activatedChannel = getActivatedChannelNumber();

            // if only one channel is activated, then we desactivate the other ones
            // (it allows the user to go through channels one by one)
            if (activatedChannel == 1) {
                datasetView.getProjector().setComposite((int) oldValue, false);
            }
            datasetView.getProjector().setComposite(p, true);

        }

        datasetView.setPosition(position.longValue(), axisId);
        display.setPosition(position.longValue(), axisId);
        ImageJFX.getThreadQueue().execute(new ViewUpdate(datasetView));
    }

    Boolean lock = Boolean.TRUE;
    
    private class ViewUpdate implements Runnable {

        final DatasetView view;

        public ViewUpdate(DatasetView view) {
            this.view = view;
        }

        public void run() {
            synchronized (lock) {
                view.getProjector().map();
                view.update();
            }
        }
    }
    
    private String getAxisName() {
        return axis.type().getLabel();
    }

    private String getAxisPosition() {
        return String.format("%d / %d", getPosition().longValue() + 1, display.dimension(axisId));
    }

    @EventHandler
    private void onDatasetViewChanged(DataViewUpdatedEvent event) {
        if (position == null) {
            return;
        }
        if (event.getView() == imageDisplayService.getActiveDatasetView(display) && display.size() > 0) {
            position.checkFromGetter();
            minProperty.checkFromGetter();
            maxProperty.checkFromGetter();
        }
    }

    private void addActions() {

        addAction(new LabelAction("Isolate this"), FontAwesomeIcon.FILES_ALT, this::isolateAxis);
        addAction(new LabelAction("Delete before this"), FontAwesomeIcon.STEP_BACKWARD, this::deleteBeforePosition);
        addAction(new LabelAction("Delete this"), FontAwesomeIcon.REMOVE, this::deleteThisPosition);
        addAction(new LabelAction("Delete after this"), FontAwesomeIcon.STEP_FORWARD, this::deleteAfterPosition);
        addAction(t -> "Duplicate image", FontAwesomeIcon.PICTURE_ALT, this::isolateCurrentPosition);

    }

    private void isolateAxis() {
        commandService.run(Isolate.class, true, "axisType", this.axis.type(), "position", position.getValue().intValue());
    }

    private void isolateCurrentPosition() {
        commandService.run(ExtractSlice.class, true);
    }

    private void deleteThisPosition() {
        Long position = display.getLongPosition(axisId);

        Map<String, Object> params = ImmutableMap.<String, Object>builder()
                .put("position", position)
                .put("quantity", 1)
                .put("axisType", axis.type())
                .build();

        commandService.run(DeleteDataFX.class, true, params);

    }

    private void deleteBeforePosition() {
        Long quantity = display.getLongPosition(axisId);
        Long position = new Long(1);

        Map<String, Object> params = ImmutableMap.<String, Object>builder()
                .put("position", position)
                .put("quantity", quantity)
                .put("axisType", axis.type())
                .build();

        commandService.run(DeleteDataFX.class, true, params);

    }

    private void deleteAfterPosition() {

        Long quantity = display.dimension(axisId) - display.getLongPosition(axisId);
        Long position = display.getLongPosition(axisId) + 1;
        Map<String, Object> params = ImmutableMap.<String, Object>builder()
                .put("axisType", axis.type())
                .put("position", position)
                .put("quantity", quantity)
                .build();

        commandService.run(DeleteDataFX.class, true, params);
    }

    private void addAction(Function<AxisType, String> labelFunction, FontAwesomeIcon icon, Runnable action) {
        MenuItem item = new MenuItem();
        item.textProperty().setValue(labelFunction.apply(axis.type()));
        item.setGraphic(GlyphsDude.createIcon(icon));
        item.setOnAction(event -> action.run());
        menuButton.getItems().add(item);
    }

    private class LabelAction implements Function<AxisType, String> {

        private final String action;

        public LabelAction(String action) {

            this.action = action;

        }

        @Override
        public String apply(AxisType t) {
            return new StringBuilder()
                    .append(action)
                    .append(" ")
                    .append(t.getLabel().contains("Z") ? "slice" : t.getLabel().toLowerCase())
                    .toString();
        }

    }

    public ReadOnlyBooleanProperty usedProperty() {
        return menuButton.showingProperty();
    }
}
