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

import ijfx.core.project.imageDBService.ImageReference;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Cyril Quinton
 */
public class DefaultFileReferenceStatus implements FileReferenceStatus{
    private List<ImageReference> wrongPathList = new ArrayList<>();
    private List<ImageReference> incorrectIdList = new ArrayList<>();
    private final Project project;
    public DefaultFileReferenceStatus(Project project) {
        this.project = project;
    }
    @Override
    public List<ImageReference> getWrongPathList() {
        return wrongPathList;
    }

    @Override
    public List<ImageReference> getIncorrectIDList() {
        return incorrectIdList;
    }

    @Override
    public boolean isOK() {
        return wrongPathList.isEmpty() && incorrectIdList.isEmpty();
    }

    @Override
    public Project getProject() {
        return project;
    }
    
}
