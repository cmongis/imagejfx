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
package ijfx.service.workflow;

import java.util.Map;
import org.scijava.module.Module;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public interface WorkflowStep {

    public static StepParameterType DEFAULT_PARAMETER_TYPE = StepParameterType.PRESET;

    public String getId();

    public void setId(String id);

    public Module getModule();

    public Map<String, Object> getParameters();

    public void setParameterType(String parameter, StepParameterType type);

    public StepParameterType getParameterType(String parameter);

    public Map<String, StepParameterType> getParameterTypes();

}
