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
package ijfx.ui.arcmenu;

import ijfx.ui.arcmenu.skin.ArcItemSkin;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import static ijfx.ui.arcmenu.skin.ArcItemSkin.CSS_ARC_MENU;
import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import javafx.animation.TranslateTransition;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * The ArcItem is represent one of possibilities in a ArcMenu.
 *
 * Each ArcItem can take three types : - Click : something happens on click -
 * Slider : the item becomes a slider for a range of values - Choices : the item
 * can slide to different values
 *
 * The default skin for an ArcItem is the ArcItemCircleSkin.
 *
 * @author Cyril MONGIS
 *
 * @param <T>
 */
public class ArcItem<T> extends Control {

    /**
     * Title of the arc item
     */
    protected final SimpleStringProperty title = new SimpleStringProperty();

    /**
     * Description
     */
    protected final SimpleStringProperty description = new SimpleStringProperty();

    /**
     * Icon (usually a FontAwesomeIcon)
     */
    protected final Property<Node> icon = new SimpleObjectProperty<Node>();

    /**
     * Type (Click,Slider or Choice)
     */
    protected ArcItemType type;

    /**
     *
     */
    protected PolarCoord polarCoordinates;

    /**
     * Slider minimum value
     */
    protected final SimpleDoubleProperty sliderMinValue = new SimpleDoubleProperty(10d);

    /**
     * Slider Maximum value
     */
    protected final SimpleDoubleProperty sliderMaxValue = new SimpleDoubleProperty(20d);

    /**
     * Slider Ratio : forgot what it does
     */
    protected final SimpleDoubleProperty sliderRatio = new SimpleDoubleProperty(0.0);

    /**
     * Value of the slider
     */
    protected final SimpleDoubleProperty sliderValue = new SimpleDoubleProperty(12.5);

    /**
     * Space between each discrete value that the slider can take
     */
    protected final SimpleDoubleProperty sliderTick = new SimpleDoubleProperty(2.5);

    /**
     * Boolean that indicates if it's sliding
     */
    protected final SimpleBooleanProperty isSliding = new SimpleBooleanProperty();

    /**
     * Forgot what it does
     */
    protected final SimpleStringProperty valueTextProperty = new SimpleStringProperty();

    /**
     * Choices when the item is in Choice mode
     */
    ArrayList<T> choices;

    PopArcMenu arcMenu;

    /**
     *
     */
    public static final double DEFAULT_SLIDER_WIDTH = 200;

    public DoubleProperty sliderWidth = new SimpleDoubleProperty(DEFAULT_SLIDER_WIDTH);

    T choice;

    Label selectionLabel = new Label();

    Logger logger = ImageJFX.getLogger();

    /**
     *
     * @return
     */
    public SimpleDoubleProperty sliderTickProperty() {
        return sliderTick;
    }

    /**
     *
     * @param title
     * @param icon
     * @param min
     * @param max
     * @param def
     */
    public ArcItem(String title, FontAwesomeIcon icon, double min, double max, double def) {
        this(title, icon);
        setType(ArcItemType.SLIDE);
    }

    /**
     *
     * @param title
     * @param icon
     * @param choices
     */
    public ArcItem(String title, FontAwesomeIcon icon, T[] choices) {
        this(title, icon, 0, choices.length - 1, 0);
        setType(ArcItemType.CHOICE);
        setChoices(choices);

    }

    /**
     *
     * @param choices
     */
    public void setChoices(T... choices) {
        this.choices = new ArrayList<T>();
        for (T choice : choices) {
            this.choices.add(choice);
        }
        sliderMinValue.set(0.0);
        sliderMaxValue.set(choices.length - 1);
        sliderTick.set(1.0);
    }

    /**
     *
     * @param title
     * @param icon
     */
    public ArcItem(String title, FontAwesomeIcon icon) {

        setTitle(title);
        //System.out.println(GlyphsDude.createIcon(icon));
        setIcon(GlyphsDude.createIcon(icon));
        setType(ArcItemType.CLICK);
        getSelectionLabel().getStyleClass().add(CSS_ARC_MENU);
        subscribeEvents();

    }

