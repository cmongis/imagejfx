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
import ijfx.ui.main.ImageJFX;
import ijfx.service.ui.AppService;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.web.WebView;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import mongis.utils.FXUtilities;
import ijfx.ui.UiConfiguration;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(context = "webapp", localization = "centerStackPane", id = "webapp-container")
public class WebAppContainer implements UiPlugin {

    WebView webView;

    Logger logger = ImageJFX.getLogger();
    
    @Parameter
    AppService appService;

    @Override
    public Node getUiElement() {
        return webView;
    }

    @Override
    public UiPlugin init() {
        try {
            FXUtilities.runAndWait(() -> {
                webView = new WebView();
                appService.bindWebView(webView);
            });
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return this;
    }

}
