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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import ijfx.plugins.LongInterval;
import ijfx.service.workflow.json.FileDeserializer;
import ijfx.service.workflow.json.FileSerializer;
import ijfx.service.workflow.json.JsonFieldName;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.imagej.ImageJ;
import net.imagej.threshold.ThresholdMethod;
import net.imglib2.display.ColorTable8;
import org.apache.commons.lang3.ArrayUtils;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandService;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class DefaultWorkflowStep implements WorkflowStep {

    protected String id;

    @JsonIgnore
    protected Module module;

    @JsonIgnore
    protected String className;

    @JsonIgnore
    protected Map<String, Object> parameters = new HashMap<>();

    @Parameter
    protected CommandService commandService;

    @Parameter
    protected ModuleService moduleSerivce;

    public static Class[] SAVED_TYPES = new Class[]{
        double.class,
        int.class,
        short.class,
        float.class,
        boolean.class,
        Boolean.class,
        Float.class,
        Double.class,
        Integer.class,
        String.class,
        File.class,
        ColorTable8.class,
        LongInterval.class, ThresholdMethod.class
    };

    public DefaultWorkflowStep() {

    }

    public DefaultWorkflowStep(Module module) {
        setModule(module);
        setParameters(module.getInputs());
    }

    public DefaultWorkflowStep(String className) {
        setClassName(className);
    }

    public DefaultWorkflowStep(String className, ImageJ ij) {
        this(className);
        createModule(ij);
    }

    public DefaultWorkflowStep(String className, String id, ImageJ ij) {
        this(className, ij);
        setId(id);
    }

    @Override
    public String getId() {
        if (id == null && module != null) {
            id = module.getInfo().getDelegateClassName();
        }
        return id;
    }

    public DefaultWorkflowStep createModule(ImageJ ij) {
        return createModule(ij.command(), ij.module());
    }

    public DefaultWorkflowStep createModule(CommandService commandService, ModuleService moduleService) {
        CommandInfo infos = commandService.getCommand(getClassName());
        if (infos != null) {
            module = moduleService.createModule(infos);
        }
        return this;
    }

    @Override
    @JsonIgnore
    public Module getModule() {

        if (module == null && commandService != null && moduleSerivce != null) {
            createModule(commandService, moduleSerivce);
        }

        return module;
    }

    @Override
    @JsonGetter("parameters")
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /*
    @Override
    public void setParameterType(String parameter, StepParameterType type) {
        parameterType.put(parameter, type);
    }

    @Override
    public StepParameterType getParameterType(String parameter) {
        if (parameterType.containsKey(parameter) == false) {
            parameterType.put(parameter, WorkflowStep.DEFAULT_PARAMETER_TYPE);
        }
        return parameterType.get(parameter);
    }*/
    @Override
    @JsonSetter("id")
    public void setId(String id) {
        this.id = id;
    }

    public void setModule(Module module) {
        //this.module = module;
        className = module.getInfo().getDelegateClassName();
    }

    @JsonSetter("parameters")
    public void setParameters(Map<String, Object> parameters) {

        parameters.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            if (ArrayUtils.contains(SAVED_TYPES, value.getClass())) {

                if (value instanceof String && value.toString().startsWith(FileSerializer.FILE_PREFIX)) {
                    this.parameters.put(key, FileDeserializer.deserialize(value.toString()));
                } else {
                    this.parameters.put(key, value);
                }

            }
        });
        //this.parameters = parameters;
    }

    /*
    @Override
    public Map<String, StepParameterType> getParameterTypes() {
        return parameterType;
    }*/
    @JsonGetter(value = JsonFieldName.CLASS)
    public String getClassName() {
        if (className == null && module != null) {
            className = module.getInfo().getDelegateClassName();
        }
        return className;
    }

    @JsonSetter(value = JsonFieldName.CLASS)
    public void setClassName(String className) {
        this.className = className;
    }

    @JsonSetter(value = "parameters")
    public void setParameter(String alpha, Object object) {
        getParameters().put(alpha, object);

    }

    @JsonGetter(value = "parameters")
    public Object getParameter(String alpha) {
        return getParameters().get(alpha);
    }

}
