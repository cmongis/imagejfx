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
package mercury.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import netscape.javascript.JSException;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class LogEntry {
    int line;
    String title;
    String reason;
    String text;
    Exception exception;
    
    long date;
    
    LogEntryType type;
   
    
    
    private final String STR_FORMAT = "[JSMESSAGE = %s]\ntitle = %s\nreason = %s\nline = %d\ntext = %s";
    
    

    public String toString() {
        return String.format(STR_FORMAT, type, title, reason, line, text);
    }

    public LogEntry(LogEntryType type) {
        date = System.currentTimeMillis();
        setType(type);
    }

    public LogEntry(Exception e) {
        this(LogEntryType.ERROR);
        setException(e);
        setText(getStackTrace(e));
    }
    
    public LogEntry(JSException e) {
        this(LogEntryType.ERROR);
        setTitle("Error when initializaing script.");
        setException(e);
        
        setText(e.getLocalizedMessage());
    }

    public LogEntry(LogEntryType type, String text) {
        this(type);
        setTitle("");
        setText(text);
    }
    
    public LogEntry(LogEntryType type, String text, Exception e) {
        this(type,text);
        setException(e);
    }

    public LogEntryType getType() {
        return type;
    }

    public LogEntry setType(LogEntryType type) {
        this.type = type;
        return this;
    }

    
    
    
    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getTitle() {
        return title;
    }

    public LogEntry setTitle(String title) {
        this.title = title;
        return this;
    }





    public String getText() {
        return text;
    }

    public LogEntry setText(String text) {
        this.text = text;
        return this;
    }

    public Exception getException() {
        return exception;
    }

    public LogEntry setException(Exception exception) {
        this.exception = exception;
        return this;
    }
 
    
    public String toMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("-----------------------");
        builder.append(getType().toString());
        builder.append(toString());   
        if(exception != null) {
            builder.append(getStackTrace(exception));
        }
        return builder.toString();
    }
    
    public String getExceptionStackTrace() {
        if(exception != null) {
            return getStackTrace(exception);
        }
        else {
            return "No exception trace";
        }
    }
    
    public static String getStackTrace(final Throwable throwable) {
     final StringWriter sw = new StringWriter();
     final PrintWriter pw = new PrintWriter(sw, true);
     throwable.printStackTrace(pw);
     return sw.getBuffer().toString();
}
    
    
}
