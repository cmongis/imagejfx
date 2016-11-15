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
package ijfx.ui.widgets;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class PopoverToggleButton extends ToggleButton {

    PopOver popover = new PopOver();

    public PopoverToggleButton() {
        super();

        addEventHandler(ActionEvent.ACTION, this::onClick);

        popover.showingProperty().addListener(this::onPopOverShowingChange);

    }

    public PopoverToggleButton(Node node, ArrowLocation arrowLocation) {
        this();
        setContent(node, arrowLocation);

    }

    public void setContent(Node node) {
        getPopover().setContentNode(node);
    }

    public void setContent(Node node, ArrowLocation anchorLocation) {
        setContent(node);
        getPopover().setArrowLocation(anchorLocation);
    }

    public PopOver getPopover() {
        return popover;
    }

    public PopoverToggleButton setButtonText(String text) {
        setText(text);
        return this;
    }

    public PopoverToggleButton setIcon(FontAwesomeIcon icon) {
        setGraphic(GlyphsDude.createIcon(icon));
        return this;
    }

    protected void onClick(ActionEvent event) {

        if (!popover.isShowing()) {
            popover.show(this);
        } else {
            popover.hide();
        }
    }

    protected void onPopOverShowingChange(Observable obs, Boolean oldValue, Boolean newValue) {
        setSelected(newValue);
    }

    
    public static void bind(ToggleButton button, PopOver popover) {
        

         popover.addEventFilter(POPOVER_CLOSE_REQUEST, event -> {
            popover.hide();

        });
        popover.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue && !button.isSelected()) {
                button.setSelected(true);
            }
            if (!newValue && button.isSelected()) {
                button.setSelected(false);
            }

        });

        button.setOnAction(action -> {
            if (!popover.isShowing()) {
                popover.show(button);
            } else {
                popover.hide();
            }
            
        });
        
    }
    public static PopOver bind(ToggleButton button, Node panel, ArrowLocation location) {

        PopOver popover = new PopOver(panel);

        popover.setArrowLocation(location);
        
        bind(button,popover);
        
        return popover;
        /*
        popover.addEventHandler(POPOVER_CLOSE_REQUEST, event -> {
            popover.hide();

        });
        popover.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue && !button.isSelected()) {
                button.setSelected(true);
            }
            if (!newValue && button.isSelected()) {
                button.setSelected(false);
            }

        });

        button.setOnAction(action -> {
            if (!popover.isShowing()) {
                popover.show(button);
            } else {
                popover.hide();
            }
            
        });*/

    }

    public static final EventType<? extends Event> POPOVER_CLOSE_REQUEST = new EventType("Error Button popover close request");
    /*
    public static void bind(ToggleButton button, Node panel, ArrowLocation location) {
        bind(button, new PopOver(panel),location);
    }*/
}
