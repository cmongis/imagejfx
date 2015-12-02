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
import ijfx.core.project.imageDBService.PlaneDB;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Cyril Quinton
 */
public class SelectImageCommand extends CommandAbstract {

    private final boolean select;
    private final List<PlaneDB> images;
    private final Project project;

    public SelectImageCommand(Project project,PlaneDB image, boolean select) {
        images = new ArrayList<>();
        images.add(image);
        this.select = select;
        this.project = project;
        setName();

    }

    public SelectImageCommand(Project project,List<PlaneDB> planes, boolean select) {
        this.select = select;
        images = planes;
        this.project = project;
        setName();
    }

    private void setName() {
        if (images.size() == 1) {
            name = select ? rb.getString("selectImage") : rb.getString("deselectImage");
        } else {
            name = select ? rb.getString("selectMultipleImages") : rb.getString("deselectMultipleImages");
        }
    }

    @Override
    public void execute() {
        exe(select);
    }
    private void exe(boolean selectImage) {
        for (PlaneDB plane: images) {
            plane.select(selectImage);
        }
        if (selectImage) {
            project.addSelectedPlane(images);
        } else {
            project.removeSelectedPlane(images);
        }
    }

    @Override
    public void undo() {
        exe(!select);
    }

    @Override
    public void redo() {
        execute();
    }

}
