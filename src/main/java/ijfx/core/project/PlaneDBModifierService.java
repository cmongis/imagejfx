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
import ijfx.core.project.imageDBService.PlaneDB;
import java.util.List;
import net.imagej.ImageJService;

/**
 *
 * @author Cyril Quinton
 */
public interface PlaneDBModifierService extends ImageJService {

   
    void addTag(Project project, PlaneDB plane, String tag);

    void addTag(Project project, PlaneDB plane, List<String> tags);

    void addTag(Project project, List<PlaneDB> planeList, List<String> tags);
    
    void addTag(Project project, List<PlaneDB> planeList, String tag);
    
    void removeTag(Project project, PlaneDB plane, String tag);

    void removeTag(Project project, PlaneDB plane, List<String> tags);

    void replaceTag(Project project, PlaneDB plane, String oldTag, String newTag);

    
    //public List<String> getLastAddedTags(Project project);
    
    
    
    
    void addMetaData(Project project, PlaneDB plane, MetaData metaData);
    void addMetaData(Project project, List<PlaneDB> planeList, MetaData metaData);
    
}
