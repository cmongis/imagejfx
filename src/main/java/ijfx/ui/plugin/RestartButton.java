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
import ijfx.ui.notification.DefaultNotificationService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import org.scijava.plugin.Plugin;
import ijfx.ui.UiConfiguration;

/**
 *
 * @author Cyril MONGIS, 2015
 */
//@Plugin(type = UiPlugin.class)
//@UiConfiguration(context = DefaultNotificationService.UPDATE_AVAILABLE_CONTEXT,id="restart-button",localization = Localization.TOP_LEFT,order = 0.0)
public class RestartButton extends AbstractContextButton{

    
    public RestartButton() {
        super("Restart", FontAwesomeIcon.RECYCLE);
    }
    
    private final String UPDATER_JAR = "start-ijfx.jar";
    
    Logger logger = ImageJFX.getLogger();
    
    @Override
    public void onAction(ActionEvent event) {
        
        
        String startCmd = String.format("java -jar %s", UPDATER_JAR);


        ProcessBuilder processBuilder = new ProcessBuilder(startCmd.split(" "));

        try {
            processBuilder.start();
            System.exit(0);
        } catch (IOException ex) {
            
            logger.log(Level.SEVERE, null, ex);
        }

        
        
    }

    @Override
    public UiPlugin init() {
        return this;
    }
    
}
