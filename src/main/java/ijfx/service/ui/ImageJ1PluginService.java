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
package ijfx.service.ui;

import ij.ImagePlus;
import ij.Menus;
import java.awt.Menu;
import java.awt.MenuItem;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;
import net.imagej.legacy.LegacyImageMap;
import net.imagej.legacy.LegacyService;
import org.scijava.display.DisplayService;
import org.scijava.options.OptionsService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class ImageJ1PluginService extends AbstractService implements ImageJService{

    @Parameter
    LegacyService legacyService;

    @Parameter
    OptionsService optionsService;
    
    
    public List<ImageJCommand> getCommandListFromMenu() {
        ArrayList<ImageJCommand> commands = new ArrayList<>();

        Menus.getCommands().forEach((label, command) -> {
            String path = getCommandMenuPath(label.toString());
            commands.add(new ImageJCommand(label.toString(), path, command.toString()));
        });

        return commands;
    }
    
    @Parameter
    DisplayService displayService;

    public class ImageJCommand {

        private String label;
        private String path;
        private String command;

        public ImageJCommand(String label, String path, String command) {
            this.label = label;
            this.path = path;
            this.command = command;
        }

        public String getLabel() {
            return label;
        }

        public ImageJCommand setLabel(String label) {
            this.label = label;

            return this;
        }

        public String getPath() {
            return path;
        }

        public ImageJCommand setPath(String path) {
            this.path = path;
            return this;
        }

        public String getCommand() {
            return command;
        }

        public ImageJCommand setCommand(String command) {
            this.command = command;
            return this;
        }

    }

    public String getCommandMenuPath(String command) {
        return getCommandMenuPath(Menus.getMenuBar(), command);
    }

    public String getCommandMenuPath(java.awt.MenuBar bar, String command) {

        for (int i = 0; i != bar.getMenuCount(); i++) {

            String path = getCommandMenuPath(bar.getMenu(i), command);

            if (path != null) {
                return bar.getMenu(i).getLabel() + separator + path;
            }

        }
        return null;
    }
    public static String separator = "/";

    public String getCommandMenuPath(Menu parent, String command) {
        for (int i = 0; i != parent.getItemCount(); i++) {
            MenuItem child = parent.getItem(i);
            if (child instanceof Menu) {
               
                String path = getCommandMenuPath((java.awt.Menu) child, command);
                if (path != null) {
                    return child.getLabel() + separator + path;
                }
            } else if (child.getLabel() == command) {
                return command;
            }
        }
        return null;
    }

    public void executeCommand(ImageJCommand command) {
        
        if(!legacyService.isSyncEnabled()) {
            ImageDisplay activeDisplay = displayService.getActiveDisplay(ImageDisplay.class);
            
            
            
           final LegacyImageMap imageMap = legacyService.getImageMap();
            final ImagePlus activeImagePlus = imageMap.lookupImagePlus(activeDisplay);
            
           if(activeDisplay != null && activeImagePlus == null) {
//            System.out.println(activeImagePlus.getWindow());
               
            legacyService.getImageMap().registerDisplay(activeDisplay);
           }
           else {
                
           }
        }
        
        legacyService.syncActiveImage();
        
        boolean isEnabled = legacyService.isInitialized();
        boolean isInitialazed = legacyService.isInitialized();
        
        LegacyImageMap legacyImageMap = legacyService.getImageMap();
        
        //System.out.println(displayService.getActiveDisplay(ImageDisplay.class));
        legacyService.getIJ1Helper().syncActiveImage(displayService.getActiveDisplay(ImageDisplay.class));
        
        
        String[] p = parseCommand(command.getCommand());
        if (p != null && p.length > 1) {
            legacyService.runLegacyCommand(p[0], p[1]);
        } else {
            legacyService.runLegacyCommand(command.getCommand(), "");
        }
        //legaxcyService.runLegacyCompatibleCommand(command.getCommand());
    }

    Pattern cmdPattern = Pattern.compile("(.*)\\(\"(.*)\"\\)");

    private String[] parseCommand(String command) {

        Matcher m = cmdPattern.matcher(command);
        if (m.find()) {
           
            return new String[]{m.group(1), m.group(2)};
        }

        /*
         if(command.contains("\\(\"")) {
         String[] result =  command.split("\\(\"");
         result[1] = result[1].replace("\"\\)", "");
         return result;
         }*/
        return null;
    }

}
