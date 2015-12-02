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

import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Cyril Quinton
 */
public class DefaultModificationRequest implements ModificationRequest {

    private final MetaDataSet addMetaData = new MetaDataSet();
    private final MetaDataSet removeMetaData = new MetaDataSet();
    private final List<String> removeKeyList = new ArrayList<>();
    private final List<String> addTag = new ArrayList<>();
    private final List<String> removeTag = new ArrayList<>();

    @Override
    public MetaDataSet getAddMetaData() {
        return addMetaData;
    }

    @Override
    public MetaDataSet getRemoveMetaData() {
        return removeMetaData;
    }

    @Override
    public List<String> getAddTag() {
        return addTag;
    }

    @Override
    public List<String> getRemoveTag() {
        return removeTag;
    }

    @Override
    public void addAddingMetaData(MetaData metaData) {
        if (metaData.getValue() != null) {
            addMetaData.put(metaData);
        } else {
            ImageJFX.getLogger().info("Metadata has no value :"+metaData.toString());
        }
    }

    @Override
    public void addRemovingMetaData(MetaData metaData) {
        removeMetaData.put(metaData);
    }

    @Override
    public void addAddingTag(String tag) {
        if (!existInList(tag, addTag)) {
            addTag.add(tag);
        }
    }

    @Override
    public void addRemovingTag(String tag) {
        if (!existInList(tag, removeTag)) {
            removeTag.add(tag);
        }
    }

    private boolean existInList(String key, List<String> list) {
        return list.contains(key);
    }

    @Override
    public boolean isEmpty() {
        return (addMetaData.isEmpty() && removeMetaData.isEmpty() && addTag.isEmpty() && removeTag.isEmpty() && removeKeyList.isEmpty());
    }

    @Override
    public List<String> getRemoveKey() {
        return removeKeyList;
    }

    @Override
    public void addRemovingKey(String key) {
        if (!removeKeyList.contains(key)) {
            removeKeyList.add(key);
        }
    }

}
