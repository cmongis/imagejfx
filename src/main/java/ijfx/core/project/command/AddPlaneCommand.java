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
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.imageDBService.PlaneDB;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;

/**
 *
 * @author Cyril Quinton
 */
public class AddPlaneCommand extends CommandAbstract {

    private final Project project;
    private final Collection<PlaneDB> planes = new ArrayList<>();
    private final boolean add;

    public AddPlaneCommand(Project project, PlaneDB plane, boolean add) {
        this(project, Arrays.asList(plane),add);
    }

    public AddPlaneCommand(Project project, Collection<PlaneDB> planes, boolean add) {
        this.project = project;
        this.planes.addAll(planes);
        this.add = add;
        String cmdName;
        ResourceBundle rb = ProjectManagerService.rb;
        if (add) {
            String key = planes.size() > 1? "addMultipleImages": "addImage";
            cmdName = rb.getString(key);
        } else {
            String key = planes.size() > 1 ? "removeMultipleImages" : "removeImageCmd";
            cmdName = rb.getString(key);
        }
        this.name = cmdName;
        
    }

    @Override
    public void execute() {
        exe(add);
    }

    private void exe(boolean addImage) {
        if (addImage) {
            project.addPlane(planes);
        } else {
            project.removeImage(planes);
        }
    }

    @Override
    public void undo() {
        exe(!add);
    }

    @Override
    public void redo() {
        execute();
    }

}