    /**
     *
     * @return
     */
    public ArrayList<T> getChoices() {
        return choices;
    }

    /**
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title.set(title);
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return title.getValue();
    }

    /**
     *
     * @return
     */
    public Node getIcon() {
        return icon.getValue();
    }

    /**
     *
     * @param icon
     */
    public void setIcon(Node icon) {
        this.icon.setValue(icon);
    }

    /**
     *
     * @param icon
     */
    public void setIcon(FontAwesomeIcon icon) {
        setIcon(GlyphsDude.createIcon(icon));
    }

    /**
     *
     * @return
     */
    public SimpleStringProperty titleProperty() {
        return title;
    }

    /**
     *
     * @return
     */
    public Property<Node> iconProperty() {
        return icon;
    }

    /**
     *
     * @param tick
     */
    public void setSliderTick(double tick) {
        sliderTickProperty().set(tick);
    }

    /**
     *
     * @return
     */
    public ArcItemType getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(ArcItemType type) {
        this.type = type;
    }

    /**
     *
     * @return
     */
    public T getChoice() {
        return choice;
    }

    /**
     *
     * @param choice
     */
    public void setChoice(T choice) {
        this.choice = choice;
    }

    /**
     *
     * @param value
     */
    public void setValue(double value) {
        this.sliderValue.set(value);
        sliderRatio.set(valueToSliderValue(value));
    }

    /**
     *
     * @param value
     * @return
     */
    public double valueToSliderValue(double value) {
        return (value - sliderMinValue.get()) / getValueRange();
    }

    /**
     *
     * @param value
     * @return
     */
    public double sliderValueToValue(double value) {
        return sliderMinValue.get() + (getValueRange() * value);
    }

    /**
     *
     * @param value
     */
    public void setSliderMinValue(double value) {
        sliderMinValue.setValue(value);
    }

    /**
     *
     * @param value
     */
    public void setSliderMaxValue(double value) {
        sliderMaxValue.setValue(value);
    }

    /**
     *
     * @return
     */
    public PolarCoord getPolarCoordinates() {
        return polarCoordinates;
    }

    /**
     *
     * @param polarCoordinates
     */
    public void setPolarCoordinates(PolarCoord polarCoordinates) {
        this.polarCoordinates = polarCoordinates;

    }

    /**
     *
     * @return
     */
    public DoubleProperty slideProperty() {
        return sliderRatio;
    }

    /**
     *
     * @return
     */
    public PopArcMenu getArcMenu() {
        return arcMenu;
    }

    /**
     *
     * @param arcRenderer
     */
    public void setArcRenderer(PopArcMenu arcRenderer) {
        this.arcMenu = arcRenderer;
    }

    /**
     *
     */
    
    boolean isSubscribed = false;
    
    public void subscribeEvents() {
        if(isSubscribed) return;
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);

        slideProperty().addListener((obj, oldValue, newValue) -> {

            sliderValue.set(getCloserChoice(sliderValueToValue(newValue.doubleValue())));

        });

        /*
        sliderValue.addListener((event, oldValue, newValue) -> {
            updateTicks(newValue.doubleValue());
            if (getType() == ArcItemType.SLIDE) {
                selectionLabel.setText(newValue.toString());
            } else if (getType() == ArcItemType.CHOICE) {
                selectionLabel.setText(choices.get(newValue.intValue()).toString());
            }
        });*/
        sliderValue.addListener(this::onSliderValueChanged);

        // makes sure that the tick hash map is always up to date
        sliderMinValue.addListener(this::onTickChanged);
        sliderMaxValue.addListener(this::onTickChanged);
        sliderTick.addListener(this::onTickChanged);

