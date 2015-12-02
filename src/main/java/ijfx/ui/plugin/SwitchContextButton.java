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

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.main.Localization;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */

@Plugin(type=UiPlugin.class)
@UiConfiguration(id="switch-context-button",localization=Localization.TOP_LEFT,order=0.4,context="always debug imagej browser")
public class SwitchContextButton extends Button implements UiPlugin{

    
    @Parameter
    UiContextService uiContextService;
   
    @Parameter
    EventService eventService;
    
    public SwitchContextButton() {
    
        super(null,GlyphsDude.createIcon(FontAwesomeIcon.BARS));
        
        setOnAction(this::onAction);
        
       getStyleClass().add("icon");
        
    
    }
    public UiPlugin init() {
        return this;
    }
    public void onAction(ActionEvent event) {
        eventService.publish(new DebugEvent("sideMenu"));
    }

    @Override
    public Node getUiElement() {
        return this;
    }

   
    
}
