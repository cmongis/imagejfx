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

import netscape.javascript.JSException;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class JSMessage {
    int line;
    String title;
    String reason;
    String text;
    Exception exception;
    
    long date;
    
    JSMessageType type;
   
    
    
    private final String STR_FORMAT = "[JSMESSAGE = %s]\ntitle = %s\nreason = %s\nline = %d\ntext = %s";

    public String toString() {
        return String.format(STR_FORMAT, type, title, reason, line, text);
    }

    public JSMessage(JSMessageType type) {
        date = System.currentTimeMillis();
        setType(type);
    }

    public JSMessage(JSException e) {
        this(JSMessageType.ERROR);
        setTitle("Error when initializaing script.");
        setException(e);
        
        setText(e.getLocalizedMessage());
    }

    public JSMessage(JSMessageType type, String text) {
        this(type);
        setTitle("");
        setText(text);
    }

    public JSMessageType getType() {
        return type;
    }

    public void setType(JSMessageType type) {
        this.type = type;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
    
}
