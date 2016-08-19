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
package ijfx.service.workflow;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ijfx.service.IjfxService;
import ijfx.service.workflow.json.ThresholdMethodDeserializer;
import ijfx.service.workflow.json.ThresholdMethodModule;
import ijfx.service.workflow.json.ThresholdMethodSerializer;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.threshold.AbstractThresholdMethod;
import net.imagej.threshold.DefaultThresholdMethod;
import net.imagej.threshold.ThresholdMethod;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class WorkflowIOService extends AbstractService implements IjfxService {

    final private ObjectMapper mapper = new ObjectMapper();

    public WorkflowIOService() {
        super();

    }

    @Override
    public void initialize() {

        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //mapper.registerModule(new ThresholdMethodModule(getContext()));
        
    }

    private <T> Module generateModule(Class<T> t, JsonSerializer<T> serializer, JsonDeserializer<T> deserializer) {
        SimpleModule m = new SimpleModule();
        m.addDeserializer(t, deserializer);
        m.addSerializer(t, serializer);
        m.registerSubtypes(DefaultThresholdMethod.class);
        return m;
    }

    public Workflow loadWorkflow(String jsonString) {
        return null;
    }

    public Workflow loadWorkflow(File file) {
        try {
            Workflow workflow = mapper.readValue(file, Workflow.class);
            workflow.getStepList().forEach(getContext()::inject);
            return workflow;
        } catch (IOException ex) {
            Logger.getLogger(WorkflowIOService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void saveWorkflow(Workflow workflow, File dest) {

        try {
            mapper.writeValue(dest, workflow);
        } catch (IOException ex) {
            Logger.getLogger(WorkflowIOService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
