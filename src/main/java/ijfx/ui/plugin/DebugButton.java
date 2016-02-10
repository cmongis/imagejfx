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
import ijfx.ui.batch.BatchProcessorConfigurator;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import ijfx.ui.notification.NotificationService;
import ijfx.service.ui.AppService;
import ijfx.service.ui.HintService;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import ijfx.ui.UiConfiguration;
import ijfx.service.uiplugin.UiPluginService;
import ijfx.ui.batch.FileBatchProcessorPanel;
import ijfx.ui.plugin.panel.LUTPanel;
import ijfx.ui.plugin.panel.OverlayManagerPanel;
import ijfx.ui.plugin.panel.OverlayPanel;
import ijfx.ui.project_manager.ProjectManager;
import org.scijava.event.EventService;

/**
 *
 * @author Cyril MONGIS, 2015
 */

@Plugin(type = UiPlugin.class)
@UiConfiguration(id="debug-button",context="debug",order=4.0,localization=Localization.TOP_RIGHT)
public class DebugButton extends MenuButton implements UiPlugin{

    @Parameter
    UiPluginService widgetService;
    
    @Parameter
    NotificationService notificationService;
    
    @Parameter
    EventService eventService;
    
    @Parameter
    AppService appService;
    
    @Parameter
    HintService hintService;
    
    public DebugButton() {
        super("D",GlyphsDude.createIcon(FontAwesomeIcon.BUG));
        
        addItem("Reload CSS",this::reloadCss);
        addItem("Reload Debug Button",event->widgetService.reload(DebugButton.class));
        addItem("Reload App Browser",event->appService.reloadCurrentView());
        addItem("Reload Batch Processing Screen",event->widgetService.reload(BatchProcessorConfigurator.class));
        addItem("Reload an other",this::reloadAnOther);
        addItem("Test hints",this::testHints);
        addItem("Show/Hide sideMenu",this::triggerDebugEvent);
        
    }
    
    
    @Override
    public Node getUiElement() {
        
        return this;
    }
    
    public void addItem(String label, EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem(label);
        item.setOnAction(handler);
        getItems().add(item);
    }

    @Override
    public UiPlugin init() {
        
        
        return this;
    }
    
    public void triggerDebugEvent(ActionEvent event) {
        eventService.publish(new DebugEvent("sideMenu"));
    }
    
    
    public void reloadCss(ActionEvent event) {
        
        String debugStyleSheet = "file:./src/main/resources/ijfx/ui/main/flatterfx.css";
        
         if(new File(debugStyleSheet.replace("file:","")).exists() == false) {

        }
        else {

        }
        
        getScene().getStylesheets().removeAll(ImageJFX.STYLESHEET_ADDR, debugStyleSheet);

        getScene().getStylesheets().add(debugStyleSheet);
        
        
        //eventService.publish(new DebugEvent("reloadSideMenu"));
    }
    
    public void reloadAnOther(ActionEvent event) {
         //  widgetService.reload(FXMLBrowserController.class);
       
        //widgetService.reload(OverlayManagerPanel.class);
        //widgetService.reload(OverlayPanel.class);
        widgetService.reload(ProjectManager.class);
        //notificationService.publish(new DefaultNotification("It works !","perfectly !").addAction("Tell him",()->System.out.println("Yeah")));
        
    }
    
    public void testHints(ActionEvent event) {
        hintService.displayHints(FileBatchProcessorPanel.class, true);
    }
    
}
