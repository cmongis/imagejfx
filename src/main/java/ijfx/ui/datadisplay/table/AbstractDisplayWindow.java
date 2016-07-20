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
package ijfx.ui.datadisplay.table;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import jfxtras.scene.control.window.CloseIcon;
import jfxtras.scene.control.window.Window;
import org.scijava.Context;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayDeletedEvent;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public abstract class AbstractDisplayWindow<T extends Display<?>> extends Window {

    private T display;

    @Parameter
    EventService eventService;

    @Parameter
    DisplayService displayService;

    static String TITLE_CLASS_NAME = "ijfx-window-titlebar";

    static String WINDOW_CLASS_NAME = "ijfx-window";

    public AbstractDisplayWindow(Context context) {
        context.inject(this);
        setContentPane(init());

        for (EventType<? extends MouseEvent> t : new EventType[]{MouseEvent.MOUSE_CLICKED, MouseEvent.DRAG_DETECTED, MouseEvent.MOUSE_PRESSED}) {
            addEventHandler(t, this::putInFront);
            getContentPane().addEventHandler(t, this::putInFront);

        }

        // close icon
        CloseIcon closeIcon = new CloseIcon(this);

        getRightIcons().add(closeIcon);

        setOnCloseAction(this::onWindowClosed);

        getStyleClass().add(WINDOW_CLASS_NAME);
        setTitleBarStyleClass(TITLE_CLASS_NAME);
        
        setPrefSize(500, 500);
        
    }

    abstract protected void display(T display);

    abstract protected Pane init();

    public T getDisplay() {
        return display;
    }

    public void show(T display) {
        this.display = display;
        display(display);
    }

    protected void onWindowClosed(ActionEvent event) {

        //mageDisplayService.getActiveDataset(imageDisplay).
        getDisplay().close();

        eventService.publishLater(new DisplayDeletedEvent(getDisplay()));
    }

    void putInFront(Event event) {
        displayService.setActiveDisplay(getDisplay());

    }

}
