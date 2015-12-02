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
package ijfx.core.listenableSystem;

import java.beans.PropertyChangeListener;

/**
 *
 * @author Cyril Quinton
 */
public interface Listenable {
    
    
    public static enum EventID {PM_CURRENT_PROJECT_CHANGE,PM_PROJECT_EMPTY_STATUS_CHANGE,
    PM_NEW_PROJECT_EVENT,PM_PROJECT_REMOVED_EVENT,P_IMAGE_LIST_CHANGE,P_RULE_CHANGE,P_HIERARCHY_CHANGE,P_QUERY_DATABASE_CHANGE, IN_UNDO_REDO_STATUS_CHANGE,TREE_NODE_CHILDREN_CHANGE,TREE_NODE_SELECTED_CHANGE,I_METADATASET_CHANGE,I_TAG_LIST_CHANGE,SELECTION_EVENT,I_FILE_NAME_CHANGE,DESTRUCT_EVENT
    };

    public void addPropertyChangeListener(String propertyName,PropertyChangeListener listener);
    
    public void addPropertyChangeListener(PropertyChangeListener listener);
    
    public void removePropertyChangeListener(PropertyChangeListener listener);
    
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

   

}
