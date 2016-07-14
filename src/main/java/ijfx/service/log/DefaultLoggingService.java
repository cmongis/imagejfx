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
package ijfx.service.log;

import ijfx.service.IjfxService;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.LogRecorderService;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import mercury.core.LogEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mercury.core.LogEntryType;
import netscape.javascript.JSException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.scijava.event.EventService;
import org.scijava.event.SciJavaEvent;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class DefaultLoggingService extends AbstractService implements IjfxService {

    protected final ObservableList<LogEntry> errorList = FXCollections.observableArrayList();

    protected int errorCount = 0;

    String reportAddress = "http://www.imagejfx.net/report/send/";

    Logger logger = ImageJFX.getLogger();
    
    int lastLevel = LogService.NONE;
    
    Handler handler = new Handler() {

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel() == Level.SEVERE && record.getThrown() != null) {
                notifyError(new LogEntry(record.getThrown()).setTitle(record.getMessage()));
            }
            if (record.getLevel() == Level.WARNING && record.getThrown() != null) {
                notifyError(new LogEntry(record.getThrown()).setTitle(record.getMessage()).setType(LogEntryType.WARNING));
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    };

    public DefaultLoggingService() {
        super();

        //Logger.getGlobal().addHandler(handler);
        ImageJFX.getLogger().addHandler(handler);

    }

    @Parameter
    EventService eventService;

    public void notifyError(LogEntry error) {
        errorList.add(error);
        eventService.publishLater(new LogErrorEvent(error));

        if (error.getType() == LogEntryType.ERROR) {
            errorCount++;
        }

    }

    public void info(String format, Object... obj) {
        logger.info(String.format(format,obj));
    }
    
    public void severe(Throwable throwable) {
        logger.log(Level.SEVERE,null,throwable);
    }
    
    public void warn(Throwable throwable, String format, Object... params) {
        logger.log(Level.WARNING,String.format(format,params),throwable);
    }
    
    public void resetErrorCount() {
        errorCount = 0;
        eventService.publishLater(new CountResetChange());
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void notifyError(JSException e) {
        notifyError(new LogEntry(e));

    }

    public ObservableList<LogEntry> getErrorList() {
        return errorList;
    }

   
    public void debug(Object o) {
        logger.info(o.toString());
        setLevel(LogService.DEBUG);
    }

 
    public void debug(Throwable thrwbl) {
        logger.log(Level.FINE,null,thrwbl);
        setLevel(LogService.DEBUG);
    }

  
    public void debug(Object o, Throwable thrwbl) {
        logger.log(Level.FINE,o.toString(),thrwbl);
        setLevel(LogService.DEBUG);
    }

   
    public void error(Object o) {
        logger.severe(o.toString());
        setLevel(LogService.ERROR);
    }

  
    public void error(Throwable thrwbl) {
        logger.log(Level.SEVERE,null,thrwbl);
        setLevel(LogService.ERROR);
    }

   
    public void error(Object o, Throwable thrwbl) {
        logger.log(Level.SEVERE,o.toString(),thrwbl);
        setLevel(LogService.ERROR);
    }

  
    public void info(Object o) {
        logger.info(o.toString());
    }

   
    public void info(Throwable thrwbl) {
        logger.log(Level.INFO,null,thrwbl);
        setLevel(LogService.INFO);
    }

   
    public void info(Object o, Throwable thrwbl) {
        logger.log(Level.INFO,o.toString(),thrwbl);
        setLevel(LogService.INFO);
    }
    
   

  
    public void trace(Object o) {
        logger.finer(o.toString());
        setLevel(LogService.TRACE);
    }

 
    public void trace(Throwable thrwbl) {
        logger.log(Level.INFO,null,thrwbl);
        setLevel(LogService.TRACE);
    }

   
    public void trace(Object o, Throwable thrwbl) {
        logger.log(Level.FINER,o.toString(),thrwbl);
        setLevel(LogService.TRACE);
    }

  
    public void warn(Object o) {
        logger.warning(o.toString());
    }

    public void warn(String format, Object... params) {
        logger.warning(String.format(format,params));
    }
    
  
    public void warn(Throwable thrwbl) {
        logger.log(Level.WARNING,null,thrwbl);
        setLevel(LogService.WARN);
    }

    public void warn(Object o, Throwable thrwbl) {
        logger.log(Level.WARNING,o.toString(),thrwbl);
        setLevel(LogService.WARN);
    }

   
    public boolean isDebug() {
        return lastLevel == LogService.DEBUG;
    }

  
    public boolean isError() {
        return lastLevel == LogService.ERROR;
    }

 
    public boolean isInfo() {
        return lastLevel == LogService.INFO;
    }

  
    public boolean isTrace() {
        return lastLevel == LogService.TRACE;
    }

  
    public boolean isWarn() {
         return lastLevel == LogService.WARN;
    }

  
    public int getLevel() {
        return lastLevel;
    }

 
    public void setLevel(int i) {
        lastLevel = i;
    }

    public class LogErrorEvent extends SciJavaEvent {

        LogEntry error;

        public LogErrorEvent(LogEntry error) {
            setError(error);
        }

        public LogEntry getError() {
            return error;
        }

        public void setError(LogEntry error) {
            this.error = error;
        }

    }

    public class CountResetChange extends SciJavaEvent {
    }

    public Boolean sendMessageToDeveloper(String sender, String description) throws UnsupportedEncodingException, IOException {

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(reportAddress);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("description", description));
        params.add(new BasicNameValuePair("sender", sender));
        params.add(new BasicNameValuePair("log", getLogAsText()));

        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                // do something useful
            } finally {
                instream.close();
            }
        } else {
            return false;
        }
        return true;
    }

    public String getLogAsText() {
        return LogRecorderService.getInstance().getLog();

    }

}
