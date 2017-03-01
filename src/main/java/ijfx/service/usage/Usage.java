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
package ijfx.service.usage;

import ijfx.ui.main.ImageJFX;
import io.socket.client.IO;
import io.socket.client.Socket;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.WeakChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import net.mongis.usage.DecisionStorage;
import net.mongis.usage.DefaultUsageFactory;
import net.mongis.usage.PreferenceDecisionStorage;
import net.mongis.usage.UsageFactory;
import net.mongis.usage.UsageLocation;
import net.mongis.usage.UsageType;

/**
 *
 * @author cyril
 */
public class Usage {

    private static final String updateServerAddress = "http://update.imagejfx.net";

    private static Socket socket;

    private static UsageFactory factory;

    public static Socket getSocket() {
        try {
            if (socket == null) {
                socket = IO.socket(updateServerAddress);
                socket.connect();
            }

        } catch (URISyntaxException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, "Error when connecting...", ex);
        }
        return socket;
    }

    public static UsageFactory factory() {

        if (factory == null) {

            while (getSocket() == null) {
                ImageJFX.getLogger().log(Level.WARNING, "Waiting for socket");
            }

            // create a decision storage strategy based on the preference of the user
            DecisionStorage storage = new PreferenceDecisionStorage(Usage.class, "debug-5");
            
            // create the factory and wrap the storage strategy into a logging wrapper
            factory = new DefaultUsageFactory(getSocket(),new LoggingDecisionStorageWrapper(storage));
            

            factory.createUsageLog(UsageType.SET, "CPU", UsageLocation.GENERAL)
                    .setValue(Runtime.getRuntime().availableProcessors())
                    .send();

            factory.createUsageLog(UsageType.SET, "RAM", UsageLocation.GENERAL)
                    .setValue(Runtime.getRuntime().maxMemory() / 1000 / 1000)
                    .send();
            
            factory.createUsageLog(UsageType.SET,"DATE",UsageLocation.GENERAL)
                    .setValue(new SimpleDateFormat("yyyy.MM.dd").format(new Date()))
                    .send();

        }
        return factory;
    }

    
    private static class LoggingDecisionStorageWrapper implements DecisionStorage{
        private final DecisionStorage decisionStorer;

        private Logger logger = ImageJFX.getLogger();
        
        public LoggingDecisionStorageWrapper(DecisionStorage decisionStorer) {
            this.decisionStorer = decisionStorer;
        }

        @Override
        public boolean hasDecided() {
            return decisionStorer.hasDecided();
        }

        @Override
        public boolean hasAccepted() {
            return decisionStorer.hasAccepted();
        }

        @Override
        public void setDecision(boolean bln) {
          if(bln) {
              logger.info("The user decided to share his usage information");
          }
          else {
              logger.info("the user refused to share his usage information");
          }
          decisionStorer.setDecision(bln);
        }
        
        
    }
    
    public static void listenButton(Button button, UsageLocation location, String id) {
        button.addEventHandler(ActionEvent.ANY, event -> {

            factory()
                    .createUsageLog(UsageType.CLICK, id, location)
                    .send();

        });
    }
    
    public static void listenButtons(Node container, UsageLocation location) {
        container
                .lookupAll(".button")
                .stream()
                .map(node->(Button)node)
                .filter(node->node.getClass().isAssignableFrom(ToggleButton.class) == false)
                .forEach(button->{
                    listenButton(button, location, button.getText());
                });
    }

    public static void listenProperty(ReadOnlyProperty<? extends Object> property, String id, UsageLocation location) {
        property.addListener((obs, oldValue, newValue) -> {
            factory()
                    .createUsageLog(UsageType.SET, id, location)
                    .setValue(obs.toString())
                    .send();
        });
    }

    public static void listenSwitch(ReadOnlyProperty<? extends Boolean> property, String id, UsageLocation location) {
        property.addListener(new WeakChangeListener<>((obs, oldValue, newValue) -> {
            factory()
                    .createUsageLog(UsageType.SET, id, location)
                    .setValue(newValue.booleanValue())
                    .send();
        }));
    }
    
    

    public final static UsageLocation MENUBAR = new UsageLocation("imagej-menu-bar");

}
