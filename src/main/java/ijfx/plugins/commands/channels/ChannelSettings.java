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

import java.util.List;
import net.imagej.Dataset;

/**
 *
 * @author cyril
 */
public interface ChannelSettings {

    public int getChannelCount();

    public List<Channel> getChannelSettings();

    public void setChannels(List<Channel> settings);

    public default Channel get(int i) {
        return getChannelSettings().get(i);
    }

    public static void applyTo(ChannelSettings set, Dataset dataset) {
        if (set != null) {
            for (int i = 0; i != set.getChannelCount(); i++) {
                // applying the settings to the dataset
                set.get(i).apply(dataset, i);
            }
        }

    }

}
