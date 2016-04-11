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
package ijfx.core.project.imageDBService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static ijfx.core.project.imageDBService.PlaneDB.IMAGE_REFERENCE_STRING;
import static ijfx.core.project.imageDBService.PlaneDB.TAG_STRING;
import static ijfx.core.project.imageDBService.PlaneDBInMemory.jsonToMetaDataset;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.logging.Level;
import org.json.JSONException;
import org.json.JSONObject;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import static ijfx.core.project.imageDBService.PlaneDB.ORIGINAL_METADATASET;
import static ijfx.core.project.imageDBService.PlaneDB.MODIFIED_METADATASET;

/**
 *
 * @author Cyril Quinton
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class PlaneDBLoaderService extends AbstractService{
    
    
    
    
     /**
     * set attributes of the image object based on a string with the json
     * format.
     *
     * @param json A string with the json format containing a tree representing
     * the image object attribute. The String has to contain at least a
     * metadataset node and an image-reference node in order to initialize the
     * image object attributes
     * @throws JSONException when either the imageReference or the metadataset
     * cannot be read from the json string.
     * @throws IOException
     * @return the imageDBService object
     */
    public PlaneDB create(String json) throws IOException, JSONException {
        PlaneDB imageDB = new PlaneDBInMemory();
        getContext().inject(imageDB);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        rootNode = mapper.readTree(json);
        JsonNode metaDataSetNode = rootNode.get(ORIGINAL_METADATASET);
        JsonNode modifiedMetaDataSetNode = rootNode.get(MODIFIED_METADATASET);
        JsonNode imageReferenceNode = rootNode.get(IMAGE_REFERENCE_STRING);
        JsonNode tagsNode = rootNode.get(TAG_STRING);
        if (metaDataSetNode == null || modifiedMetaDataSetNode == null || imageReferenceNode == null) {
            String message = ("The loaded file do not contain the compulsory fields, project can't be loaded");
            throw new JSONException(message);
        }
        JSONObject jsonMeta = new JSONObject(metaDataSetNode.toString());
        
        imageDB.addMetaDataSet(jsonToMetaDataset(jsonMeta), ORIGINAL_METADATASET);
        try {
            JSONObject jsonModifiedMeta = new JSONObject(modifiedMetaDataSetNode.toString());
            imageDB.addMetaDataSet(jsonToMetaDataset(jsonModifiedMeta), MODIFIED_METADATASET);
        } catch (JSONException ex) {
            ImageJFX.getLogger().log(Level.WARNING,
                    "can't get the modified metadata from the json", ex);
            //if the modified metadataset can't be loaded, it is initialized
            //with the values of the original metadataset
            imageDB.resetModifiedMetaDataSet();
        }
        if (tagsNode != null) {
            if (tagsNode.isArray()) {
                for (JsonNode objNode : tagsNode) {
                    imageDB.addTag(objNode.asText());
                }
            }
        } else {
          ImageJFX.getLogger().info("Something happens... here but I need to figure it out");
        }

        JSONObject jsonImageRef = new JSONObject(imageReferenceNode.toString());
       
        String path = jsonImageRef.getString(ImageReference.PATH_STRING);
        String id = jsonImageRef.getString(ImageReference.ID_STRING);
        JsonNode planeIndexNode = rootNode.get(PlaneDB.PLANE_INDEX_STRING);
        long planeIndex = planeIndexNode.asLong();
        imageDB.setImageReference(new ImageReferenceImpl(id, path));
        imageDB.setPlaneIndex(planeIndex);
        return imageDB;
    }
}
