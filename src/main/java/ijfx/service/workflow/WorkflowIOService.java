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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import ijfx.service.IjfxService;
import ijfx.service.workflow.json.ThresholdMethodDeserializer;
import ijfx.service.workflow.json.ThresholdMethodSerializer;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        //mapper.getSubtypeResolver().registerSubtypes(ThresholdMethod.class);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        //mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
       // generateModule(ThresholdMethod.class, new ThresholdMethodSerializer<>(getContext()), new ThresholdMethodDeserializer<>(getContext()));
//mapper.registerModule(new ThresholdMethodModule(getContext()));
        
    }

    private <T> void generateModule(Class<T> t, JsonSerializer<T> serializer, JsonDeserializer<T> deserializer) {
        SimpleModule m = new SimpleModule();
        m.addDeserializer(t, deserializer);
        m.addSerializer(t, serializer);
        m.registerSubtypes(DefaultThresholdMethod.class);
        
        mapper.registerModule(m);
        
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
            if(dest.getName().endsWith(".json") == false) {
                dest = new File(dest.getParentFile(),dest.getName()+".json");
            }
            mapper.writeValue(dest, workflow);
        } catch (IOException ex) {
            Logger.getLogger(WorkflowIOService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    
    
    private class MyTypeResolver extends TypeIdResolverBase {

        @Override
        public JavaType typeFromId(String id) {
            if (id.contains("net.imagej.threshold")) {
                return _baseType.forcedNarrowBy(ThresholdMethod.class);
            } else {
                try {
                    return TypeFactory.defaultInstance().constructType(ClassUtil.findClass(id));
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    throw new IllegalStateException("cannot find class '" + id + "'");
                }
            }
        }

        @Override
        public String idFromValue(Object value) {
            return value.getClass().getName();

        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {

            return suggestedType.getName();

        }

        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;

        }
    }
}
