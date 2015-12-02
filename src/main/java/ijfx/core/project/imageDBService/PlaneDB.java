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

import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.project.ImageFormatProvider;
import ijfx.core.listenableSystem.Listenable;
import java.io.File;
import java.util.List;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import net.imglib2.img.Img;

/**
 *
 * @author Cyril Quinton
 */
public interface PlaneDB extends Selectable, ImageFormatProvider {

   
    /**
     * value: {@value}
     */
    public static final String METADATASET_STRING = "metaDataSet";
    /**
     * value: {@value}
     */
    public static final String MODIFIED_METADATASET_STRING = "ModifiedMetaDataSet";
    /**
     * value: {@value}
     */
    public static final String IMAGE_REFERENCE_STRING = "ImageReference";
    /**
     * value: {@value}
     */
    public static final String TAG_STRING = "tags";

    public static String FILE_NAME_STRING = "FILE_NAME";

    public static String PLANE_INDEX_STRING = "planeIndex";


    public static String METADATA_ADDED = "metaDataAdded";

    public static String METADATA_REMOVED = "metaDataRemoved";

    public static String METADATA_MODIFIED = "metaDataModified";

    public static String IMAGE_REFERENCE_CHANGE = "imageReferenceChange";

    public static String ADD_TAG_EVENT = "addTagEvent";

    public static String REMOVE_TAG_EVENT = "removeTagEvent";

    public String getName();

    /**
     *
     * @return a correct file containing the image.
     */
    public File getFile();

    public ReadOnlyMapProperty<String, MetaData> getMetaDataSetProperty(String identifier);

    public ReadOnlyMapProperty<String, MetaData> getMetaDataSet();

    public ReadOnlyListProperty<String> getTags();

    public void addMetaData(MetaData metaData, String identifier);

    public void addMetaData(MetaData metaData);

    public void addMetaDataSet(MetaDataSet metaDataSet, String identifier);

    void addMetaData(String key, Object value, String identifier);

    void modifyMetaData(String key, Object newValue, String identifier);

    void modifyMetaData(String key, Object newValue);

    MetaData removeMetaData(String key, String identifier);

    MetaData removeMetaData(String key);
    
     MetaData removeMetaData(MetaData metaData, String identifier);
    
    MetaData removeMetaData(MetaData metaData);

    public void setImageReference(ImageReference ref);

    public void addTag(String tag);

    public boolean removeTag(String tag);

    public String getImageID();

    public long getPlaneIndex();

    public void setPlaneIndex(long planeIndex);

    public ImageReference getImageReference();

    public void resetModifiedMetaDataSet();

    public void setDestructed();
    
    public default Img getImg() {
        return null;
    }
    
    public ReadOnlyBooleanProperty destructedProperty();

}
