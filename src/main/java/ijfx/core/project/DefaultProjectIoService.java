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
package ijfx.core.project;

import ijfx.core.project.query.DefaultModifier;
import ijfx.core.project.query.Selector;
import ijfx.core.project.query.QueryParser;
import ijfx.core.project.query.Modifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import static ijfx.core.metadata.MetaData.metaDataSetToMap;
import static ijfx.core.project.Project.HIERARCHY_STRING;
import static ijfx.core.project.Project.IMAGE_STRING;
import static ijfx.core.project.Project.RULE_STRING;
import ijfx.core.project.imageDBService.PlaneDB;
import static ijfx.core.project.imageDBService.PlaneDB.IMAGE_REFERENCE_STRING;
import static ijfx.core.project.imageDBService.PlaneDB.METADATASET_STRING;
import static ijfx.core.project.imageDBService.PlaneDB.MODIFIED_METADATASET_STRING;
import static ijfx.core.project.imageDBService.PlaneDB.PLANE_INDEX_STRING;
import static ijfx.core.project.imageDBService.PlaneDB.TAG_STRING;
import ijfx.core.project.imageDBService.PlaneDBInMemory;
import ijfx.core.project.imageDBService.PlaneDBLoaderService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.main.ImageJFX;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.DataFormatException;
import javafx.concurrent.Task;
import mercury.core.MercuryTimer;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import static ijfx.core.project.Project.SETTINGS_STRING;
import ijfx.core.project.modifier.ModifierPlugin;
import ijfx.core.project.query.ModifierFactory;
import ijfx.core.project.query.QueryService;

