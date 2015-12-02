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
package ijfx.ui.tool;

import net.imagej.ImageJService;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Basic implementation of the @FxToolService.
 * 
 * Mainly publishes a @ToolCahngeEvent.
 * 
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class DefaultFxToolService extends AbstractService implements FxToolService {

    FxTool currentTool;

    @Parameter
    EventService eventService;

    @Override
    public FxTool getCurrentTool() {
        return currentTool;
    }

    @Override
    public void setCurrentTool(FxTool currentTool) {
        this.currentTool = currentTool;
        eventService.publish(new ToolChangeEvent(currentTool));
    }

}
