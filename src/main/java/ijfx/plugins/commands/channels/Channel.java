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
package ijfx.plugins.commands.channels;

import net.imagej.Dataset;
import net.imglib2.display.ColorTable;

/**
 *
 * @author cyril
 */
public interface Channel {
    
    
    public double getChannelMin();
    public double getChannelMax();
    public void setChannelMin(double min);
    public void setChannelMax(double max);
    public String getChannelName();
    public void setChannelName(String name);
    public ColorTable getColorTable();
    public void setColorTable(ColorTable colorTable);
    
    public default void apply(Dataset dataset,int channel) {
        dataset.setChannelMaximum(channel, getChannelMax());
        dataset.setChannelMinimum(channel, getChannelMin());
        dataset.setColorTable(getColorTable(), channel);
    }
    
   
    public static final String FORMAT = "%s(%.1f - %.1f)";
   
    public static String toString(Channel settings) {
        
        
        return String.format(FORMAT,settings.getChannelName(),settings.getChannelMin(),settings.getChannelMax());
        
        
        
    }
    
}
