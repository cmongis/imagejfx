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
package ijfx.ui;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public final class UiContexts {
    public static final String
            WEB_APPLICATION = "webapp"
            ,IMAGEJ = "imagej "
            ,PROJECT_MANAGER = "project-manager"
            ,IMAGE_OPEN = "image-open"
            ,DEBUG = "debug"
            ,EXPLORE = "explore"
            ,SEGMENT = "segment"
            ,VISUALIZE = "visualize"
            ,CORRECT = "correction"
            ,PROJECT_BATCH_PROCESSING = "project-batch-processing"
            ,FILE_BATCH_PROCESSING = "file-batch-processing"
            ,PROJECT_PLANE_SELECTED = "project-plane-selected"
            ,BATCH = "batch"
            ;
    
    
    public static String or(String... contextList) {
        return String.join(" ", contextList);
    }
    
    public static String list(String ... contextList) {
        return or(contextList);
    }
    
    
    public static String and(String... contextList) {
        return String.join("+",contextList);
    }
    
    
}