        //translateXProperty().addListener((event, oldValue, newValue) -> placeLabel(newValue.doubleValue()));
        //translateYProperty().addListener((event, oldValue, newValue) -> placeLabel(newValue.doubleValue()));
        selectionLabel.translateXProperty().bind(translateXProperty());
        selectionLabel.translateYProperty().bind(Bindings.createDoubleBinding(this::getLabelY, translateXProperty(), translateYProperty()));
        isSubscribed = true;
// rebuildTickHashMap();
    }

    private void onSliderValueChanged(Observable value, Number oldValue, Number newValue) {
        updateTicks(newValue.doubleValue());
        if (getType() == ArcItemType.SLIDE) {
            selectionLabel.setText(newValue.toString());
        } else if (getType() == ArcItemType.CHOICE) {
            selectionLabel.setText(choices.get(newValue.intValue()).toString());
        }
    }

    /**
     *
     * @param value
     */
    private double getLabelY() {
        // if(isSliding.get()) {
        if (getPolarCoordinates() == null) {
            return 0.0;
        }
        return getPolarCoordinates().getLocationCloserToCenter(-getHeight()).getY() + ImageJFX.MARGIN;
        //}
        //else {

        //}
    }

    /*
    public void placeLabel(double value) {
        if (isSliding.get()) {
            
            selectionLabel.setTranslateX(getTranslateX());
            selectionLabel.setTranslateY(getTranslateY() + getHeight() + ImageJFX.MARGIN);
        } else {
            //selectionLabel.setTranslateX(getPolarCoordinates().getLocationCloserToCenter(-10).getX());
            selectionLabel.setTranslateY(getPolarCoordinates().getLocationCloserToCenter(-10).getY());
            
            selectionLabel.setTranslateY(getTranslateY() + getHeight() + ImageJFX.MARGIN);
        }
    }*/
    /**
     *
     * @return
     */
    public Label getSelectionLabel() {
        return selectionLabel;
    }

    /**
     *
     * @param selectionLabel
     */
    public void setSelectionLabel(Label selectionLabel) {
        this.selectionLabel = selectionLabel;
    }

    /**
     *
     */
    public void unsubscribeEvents() {
        
        removeEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        removeEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
        isSubscribed = false;
    }

    MouseEvent lastEvent = null;
    MouseEvent firstEvent = null;

    // event used for sliding
    private void onMouseDragged(MouseEvent event) {
        
        
       
        logger.info("event : "+event);
        logger.info("firstEvent : "+firstEvent);
        logger.info("lastEvent : "+lastEvent);
        
        if (getType() == ArcItemType.CLICK) {
            return;
        }
        if (firstEvent == null) {
            firstEvent = event;
            lastEvent = event;
            originalValue = sliderValue.get();

            isSliding.set(true);

        }
        
        double shift = event.getScreenX() - firstEvent.getScreenX();

        if (lastEvent != null) {
            double diff = event.getScreenX() - lastEvent.getScreenX();

            double newSliderValue = sliderRatio.get() + diff / sliderWidth.getValue();
            
            /*
            if (newSliderValue < 0.0 || newSliderValue > 1.0) {
                event.consume();
                return;
            }*/

            // setTranslateX(getTranslateX() + diff);
            setTranslateX(getPolarCoordinates().xProperty().getValue() + shift);
            sliderRatio.set(newSliderValue);

        }
        lastEvent = event;
        displayBar();
        //event.consume();
//
    }
    //event used for button click

    private void onMouseReleased(MouseEvent event) {

        TranslateTransition backToOrigin = new TranslateTransition(Duration.millis(200), this);
        backToOrigin.setFromX(getTranslateX());
        backToOrigin.setToX(getPolarCoordinates().xProperty().doubleValue());
        backToOrigin.play();

        /*
        backToOrigin = new TranslateTransition(Duration.millis(200), selectionLabel);
        backToOrigin.setFromX(getTranslateX());
        backToOrigin.setToX(getPolarCoordinates().xProperty().doubleValue());
        backToOrigin.play();*/
        lastEvent = null;
        firstEvent = null;
        hideBar();
        sliderRatio.set(valueToSliderValue(sliderValue.get()));
        isSliding.set(false);
        //event.consume();
    }

    ;

    private void onTickChanged(ObservableValue<? extends Number> obs, Number oldValue, Number newValue) {
        rebuildTickHashMap();
    }
    ;

    Group bar;

    /**
     *
     */
    protected double originalValue;

    /**
     * Displays the bar so the current value is centered on the slide departure
     */
    public void displayBar() {

        if (getArcMenu().getChildren().contains(bar)) {
            return;
        }

        bar = new Group();

        Rectangle rect = new Rectangle(sliderWidth.getValue(), 10);
        if (getType() == ArcItemType.SLIDE) {
            rect.setFill(Color.WHITE);
        } else {
            rect.setFill(null);
        }

        bar.getChildren().addAll(rect);

        if (getType() == ArcItemType.CHOICE) {
            getTickHashMap().forEach((value, node) -> {
                node.setTranslateX(valueToSliderValue(value) * sliderWidth.getValue());
                bar.getChildren().add(node);
            });
        }

       bar.setTranslateX(getTranslateValue());
       bar.setTranslateY(getPolarCoordinates().yProperty().doubleValue());
       //bar.translateXProperty().bind(Bindings.createDoubleBinding(this::getTranslateValue, translateYProperty()));
       //bar.translateYProperty().bind(Bindings.createDoubleBinding(getPolarCoordinates().yProperty()::doubleValue,translateYProperty()));
       //adding the bar
        getArcMenu().getChildren().add(0, bar);

    }

    /**
     *
     * @return
     */
    public HashMap<Double, Node> getTickHashMap() {
        if (tickHashMap == null) {
            rebuildTickHashMap();
        }
        return tickHashMap;
    }

    HashMap<Double, Node> tickHashMap;

    /**
     *
     */
    public void rebuildTickHashMap() {

        if (getSkin() == null) {
            return;
        }
        tickHashMap = new HashMap<>();

        for (double i = sliderMinValue.get(); i <= sliderMaxValue.get(); i += sliderTick.get()) {

            tickHashMap.put(new Double(i), createTick(i));

        }
    }

    /**
     *
     * @param newValue
     */
    protected void updateTicks(double newValue) {
        if (tickHashMap == null) {
            rebuildTickHashMap();
        }

        tickHashMap.forEach((key, node) -> {

            if (key.equals(newValue)) {
                node.getStyleClass().add(ArcItemSkin.CSS_ARC_ITEM_CHOICE_BOX_HOVER);
            } else {
                node.getStyleClass().remove(ArcItemSkin.CSS_ARC_ITEM_CHOICE_BOX_HOVER);
            }
        });

    }

    /**
     *
     * @param value
     * @return
     */
    public Node createTick(final double value) {
        ArcItemSkin skin = (ArcItemSkin) getSkin();

        try {
            Node tick = skin.createChoiceBox();

            return tick;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *
     * @param origin
     * @param toPlace
     * @param xdiff
     * @param ydiff
     */
    private void centerDependingOn(Node origin, Node toPlace, double xdiff, double ydiff) {

        double x = origin.getTranslateX();

        toPlace.setTranslateX(x);
        toPlace.setTranslateY(origin.getTranslateY());
    }

    /**
     *
     */
    public void hideBar() {
        getArcMenu().getChildren().remove(bar);
    }

    /**
     *
     * @return
     */
    public double getValueRange() {
        return sliderMaxValue.get() - sliderMinValue.get();
    }

    // the step is actual number of ticks required to arrive to the current sliderValue
    private int getStep(double value) {
        double begin = sliderMinValue.getValue();
        double end = sliderMaxValue.getValue();
        double t = sliderTick.getValue();

        return (int) Math.round((value - begin) / t);
    }

    private double getTranslateValue() {

        final double sliderW = sliderWidth.getValue();

        double x = polarCoordinates.xProperty().getValue() + (sliderW / 2) - (sliderRatio.get() * sliderW);

        return x;
    }

    // the closer choice is the sliderTick sliderValue from the brut sliderValue
    private double getCloserChoice(double value) {
        double begin = sliderMinValue.getValue();
        double end = sliderMaxValue.getValue();
        double t = sliderTick.getValue();

        int step = getStep(value);

        return begin + step * t;
    }

}
