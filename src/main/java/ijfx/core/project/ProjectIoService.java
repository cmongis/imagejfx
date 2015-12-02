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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;
import net.imagej.ImageJService;

/**
 *
 * @author Cyril Quinton
 */
public interface ProjectIoService extends FormatProvider, ImageJService{

    Project createProject();

     void load(File file) throws IOException, DataFormatException;

    void save(Project project, File file) throws IOException;
    
    List<AnnotationRule> loadRules(File file) throws IOException, DataFormatException;

}
