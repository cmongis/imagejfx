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
package ijfx.core.project.command;

import ijfx.core.project.Project;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Cyril Quinton
 */
public class SetHierarchyCommand extends ProjectCommandImpl {
    private final List<String> hierarchy;
    private List<String> hierarchyBU;
    public SetHierarchyCommand(Project project,List<String> hierarchy) {
        super(project);
        this.hierarchy = hierarchy;
        name = rb.getString("setHierarchy");
    }

    @Override
    public void execute() {
        hierarchyBU = new ArrayList<>();
        for (String key: project.getHierarchy()) {
            hierarchyBU.add(key);
        }
        project.setHierarchy(hierarchy);
    }

    @Override
    public void undo() {
        project.setHierarchy(hierarchyBU);
    }

    @Override
    public void redo() {
        execute();
    }
    
}
