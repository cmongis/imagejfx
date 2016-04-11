/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.ui.explorer;

import ijfx.core.metadata.MetaDataSet;
import ijfx.core.project.AnnotationRule;
import ijfx.core.project.Project;
import ijfx.core.project.command.Invoker;
import ijfx.core.project.imageDBService.PlaneDB;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;

/**
 *
 * @author cyril
 */
public class FolderPlaneProject implements Project{

    @Override
    public void addPlane(PlaneDB image) {
    }

    @Override
    public void addPlane(Collection<PlaneDB> planes) {
    }

    @Override
    public void removeImage(PlaneDB image) {
    }

    @Override
    public void removeImage(Collection<PlaneDB> planes) {
    }

    @Override
    public Invoker getInvoker() {
    }

    @Override
    public ReadOnlyListProperty<PlaneDB> getSelection() {
    }

    @Override
    public void addSelectedPlane(PlaneDB plane) {
    }

    @Override
    public void addSelectedPlane(Collection<PlaneDB> planes) {
    }

    @Override
    public void removeSelectedPlane(Collection<PlaneDB> planes) {
    }

    @Override
    public void removeSelectedPlane(PlaneDB plane) {
    }

    @Override
    public void addAnnotationRule(AnnotationRule rule) {
    }

    @Override
    public void removeAnnotationRule(AnnotationRule rule) {
    }

    @Override
    public ReadOnlyListProperty<AnnotationRule> getAnnotationRules() {
    }

    @Override
    public ReadOnlyListProperty<PlaneDB> getImages() {
    }

    @Override
    public ReadOnlyListProperty<String> getHierarchy() {
    }

    @Override
    public void setHierarchy(List<String> hierachyKey) {
    }

    @Override
    public ReadOnlyProperty<Boolean> hasChangedProperty() {
    }

    @Override
    public Collection<String> getValues(String key) {
    }

    @Override
    public boolean containMetaDataKey(String key) {
    }

    @Override
    public File getFile() {
    }

    @Override
    public void setFile(File file) {
    }

    @Override
    public List<String> getMetaDataKeys() {
    }

    @Override
    public MetaDataSet getSettings() {
    }

    @Override
    public Property<String> groupByProperty() {
    }

    @Override
    public void query(Predicate<PlaneDB> p, Consumer<PlaneDB> c) {
    }

    @Override
    public boolean hasChanged() {
    }

    @Override
    public void setChanged(boolean changed) {
    }
    
}
