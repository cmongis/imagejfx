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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.imagej.Dataset;
import net.imagej.display.DatasetView;
import net.imglib2.display.ColorTable;

/**
 *
 * @author cyril
 */
public class DefaultChannelSettings implements ChannelSettings {

    List<Channel> channelSettings = new ArrayList<>();

    @Override
    public int getChannelCount() {
        return channelSettings.size();
    }

    @Override
    public List<Channel> getChannelSettings() {
        return channelSettings;
    }

    @Override
    public void setChannels(List<Channel> settings) {
        channelSettings = settings;
    }
    
    public String toString() {
        return getChannelSettings()
                .stream()
                .map(channel->channel.toString())
                .collect(Collectors.joining(","));
    }

    public void importFromDataset(Dataset dataset) {

        long channelCount = dataset.getChannels();
        channelSettings.clear();
        double min, max;
        ColorTable table;
        for (int i = 0; i != channelCount; i++) {

            min = dataset.getChannelMinimum(i);
            max = dataset.getChannelMaximum(i);

            table = dataset.getColorTable(i);
            Channel settings = new DefaultChannel();
            settings.setChannelMin(min);
            settings.setChannelMax(max);
            settings.setColorTable(table);

            channelSettings.add(settings);
        }

    }

    public DefaultChannelSettings importFromDatasetView(DatasetView view) {

        long channelCount = view.getChannelCount();
        channelSettings.clear();
        double min, max;
        ColorTable table;
        
        for (int i = 0; i != channelCount; i++) {
            min = view.getChannelMin(i);
            max = view.getChannelMax(i);
            table = view.getColorTables().get(i);
            Channel settings = new DefaultChannel();
            settings.setChannelMin(min);
            settings.setChannelMax(max);
            settings.setColorTable(table);
            channelSettings.add(settings);
        }
        return this;
    }
    
    
    

}