/**
 *
 * @author Cyril Quinton
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class DefaultProjectIoService extends AbstractService implements ProjectIoService {

    private final String fileExtension = ".json";
    private final String format = "*.json";

    @Parameter
    private DefaultProjectManagerService projectService;
    @Parameter
    private PlaneDBLoaderService planeDBLoaderService;
    
    @Parameter
    LoadingScreenService loadingService;
    
    @Parameter
    QueryService queryService;

    @Override
    public Project createProject() {
        Project project = new DefaultProject();
        addProject(project);
        return project;
    }
    
    

    @Override
    public void load(File file) throws IOException, DataFormatException {
        
        MercuryTimer timer = new MercuryTimer("project load");
        
        Project project = new DefaultProject();
        JsonNode rootNode = getRootNode(file);
        JsonNode imagesNode = rootNode.get(IMAGE_STRING);
        JsonNode settingsNode = rootNode.get(SETTINGS_STRING);
        
        timer.elapsed("json creation");
        
        if (imagesNode == null) {
            throw (new DataFormatException("Can't find the list of image"
                    + " object in the json"));
        }
        if (imagesNode.isArray()) {
            for (JsonNode imageNode : imagesNode) {
                try {
                    
                    PlaneDB imageDB = planeDBLoaderService.create(imageNode.toString());
                    project.addPlane(imageDB);
                } catch (JSONException ex) {
                    throw (new DataFormatException(ex.getMessage()));
                }
            }
        }
        
        
        timer.elapsed("image node process");
        List<AnnotationRule> ruleList = ParseRules(rootNode);
        for (AnnotationRule rule: ruleList) {
             project.addAnnotationRule(rule);
        }
        
        timer.elapsed("rule process");
        //load hierachy
        JsonNode hierarchyNode = rootNode.get(HIERARCHY_STRING);
        if (hierarchyNode == null) {
            ImageJFX.getLogger().log(Level.WARNING,
                    "can't load a hierarchy");
            project.setHierarchy(new ArrayList<String>());
        } else if (hierarchyNode.isArray()) {
            //retrieve the list of metadata that define a hierarchy

            project.setHierarchy(parseHierarchy(hierarchyNode));
        }
        
        timer.elapsed("hierarchy process");
        
        project.setFile(file);
        
        timer.elapsed("project.setFile()");
        
        if(settingsNode != null) {
        settingsNode.fields().forEachRemaining((str)->{
            
           project.getSettings().put(new GenericMetaData(str.getKey(),str.getValue().asText()));
        });
        }
        System.out.println(project.getSettings());
        
        addProject(project);
        
        timer.elapsed("adding the project");
        
        
        
    }
    private JsonNode getRootNode(File projectFile) throws IOException {
        String json = DefaultProjectManagerService.readFile(projectFile);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        return rootNode;
    }
    
    private List<AnnotationRule> ParseRules(JsonNode rootNode) {
        List<AnnotationRule> ruleList = new ArrayList<>();
         JsonNode rulesNode = rootNode.get(RULE_STRING);
        if (rulesNode == null) {
            ImageJFX.getLogger().log(Level.WARNING,
                    "can't load the rules");
        } else if (rulesNode.isArray()) {
            for (JsonNode ruleNode : rulesNode) {
                AnnotationRule rule = parseAnnotationRule(ruleNode);
                if (rule == null) {
                    ImageJFX.getLogger().warning("No rule associated to the project.");
                } else {
                    ruleList.add(rule);
                }
            }

        }
        return ruleList;
    }

    private void addProject(Project project) {
        
        
        Task task = new Task() {
            public Object call() {
                projectService.addProject(project);
                return null;
            }
        };
        
      // LoadingScreen.getInstance().submitTask(task, false);
        ImageJFX.getThreadPool().submit(task);
        
    }

    private AnnotationRule parseAnnotationRule(JsonNode ruleNode) {
        String selectorString = ruleNode.get(Selector.SELECTOR_STRING).get(QueryParser.NON_PARSED_STRING).asText();
        String modifierString = ruleNode.get(Modifier.MODIFIER_STRING).get(QueryParser.NON_PARSED_STRING).asText();
        boolean enable = ruleNode.get(QueryParser.ENABLE).asBoolean();
        if (selectorString != null && modifierString != null) {
            Selector selector = queryService.getSelector(selectorString);
            ModifierPlugin modifier = queryService.getModifier(modifierString);
            AnnotationRule rule = new AnnotationRuleImpl(selector, modifier);
            rule.setUnable(enable);
            return rule;
        }
        return null;
    }

    private List<String> parseHierarchy(JsonNode hierarchyNode) {
        List<String> hierarchy = new ArrayList<>();
        for (JsonNode metaName : hierarchyNode) {
            hierarchy.add(metaName.textValue());
        }
        return hierarchy;
    }

    @Override
    public void save(Project project, File file) throws IOException {
        //transform each metadataset to a simple Map<String,String>
        //ArrayNode arrayNode = this.toJSONNode();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        //create an arrayNode with every image
        ArrayNode arrayNodeImages = new ArrayNode(JsonNodeFactory.instance);
        for (PlaneDB image : project.getImages()) {
            if (image.getClass() == PlaneDBInMemory.class) {
                PlaneDBInMemory imageInMemory = (PlaneDBInMemory) image;
                arrayNodeImages.add(planeToJsonNode(image));
            }

        }
        //add images to the root node
        root.putArray(Project.IMAGE_STRING).addAll(arrayNodeImages);

        //add rules
        ArrayNode arrayNodeRules = new ArrayNode(JsonNodeFactory.instance);
        List<AnnotationRule> rules = project.getAnnotationRules();
        for (AnnotationRule rule : rules) {
            arrayNodeRules.add(ruleToJsonNode(rule));
        }
        root.putArray(Project.RULE_STRING).addAll(arrayNodeRules);
        
        //add hierarchy
        List<String> hierarchyList = project.getHierarchy();
        ArrayNode arrayNodeHierarchy = mapper.valueToTree(hierarchyList);
        root.putArray(Project.HIERARCHY_STRING).addAll(arrayNodeHierarchy);

        
        // creates an object node with the settings
        ObjectNode settingsNode = root.putObject(SETTINGS_STRING);
        project.getSettings().forEach((str,metadata)->{
            settingsNode.put(str, metadata.getStringValue());
        });

        //save the project
        if (!FilenameUtils.getExtension(file.getName()).equals(fileExtension)) {
            String path = FilenameUtils.removeExtension(file.getAbsolutePath());
            path = path + fileExtension;
            file = new File(path);
        }
        mapper.writeValue(file, root);
        
        
        
        
        
        project.setChanged(false);
        project.setFile(file);
    }

    // save only the non parsed version of the selector and modifier
    private JsonNode ruleToJsonNode(AnnotationRule rule) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ObjectNode selectorNode = mapper.createObjectNode();
        Selector selector = rule.getSelector();
        selectorNode.put(QueryParser.NON_PARSED_STRING, selector.getQueryString());
        ObjectNode modifierNode = mapper.createObjectNode();
        ModifierPlugin modifier = rule.getModifier();
        modifierNode.put(QueryParser.NON_PARSED_STRING, modifier.toString());
        root.put(Selector.SELECTOR_STRING, selectorNode);
        root.put(Modifier.MODIFIER_STRING, modifierNode);
        root.put(QueryParser.ENABLE, rule.unableProperty().get());
        return root;
    }

      /**
     * Create a jsonNode from the object. <br>
     * This function is useful to save an image object in the json format within
     * a json tree.
     *
     * @return a JsonNode containing all attribute of the image object.  <br>
     * One can navigate through this node using Json Jackson.
     */
    private JsonNode planeToJsonNode(PlaneDB planeDB) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        HashMap<String, String> mapMetaData = MetaData.metaDataSetToMap(planeDB.getMetaDataSetProperty(METADATASET_STRING));
        HashMap<String, String> mapModifiedMetaData = metaDataSetToMap(planeDB.getMetaDataSetProperty(MODIFIED_METADATASET_STRING));
        JsonNode metaNode = mapper.valueToTree(mapMetaData);
        JsonNode modifiedMetaNode = mapper.valueToTree(mapModifiedMetaData);

        ObjectNode imageRefNode = mapper.createObjectNode();
        String path = planeDB.getImageReference().getPath();
        String id = planeDB.getImageReference().getId();
        imageRefNode.put(ijfx.core.project.imageDBService.ImageReference.PATH_STRING, path);
        imageRefNode.put(ijfx.core.project.imageDBService.ImageReference.ID_STRING, id);
        ArrayNode tagNode = mapper.valueToTree(planeDB.getTags());
        root.put(METADATASET_STRING, metaNode);
        root.put(MODIFIED_METADATASET_STRING, modifiedMetaNode);
        root.put(IMAGE_REFERENCE_STRING, imageRefNode);
        root.put(PLANE_INDEX_STRING, planeDB.getPlaneIndex());
        root.putArray(TAG_STRING).addAll(tagNode);
        return root;
    }

    @Override
    public List<String> getAcceptedFormat() {
        ArrayList<String> list = new ArrayList<>();
        list.add(format);
        return list;
    }

    @Override
    public List<AnnotationRule> loadRules(File file) throws IOException, DataFormatException {
        return ParseRules(getRootNode(file));
    }

}
