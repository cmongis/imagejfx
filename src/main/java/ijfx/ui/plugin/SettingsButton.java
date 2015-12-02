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
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.service.ui.AppService;
import ijfx.service.uicontext.UiContextService;
import javafx.scene.Node;
import javafx.scene.control.Button;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import ijfx.ui.UiConfiguration;

/**
 *
 * @author Cyril MONGIS, 2015
 */
//@Plugin(type = UiPlugin.class)
//@UiConfiguration(id = "settings-button", localization = "topRightHBox", context = "imagej image-browser",order=3.0)
public class SettingsButton implements UiPlugin {

    Button button;

    @Parameter
    UiContextService contextService;

    @Parameter
    AppService appService;

    public SettingsButton() {
        button = GlyphsDude.createIconButton(FontAwesomeIcon.GEAR, "Settings");
    }

    @Override
    public Node getUiElement() {
        return button;
    }

    @Override
    public UiPlugin init() {

        button.setOnAction(action -> {
            action();
        });

        return this;

    }

    public void action() {
        //contextService.leave("imagej");
        contextService.enter("webapp");
        appService.showApp("index");
       // contextService.update();

        button.getGraphic().getStyleClass().forEach(s -> System.out.println(s));
    }
}
