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

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Reader;
import io.scif.bf.BioFormatsFormat;
import io.scif.img.DefaultImgFactoryHeuristic;
import io.scif.img.DefaultImgUtilityService;
import java.awt.Image;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.collections.FXCollections;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * this class is used to store an image with all it's linked informations
 *
 * @author Cyril MONGIS, 2015
 */
public class PlaneDBInMemory implements PlaneDB {

    public static HashMap<String, String> ACCEPTED_FORMAT_MAP = new HashMap<>();

    /**
     * A HashMap containing different metaDatasets.
     */
    private final HashMap<String, ReadOnlyMapWrapper<String, MetaData>> metaDataSets = new HashMap<>();

    /**
     * An object that contains all the needed information to find the linked
     * image in a file.
     */
    private ImageReference imageReference;
    /**
     * a list of tags that specify extra information about the image
     */
    private ReadOnlyListWrapper<String> tags = new ReadOnlyListWrapper<>(this, "tags", FXCollections.observableArrayList());

    private ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper(this, "selected", false);
    private ReadOnlyBooleanWrapper destructed = new ReadOnlyBooleanWrapper(this, "destructed", false);
    private final PropertyChangeSupport listenableService = new PropertyChangeSupport(this);
    private long planeIndex;

    public PlaneDBInMemory() {
        addMetaDataSet(new MetaDataSet(), METADATASET_STRING);
        addMetaDataSet(new MetaDataSet(), MODIFIED_METADATASET_STRING);

    }

    @Override
    public void select(boolean select) {
        selected.set(select);
    }

    @Override
    public void setDestructed() {
        destructed.set(true);
    }

    @Override
    public void addMetaData(MetaData metadata, String identifier) {
        if (metaDataSets.get(identifier) != null) {
            metaDataSets.get(identifier).put(metadata.getName(), metadata);
        }
    }

    @Override
    public void addMetaDataSet(MetaDataSet metaDataSet, String identifier) {
        if (metaDataSets.containsKey(identifier)) {
            /*ImageJFX.getLogger()
             .log(Level.WARNING, "overriding a metadataset");*/
        }
        metaDataSets.put(identifier, new ReadOnlyMapWrapper<>(this, identifier, FXCollections.observableMap(metaDataSet)));

    }

    @Override
    public void setImageReference(ImageReference ref) {
        this.imageReference = ref;
    }

    @Override
    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    @Override
    public ImageReference getImageReference() {
        return imageReference;
    }

    public static MetaDataSet jsonToMetaDataset(JSONObject jsonObj) throws JSONException {
        Iterator it = jsonObj.keys();
        MetaDataSet _metaDataSet = new MetaDataSet();
        while (it.hasNext()) {
            Object key = it.next();
            _metaDataSet.put(new GenericMetaData(key.toString(),
                    jsonObj.get(key.toString())));
        }
        return _metaDataSet;

    }

    public void setImageReference(ImageReferenceImpl ref) {
        imageReference = ref;
    }

    /**
     * make {@link Image#modifiedMetaDataSet} become equal to
     * {@link Image#metaDataSet}
     * <br>
     * serialize the metaDatasets list and deserialize it to make a deep copy
     */
    @Override
    public void resetModifiedMetaDataSet() {
        metaDataSets.get(MODIFIED_METADATASET_STRING).clear();
        HashMap<String, String> metaMap = MetaData.metaDataSetToMap(metaDataSets.get(METADATASET_STRING).get());
        for (String key : metaMap.keySet()) {
            addMetaData(new GenericMetaData(key, metaMap.get(key)), MODIFIED_METADATASET_STRING);

        }

    }

    /**
     * test if the image corresponds to the given selector<br>
     * The javaScript engine NasHorn is used to perform the query.
     *
     * @param selector a javaScript syntax string to be evaluate by a javaScript
     * engine.
     * @return a boolean that specify if the image object is selected by the
     * selector
     */
    @Override
    public String getName() {
        return imageReference.imageName() + " " + planeIndex;
    }

    @Override
    public File getFile() {
        return new File(imageReference.getPath());
    }

    @Override
    public boolean removeTag(String tag) {
        if (tags.get().contains(tag)) {
            tags.remove(tag);
            return true;
        }
        return false;
    }

    @Override
    public String getImageID() {
        return imageReference.getId();
    }

    @Override
    public String toString() {
        return imageReference != null ? imageReference.imageName() + getPlaneIndex() : super.toString();
    }

    @Override
    public HashMap<String, String> getAcceptedImageFormats() {
        return ACCEPTED_FORMAT_MAP;
    }

    @Override
    public void addMetaData(String key, Object value, String identifier) {
        addMetaData(new GenericMetaData(key, value), identifier);
    }

    @Override
    public void modifyMetaData(String key, Object newValue, String identifier) {
        ReadOnlyMapWrapper<String, MetaData> metaDataSet = metaDataSets.get(identifier);
        if (metaDataSet != null && metaDataSet.containsKey(key)) {
            MetaData newVal = new GenericMetaData(key, newValue);
            metaDataSet.put(key, newVal);
        }
    }

    @Override
    public long getPlaneIndex() {
        return planeIndex;
    }

    @Override
    public void setPlaneIndex(long planeIndex) {
        this.planeIndex = planeIndex;
    }

    @Override
    public void addMetaData(MetaData metaData) {
        addMetaData(metaData, MODIFIED_METADATASET_STRING);
    }

    @Override
    public void modifyMetaData(String key, Object newValue) {
        modifyMetaData(key, newValue, MODIFIED_METADATASET_STRING);
    }

    @Override
    public MetaData removeMetaData(String key, String identifier) {
        if (metaDataSetAndKeyExist(identifier, key)) {
            ReadOnlyMapWrapper<String, MetaData> metaDataSet = metaDataSets.get(identifier);
            return metaDataSet.remove(key);
        }
        return null;
    }

    private boolean metaDataSetAndKeyExist(String metadataSet, String key) {
        ReadOnlyMapWrapper<String, MetaData> metaDataSet = metaDataSets.get(metadataSet);
        return (metaDataSet != null && metaDataSet.containsKey(key));
    }

    @Override
    public MetaData removeMetaData(String key) {
        return removeMetaData(key, MODIFIED_METADATASET_STRING);
    }

    @Override
    public MetaData removeMetaData(MetaData metaData, String identifier) {
        String key = metaData.getName();
        if (metaDataSetAndKeyExist(identifier, key)) {
            ReadOnlyMapWrapper<String, MetaData> metaDataSet = metaDataSets.get(identifier);
            if (metaDataSet.get(key).getStringValue().equals(metaData.getStringValue())) {
                return metaDataSet.remove(key);
            }
        }
        return null;
    }

    @Override
    public MetaData removeMetaData(MetaData metaData) {
        return removeMetaData(metaData, MODIFIED_METADATASET_STRING);
    }

    @Override
    public ReadOnlyMapProperty<String, MetaData> getMetaDataSet() {
        return getMetaDataSetProperty(MODIFIED_METADATASET_STRING);
    }

    @Override
    public ReadOnlyMapProperty<String, MetaData> getMetaDataSetProperty(String identifier) {
        if (metaDataSets.containsKey(identifier)) {
            return metaDataSets.get(identifier).getReadOnlyProperty();
        }
        return null;
    }

    @Override
    public ReadOnlyListProperty<String> getTags() {
        return tags.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyBooleanProperty destructedProperty() {
        return destructed.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyBooleanProperty selectedProperty() {
        return selected.getReadOnlyProperty();
    }

    

}
