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

import ijfx.core.metadata.MetaDataSet;
import ijfx.core.project.command.Invoker;
import ijfx.core.project.imageDBService.PlaneDB;
import java.io.File;
import java.util.Collection;
import java.util.List;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;

/**
 *
 * @author Cyril Quinton
 */
public interface Project extends QueryCollection<PlaneDB>, Changeable {

    
    /**
     * value: {@value}
     */
    public static final String RULE_STRING = "rules";

    /**
     * value: {@value}
     */
    public static final String IMAGE_STRING = "image";

    /**
     * value: {@value}
     */
    public static final String HIERARCHY_STRING = "hierarchy";
    /**
     * value: {@value}
     */

    public static final String SETTINGS_STRING = "settings";
    
    public static final String MODIFY_DATABASE_STRING = "modifyDataBase";
    
    public static final String PLANE_ADDED_EVENT = "planeAddedEvent";
    
    public static final String PLANE_DESTRUCTED_EVENT = "planeDestructedEvent";

    public void addPlane(PlaneDB image);
    
    public void addPlane(Collection<PlaneDB> planes);

    public void removeImage(PlaneDB image);
    
    public void removeImage(Collection<PlaneDB> planes);

    public Invoker getInvoker();

    public ReadOnlyListProperty<PlaneDB> getSelection();
    
    public void addSelectedPlane(PlaneDB plane);
    
    public void addSelectedPlane(Collection<PlaneDB> planes);
    
    public void removeSelectedPlane(Collection<PlaneDB> planes);
    
    public void removeSelectedPlane(PlaneDB plane);

    public void addAnnotationRule(AnnotationRule rule);
    
    public void removeAnnotationRule(AnnotationRule rule);
    
    public ReadOnlyListProperty<AnnotationRule> getAnnotationRules();

    public ReadOnlyListProperty<PlaneDB> getImages();

    public ReadOnlyListProperty<String> getHierarchy();

    public void setHierarchy(List<String> hierachyKey);

    public ReadOnlyProperty<Boolean> hasChangedProperty();
    
    /**
     * Returns all the possible values for a defined key
     * @param key
     * @return all the possible values for a defined key, usually a set of string
     */
    public Collection<String> getValues(String key);

    public boolean containMetaDataKey(String key);

    public File getFile();

    public void setFile(File file);

    List<String> getMetaDataKeys();

    public MetaDataSet getSettings();
   
    public static Project NO_PROJECT = null;
    
    
}
