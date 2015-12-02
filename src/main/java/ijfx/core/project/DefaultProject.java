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
import ijfx.core.project.command.InvokerImpl;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.utils.FileName;
import ijfx.ui.main.ImageJFX;

import java.awt.Image;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.collections.FXCollections;

/*
 * 
 * To change this license header, choose License Headers in ImageSetProject Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 */
/**
 * @author Cyril MONGIS, 2015
 */
public class DefaultProject implements Project {

    private String currentMetaDataDB = PlaneDB.MODIFIED_METADATASET_STRING;
    
    private boolean changed;
    /**
     * A list of all image object.
     */
    private final ReadOnlyListWrapper<PlaneDB> images = 
            new ReadOnlyListWrapper<>(this, "images", FXCollections.observableArrayList());
    
    private final ReadOnlyListWrapper<PlaneDB> selectedPlanes = new ReadOnlyListWrapper<>(this, "selectedPlanes", FXCollections.observableArrayList());
    /**
     * A list of all rules
     */
    private final ReadOnlyListWrapper<AnnotationRule> rules =
            new ReadOnlyListWrapper<>(this, "annotationRules", FXCollections.observableArrayList());
    /**
     * a HierarchyImpl (defined by the user).
     */
    private ReadOnlyListWrapper<String> hierarchy =
            new ReadOnlyListWrapper<>(this, "hierarchy", FXCollections.observableArrayList());

    /**
     * * An identifier of the {@link MetaDataSet} object to modify in each
     * ImageDBService object when applying rules. It should be
     * {@link Image#MODIFIED_METADATASET_STRING}.
     */
    private String modifyDataBase = PlaneDB.MODIFIED_METADATASET_STRING;
    private final List<PlaneDB> selection = new ArrayList<>();
    private final InvokerImpl invoker;
    private File file;
    private final PropertyChangeSupport listenableService = new PropertyChangeSupport(this);
    

    public InvokerImpl getInvoker() {
        return invoker;
    }

    public DefaultProject() {
        invoker = new InvokerImpl(this);
        changed = false; 
        hierarchy.add(MetaData.FILE_NAME);
        hierarchy.add(MetaData.TIME);
        hierarchy.add(MetaData.CHANNEL);
        hierarchy.add(MetaData.Z_POSITION);
    }

    @Override
    public void addPlane(PlaneDB image) {
        images.add(image);
    }

    @Override
    public void removeImage(PlaneDB image) {
        images.remove(image);
        selectedPlanes.remove(image);
        image.setDestructed();
    }

  

    public PlaneDB getImage(int index) {
        return images.get(index);
    }

    @Override
    public void setHierarchy(List<String> hierarchyKey) {
        //TODO: control the new keys
        hierarchy.set(FXCollections.observableArrayList(hierarchyKey));
    }

   

  
    public void resetModifiedMetadatasets() {
        for (PlaneDB image : images) {
            image.resetModifiedMetaDataSet();
        }
    }
    
    @Override
    public void query(Predicate<PlaneDB> p, Consumer<PlaneDB> c) {
        QueryCollection.query(p, c, images);
    }

    private boolean check(Predicate<PlaneDB> p) {
        return images.stream().anyMatch(p);
    }

   

    @Override
    public ReadOnlyListProperty<PlaneDB> getSelection() {
        return selectedPlanes.getReadOnlyProperty();
    }

   

    @Override
    public List<String> getValues(String key) {
        Predicate<PlaneDB> p = (PlaneDB t) -> true;
        List<String> values = new ArrayList<>();
        Consumer<PlaneDB> getValues = (PlaneDB t) -> {
            ReadOnlyMapProperty<String,MetaData> metaDataSet = t.getMetaDataSetProperty(currentMetaDataDB);
            if (metaDataSet != null && metaDataSet.containsKey(key)) {
                String value = metaDataSet.get(key).getStringValue();
                if (!values.contains(value)) {
                    values.add(value);
                }

            }
        };
        query(p, getValues);
        return values;
    }

    @Override
    public boolean containMetaDataKey(String key) {
        return check((PlaneDB t) -> {
            ReadOnlyMapProperty<String,MetaData> metaDataSet = t.getMetaDataSetProperty(currentMetaDataDB);
            return metaDataSet != null && metaDataSet.containsKey(key);
        });
    }

   

    @Override
    public List<String> getMetaDataKeys() {
        List<String> keys = new ArrayList<>();
        Consumer<PlaneDB> consumer = (PlaneDB t) -> {
            ReadOnlyMapProperty<String,MetaData> metaDataSet = t.getMetaDataSetProperty(currentMetaDataDB);
            if (metaDataSet != null) {
                metaDataSet.keySet().stream().filter((key) -> (!keys.contains(key))).forEach((key) -> {
                    keys.add(key);
                });
            }
        };
        query((PlaneDB t) -> true, consumer);
        return keys;

    }

    @Override
    public String toString() {
        return file == null ? ProjectManagerService.rb.getString("untitled") : FileName.getName(file);
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    @Override
    public void setChanged(boolean changed) {
        this.changed = changed;
    }
     
    @Override
    public void addAnnotationRule(AnnotationRule rule) {
        rules.add(rule);
    }

    @Override
    public void removeAnnotationRule(AnnotationRule rule) {
        rules.remove(rule);
    }

    @Override
    public void addPlane(Collection<PlaneDB> planes) {
        images.addAll(planes);
                List<PlaneDB> selected = planes.stream().filter((PlaneDB t) -> t.selectedProperty().get()).collect(Collectors.toList());
                selectedPlanes.addAll(selected);
        
    }

    @Override
    public void removeImage(Collection<PlaneDB> planes) {
        boolean resp = images.removeAll(planes);
        selectedPlanes.removeAll(planes);
        if (!resp) {
            ImageJFX.getLogger().warning("Couldn't remove the planes");
        }
    }

    @Override
    public ReadOnlyListProperty<AnnotationRule> getAnnotationRules() {
        return rules.getReadOnlyProperty();
    }

    @Override
    public synchronized ReadOnlyListProperty<PlaneDB> getImages() {
        return images.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyListProperty<String> getHierarchy() {
        return hierarchy.getReadOnlyProperty();
    }

    @Override
    public void addSelectedPlane(PlaneDB plane) {
       
        selectedPlanes.add(plane);
        
    }
    

    @Override
    public void addSelectedPlane(Collection<PlaneDB> planes) {
        selectedPlanes.addAll(planes);
    }

    @Override
    public void removeSelectedPlane(Collection<PlaneDB> planes) {
        selectedPlanes.removeAll(planes);
        
    }

    @Override
    public void removeSelectedPlane(PlaneDB plane) {
        selectedPlanes.remove(plane);
    }

  

  

 

}
