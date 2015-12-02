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
package ijfx.bridge;

import java.util.List;
import net.imagej.Data;
import net.imagej.Dataset;
import net.imagej.Position;
import net.imagej.display.DataView;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayView;
import net.imagej.overlay.Overlay;
import org.scijava.Priority;
import org.scijava.display.DisplayService;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.script.ScriptService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Curtis Rueden
 * 
 * This ImageDisplayService is just a copy of the DefaultImageDisplayService that overlap
 * prevent the LegacyImageService
 */
@Plugin(type = Service.class,priority = Priority.VERY_HIGH_PRIORITY+1)
public class PowerImageDisplayService extends AbstractService implements ImageDisplayService{
    	

	@Parameter
	private EventService eventService;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private DisplayService displayService;

	@Parameter
	private ScriptService scriptService;

	// -- ImageDisplayService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public PluginService getPluginService() {
		return pluginService;
	}

	@Override
	public DisplayService getDisplayService() {
		return displayService;
	}

	@Override
	public DataView createDataView(final Data data) {
		for (final DataView dataView : getDataViews()) {
			if (dataView.isCompatible(data)) {
				dataView.initialize(data);
				return dataView;
			}
		}
		throw new IllegalArgumentException("No data view found for data: " + data);
	}

	@Override
	public List<? extends DataView> getDataViews() {
		return pluginService.createInstancesOfType(DataView.class);
	}

	@Override
	public ImageDisplay getActiveImageDisplay() {
		return displayService.getActiveDisplay(ImageDisplay.class);
	}

	@Override
	public Dataset getActiveDataset() {
		return getActiveDataset(getActiveImageDisplay());
	}

	@Override
	public DatasetView getActiveDatasetView() {
		return getActiveDatasetView(getActiveImageDisplay());
	}
	
	@Override
	public Position getActivePosition() {
		return getActivePosition(getActiveImageDisplay());
	}

	@Override
	public Dataset getActiveDataset(final ImageDisplay display) {
		final DatasetView activeDatasetView = getActiveDatasetView(display);
		return activeDatasetView == null ? null : activeDatasetView.getData();
	}

	@Override
	public DatasetView getActiveDatasetView(final ImageDisplay display) {
		if (display == null) return null;
		final DataView activeView = display.getActiveView();
		if (activeView instanceof DatasetView) {
			return (DatasetView) activeView;
		}
		return null;
	}
	
	@Override
	public Position getActivePosition(final ImageDisplay display) {
		if (display == null) return null;
		final DatasetView activeDatasetView = this.getActiveDatasetView(display);
		if(activeDatasetView == null) return null;
		return activeDatasetView.getPlanePosition();
	}

	@Override
	public List<ImageDisplay> getImageDisplays() {
		return displayService.getDisplaysOfType(ImageDisplay.class);
	}

	@Override
	public void initialize() {
		scriptService.addAlias(ImageDisplay.class);
		scriptService.addAlias(DatasetView.class);
		scriptService.addAlias(DataView.class);
		scriptService.addAlias(OverlayView.class);
		scriptService.addAlias(Overlay.class);
	}
}

