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
package ijfx.ui.project_manager.project;

import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import java.util.HashMap;
import javafx.collections.ListChangeListener;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril Quinton
 */
@Plugin(type = Service.class, priority = Priority.FIRST_PRIORITY)
public class DefaultProjectViewModelService extends AbstractService implements ProjectViewModelService {

    @Parameter
    ProjectManagerService pm;
    
    
    
    private boolean initialized = false;

    private HashMap<Project, ProjectViewModel> map = new HashMap<>();

    @Override
    public ProjectViewModel getViewModel(Project project) {
        if (!initialized) {
            pm.getProjects().addListener(this::handleProjectListChange);
            initialized = true;
        }
        if (!map.containsKey(project)) {
            ProjectViewModel viewModel = new DefaultProjectViewModel();
            map.put(project, viewModel);
        }
        return map.get(project);
    }

    private void handleProjectListChange(ListChangeListener.Change<? extends Project> c) {
        while (c.next()) {
            if (c.wasRemoved()) {
                for (Project removedProject : c.getRemoved()) {
                    map.remove(removedProject);
                }
            }
        }
    }

}
