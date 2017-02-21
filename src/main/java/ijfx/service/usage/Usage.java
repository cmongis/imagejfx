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
import java.util.logging.Level;
import javafx.beans.property.ReadOnlyProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import net.mongis.usage.DefaultUsageFactory;
import net.mongis.usage.UsageFactory;
import net.mongis.usage.UsageLocation;
import net.mongis.usage.UsageType;

/**
 *
 * @author cyril
 */
public class Usage {

    private static final String updateServerAddress = "http://localhost:8008/";

    private static Socket socket;

    private static UsageFactory factory;
    
    public static Socket getSocket() {
        try {
            if (socket == null) {
                socket = IO.socket(updateServerAddress);
                 socket.connect();
            }
           
        } catch (URISyntaxException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, "Error when connecting...",ex);
        }
        return socket;
    }
    
    public static UsageFactory factory() {
        
        if(factory == null) {
            
            while(getSocket() == null) {
                ImageJFX.getLogger().log(Level.WARNING, "Waiting for socket");
            }
            
            factory = new DefaultUsageFactory(getSocket());
            factory.setDecision(Boolean.TRUE);
        }   
        return factory;
    }
    
    public static void listenButton(Button button,UsageLocation location, String id) {
        button.addEventHandler(ActionEvent.ANY, event->{
            
            factory()
                    .createUsageLog(UsageType.CLICK, id, location)
                    .send();
        
        });
    }
    public static void listenProperty(ReadOnlyProperty<? extends Object> property, String id,UsageLocation location) {
        property.addListener((obs,oldValue,newValue)->{
            factory()
                    .createUsageLog(UsageType.SET,id,location)
                    .setValue(obs.toString())
                    .send();
        });
    }
    
    public static void listenSwitch(ReadOnlyProperty<? extends Boolean> property, String id, UsageLocation location) {
       property.addListener((obs,oldValue,newValue)->{
            factory()
                    .createUsageLog(UsageType.SET,id,location)
                    .setValue(newValue.booleanValue())
                    .send();
        });
    }
    
    
    public final static UsageLocation MENUBAR = new UsageLocation("imagej-menu-bar");

}
