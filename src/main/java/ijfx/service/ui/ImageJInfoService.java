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
package ijfx.service.ui;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ijfx.ui.module.json.ModuleItemSerializer;
import ijfx.ui.module.json.ModuleSerializer;
import ijfx.ui.main.ImageJFX;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import mercury.core.AngularMethod;
import mercury.core.JSONUtils;
import net.imagej.ImageJService;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import netscape.javascript.JSObject;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class ImageJInfoService extends AbstractService implements ImageJService {

    @Parameter
    AppService appService;

    @Parameter
    ModuleService moduleService;

    Logger logger = ImageJFX.getLogger();

    @AngularMethod(sync = true, description = "Return the list of all widgets", inputDescription = "No input")
    public JSObject getModuleList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            logger.fine("Getting module list");
            SimpleModule simpleModule = new SimpleModule("ModuleSerializer");
            // simpleModule.addSerializer(ModuleItem<?>.class,new ModuleItemSerializer());
            simpleModule.addSerializer(ModuleInfo.class, new ModuleSerializer());
            simpleModule.addSerializer(ModuleItem.class, new ModuleItemSerializer());
            mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.registerModule(simpleModule);
           
            ArrayNode arrayNode = mapper.createArrayNode();
            
          
            
            //String json = mapper.writeValueAsString(moduleService.getModules());
            logger.fine("JSON done !");
            System.out.println(arrayNode.toString());
            return JSONUtils.parseToJSON(appService.getCurrentWebEngine(), mapper.writeValueAsString(moduleService
                    .getModules()
            .stream()
            .filter(m->m!=null)
            .collect(Collectors.toList())));
            //return JSONUtils.parseToJSON(appService.getCurrentWebEngine(), json);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        logger.warning("Returning null");
        return null;
    }

}
