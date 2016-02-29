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
import ijfx.service.log.LogService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import ijfx.ui.UiConfiguration;

/**
 *
 * @author Cyril MONGIS, 2015
 */

public class ErrorButton extends AbstractContextButton {

    PopOver popover;

    @Parameter
    LogService logEntryService;

    LogPanel errorPanel;
    
    

    @Parameter
    EventService eventService;

    @Parameter
    Context context;

    public static final EventType POPOVER_CLOSE_REQUEST = new EventType("Error Button popover close request");
    
    
    public ErrorButton() {
        //super();
        super("", FontAwesomeIcon.INFO);
        popover = new PopOver();
        getButton().getStyleClass().add("icon");
    }

    @Override
    public void onAction(ActionEvent event) {
       
        if (popover.isShowing()) {
          
            popover.hide();
            return;
        }
        updateButton();
        popover.setAutoHide(true);
        popover.show(getButton());
    }

    @Override
    public UiPlugin init() {

        try {

            errorPanel = new LogPanel();
            popover.setContentNode(errorPanel);

            context.inject(errorPanel);
            errorPanel.init();
            errorPanel.addEventHandler(POPOVER_CLOSE_REQUEST, event->{
                popover.hide();
            });
            
            
            
            

        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE,null,ex);;
        }

        return this;
    }
    
    public void updateButton() {
        if(logEntryService.getErrorCount() > 0) {
            getButton().getStyleClass().add(ImageJFX.BUTTON_DANGER_CLASS);
            getButton().setText(logEntryService.getErrorCount()+"");
        }
        else {
            getButton().setText("");
            getButton().getStyleClass().remove(ImageJFX.BUTTON_DANGER_CLASS);
            getButton().getStyleClass().remove(ImageJFX.BUTTON_DANGER_CLASS);
        }
    }
    
    @EventHandler
    public void handleEvent(LogService.LogErrorEvent event) {
        Platform.runLater(()->updateButton());
    }
    
    @EventHandler
    public void handleEvent(LogService.CountResetChange event) {
        Platform.runLater(()->updateButton());
    }
    
    
}
