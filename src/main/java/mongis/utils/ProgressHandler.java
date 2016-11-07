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
package mongis.utils;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public interface ProgressHandler {
    
    
    public void setProgress(double progress);
    public void setProgress(double workDone,double total);
    public void setProgress(long workDone,long total);
    public void setStatus(String message);
    public default void setStatus(String format, Object... params) {
        setStatus(String.format(format,params));
    }
    public void setTotal(double total);
    public void increment(double inc);
    public boolean isCancelled();
    
    public static ProgressHandler check(ProgressHandler handler) {
        return handler == null ? new SilentProgressHandler() : handler;
    }
    
}
