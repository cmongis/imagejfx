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
package ijfx.ui.project_manager;

import ijfx.core.project.Project;
import ijfx.core.project.DefaultProjectManagerService;
import ijfx.core.project.hierarchy.MetaDataHierarchy;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.project_manager.hierarchy.creator.MetaDataHierarchyImpl;
import ijfx.service.uicontext.UiContextService;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.collections.ListChangeListener;
import net.imagej.ImageJService;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril Quinton
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class FolderOrganisationService extends AbstractService implements ImageJService {
    public enum PlaneClass{ALL_PLANE,SELECTED_PLANE};
    private final HashMap<List, MetaDataHierarchy> map = new HashMap<>();
    private PropertyChangeListener pmDestructEventListener;
    private boolean initialized = false;
    @Parameter
    private DefaultProjectManagerService projectManager;
    @Parameter
    private UiContextService contextService;

    public MetaDataHierarchy getFolderCreator(Project project,PlaneClass pClass ) {
        List ls = Arrays.asList(project,pClass);
        if (!initialized) {
            projectManager.getProjects().addListener(this::handleProjectListChange);
            initialized = true;
        }
        if (!map.containsKey(project)) {
            
            ReadOnlyListProperty<String> hierarchy = project.getHierarchy();
            ReadOnlyListProperty<PlaneDB> planes;
            switch (pClass) {
                case ALL_PLANE : planes = project.getImages(); break;
                case SELECTED_PLANE : planes = project.getSelection(); break;
                    default:planes = project.getImages();
            }
            map.put(ls, new MetaDataHierarchyImpl(project,hierarchy, planes));
        }
        return map.get(ls);
    }

    private void handleProjectListChange(ListChangeListener.Change<? extends Project> c) {
        while (c.next()) {
            if (c.wasRemoved()) {
                for (Project removedProject : c.getRemoved()) {
                    MetaDataHierarchy metaDataHierarchy = map.get(removedProject);
                    if (metaDataHierarchy != null) {
                        metaDataHierarchy.stopListening();
                        map.remove(removedProject);
                    }
                }
            }
        }
    }
}
