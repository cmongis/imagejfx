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
package ijfx.ui.service;

import ijfx.service.IjfxService;
import ijfx.ui.main.ImageJFX;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Callback;
import net.imagej.display.ColorMode;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.display.event.LUTsChangedEvent;
import org.scijava.Priority;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayActivatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class, priority = Priority.VERY_LOW_PRIORITY)
public class ImageDisplayFXService extends AbstractService implements IjfxService {

    private ControlableProperty<ImageDisplay, DatasetView> currentDatasetView;

    private ControlableProperty<DatasetView, ColorMode> currentColorMode;

    private ControlableProperty<ImageDisplayService, ImageDisplay> currentImageDisplay;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    DisplayService displayService;

    @Override
    public void initialize() {

        currentImageDisplay = new ControlableProperty<ImageDisplayService, ImageDisplay>()
                .setBean(imageDisplayService)
                .setSetter(displayService::setActiveDisplay)
                .setGetter(imageDisplayService::getActiveImageDisplay);

        currentDatasetView = new ControlableProperty<ImageDisplay, DatasetView>()
                .bindBeanTo(currentImageDisplay)
                .setGetter(imageDisplayService::getActiveDatasetView);

        currentColorMode = new ControlableProperty<DatasetView, ColorMode>()
                .bindBeanTo(currentDatasetView)
                .setCaller(DatasetView::getColorMode)
                .setBiSetter(this::setColorMode);

    }

    public Property<ColorMode> currentColorModeProperty() {
        return currentColorMode;
    }

    public void setColorMode(DatasetView view, ColorMode mode) {
        view.setColorMode(mode);
        
        ImageJFX.getThreadPool().submit(view::update);
        
    }
    
    

    @EventHandler
    public void onImageDisplayChanged(DisplayActivatedEvent event) {

        if (event.getDisplay() instanceof ImageDisplay) {
            currentImageDisplay.setSilently((ImageDisplay) event.getDisplay());
        }

    }

    @EventHandler
    public void onDatasetViewUpdated(DataViewUpdatedEvent event) {
        if (currentDatasetView.getValue() == event.getView()) {
            currentColorMode.getValue();
        }
    }

    @EventHandler
    public void onLutChangedEvent(LUTsChangedEvent event) {
        if (currentDatasetView.getValue() == event.getView()) {
            currentColorMode.getValue();
        }
    }

    private class ControlableProperty<R, T> extends ObjectPropertyBase<T> {

        private Getter<T> getter;

        private Setter<T> setter;

        private BiConsumer<R, T> doubleSetter;

        private Callback<R, T> doubleGetter;

        private final Property<R> beanProperty = new SimpleObjectProperty<R>();

        public ControlableProperty() {

            beanProperty().addListener(this::onBeanChanged);

        }

        public ControlableProperty<R, T> setBean(R bean) {
            beanProperty.setValue(bean);
            return this;
        }

        public Property<R> beanProperty() {
            return beanProperty;
        }

        public ControlableProperty<R, T> bindBeanTo(Property<R> property) {
            beanProperty.bind(property);
            return this;
        }

        @Override
        public void setValue(T t) {

            T oldValue = super.getValue();

            // avoid loop
            if (oldValue != t) {

                if (doubleSetter != null) {
                    doubleSetter.accept(beanProperty.getValue(), t);

                } else if (setter != null) {
                    setter.set(t);

                }

                super.setValue(t);

            }

            Platform.runLater(() -> this.getValue());

        }

        public void setSilently(T t) {
            super.setValue(t);
        }

        public ControlableProperty<R, T> setGetter(Getter<T> g) {
            this.getter = g;
            getValue();
            return this;
        }

        public ControlableProperty<R, T> setCaller(Callback<R, T> callback) {
            doubleGetter = callback;
            getValue();
            return this;
        }

        public ControlableProperty<R, T> setBiSetter(BiConsumer<R, T> biConsumer) {
            doubleSetter = biConsumer;

            return this;
        }

        public ControlableProperty<R, T> setSetter(Setter<T> s) {
            this.setter = s;
            return this;
        }

        public void onBeanChanged(Observable obs, R oldBean, R newBean) {
            Platform.runLater(this::getValue);
        }

        @Override
        public Object getBean() {
            return beanProperty.getValue();

        }

        public T getValue() {

            T t;

            if (doubleGetter != null && beanProperty.getValue() != null) {
                t = doubleGetter.call(beanProperty.getValue());

            } else if (getter != null) {
                t = getter.get();
            } else {
                t = super.getValue();
            }

            if (super.getValue() != t) {
                super.setValue(t);
            }
            return t;

        }

        @Override
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public void notifyChanged() {
            getValue();

        }

    }

    @FunctionalInterface
    private interface Getter<T> {

        public T get();
    }

    @FunctionalInterface
    private interface Setter<T> {

        public void set(T t);
    }

}
