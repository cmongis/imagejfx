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
import ijfx.ui.main.Localization;
import ijfx.ui.tool.FxTool;
import ijfx.service.uicontext.UiContextService;
import java.util.logging.Level;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import ijfx.ui.UiConfiguration;
import ijfx.service.uiplugin.UiPluginService;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.tool.FxToolService;
import java.util.Map;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "imagej-toolbar", context = "imagej+visualize+image-open", localization = Localization.LEFT)
public class ImageJToolBar extends VBox implements UiPlugin {

    @Parameter
    Context context;

    @Parameter
    UiContextService contextService;

    @Parameter
    UiPluginService loaderService;

    @Parameter
    PluginService pluginService;

    @Parameter
    FxToolService fxToolService;

    @Override
    public Node getUiElement() {

        return this;
    }

    @Override
    public UiPlugin init() {
        for (FxTool tool : fxToolService.getTools()) {
            getChildren().add(tool.getNode());

            if (ToggleButton.class.isAssignableFrom(tool.getNode().getClass())) {

                ToggleButton button = (ToggleButton) tool.getNode();
                button.setText("");
            }
        }
        return this;
    }

}
