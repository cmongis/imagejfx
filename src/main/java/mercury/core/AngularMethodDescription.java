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
package mercury.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Method;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class AngularMethodDescription {
    String name;
    private AngularMethod annotation;

    public AngularMethodDescription(Method m) {
        name = m.getName();
        annotation = m.getAnnotation(AngularMethod.class);
        if (annotation != null) {
        }
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("description")
    public String getDescription() {
        return annotation.description();
    }
    
    @JsonProperty("inputExample")
    public String getInputExample() {
        return annotation.inputExample().replaceAll("'", "\\\"");
    }
    


    @JsonProperty("outputExample")
    public String getExample() {
        return annotation.outputExample();
    }
    
    @JsonProperty("inputDescription")
    public String getInput() {
        return annotation.inputDescription();
    }
    
    @JsonProperty("outputDescription")
    public String getOutputDescription() {
        return annotation.outputDescription();
    }
    
    @JsonProperty("sync")
    public boolean isSync() {
        return annotation.sync();
    }
    
}
