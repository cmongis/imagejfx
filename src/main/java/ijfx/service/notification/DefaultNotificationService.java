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
package ijfx.service.notification;

import ijfx.ui.main.ImageJFX;
import ijfx.service.uicontext.UiContextService;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class DefaultNotificationService extends AbstractService implements NotificationService {

    List<Notification> notificationList = new ArrayList<>();

    @Parameter
    EventService eventService;

    @Parameter
    UiContextService contextService;

    final static private Logger logger = ImageJFX.getLogger();

    //private static final String updateServerAddress = "http://update.imagejfx.net/";
    private static final String updateServerAddress = "http://localhost:3000/";
    private static final String EVENT_WELCOME = "welcome";
    private static final String EVENT_NEW_UPDATE = "new update";
    private static final String REQUEST_SUBSCRIBE = "subscribe/update";
    private static final String REQUEST_USER_NUMBER = "subscribe/user_number";

    private Socket socket;

    public static final String UPDATE_AVAILABLE_CONTEXT = "update-available";

    public DefaultNotificationService() {
        super();

        initializeSocket();

    }

    @Override
    public void publish(Notification notification) {
        notificationList.add(notification);
        eventService.publishLater(new NotificationEvent(notification));
    }

    @Override
    public List<Notification> getAllNotification() {

        return notificationList;
    }

    public void initializeSocket() {
        try {
            socket = IO.socket(updateServerAddress);

            socket
                    .on(Socket.EVENT_CONNECT, this::onSocketConnected)
                    .on(EVENT_WELCOME, this::onWelcomeMessage)
                    .on(EVENT_NEW_UPDATE, this::onNewUpdate)
                    .on(Socket.EVENT_DISCONNECT, this::onSocketDisconnected);
            socket.connect();

        } catch (URISyntaxException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void onSocketConnected(Object... args) {

        logger.info("[PushServer] Connected. Subscribing...");

        // subscribe to the update notification push service
        socket.emit(REQUEST_SUBSCRIBE);
        socket.emit(REQUEST_USER_NUMBER);
        
        eventService.publish(new SocketConnectedEvent().setObject(socket));
        

    }

    private void onWelcomeMessage(Object... args) {
        logger.info("[PushServer] Subscribed");
    }

    private void onSocketDisconnected(Object... args) {
        logger.info("[PushServer]disconnected");
    }

    // when a new update is pushed on the server
    private void onNewUpdate(Object... args) {

        // notify the view
        notifyNewUptdate();

        // entering a new context
        //contextService.enter(UPDATE_AVAILABLE_CONTEXT);
        //contextService.update();
    }

    private void notifyNewUptdate() {
        publish(new DefaultNotification("ImageJFX : new update available !", "Restart ImageJFX to download the new modifications."));
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void notifiyServer(String reason, NotificationData data) {
        socket.emit(reason, data.getData());
    }

}
