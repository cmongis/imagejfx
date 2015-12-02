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

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.imageDBService.command.ImageCommand;

/**
 *
 * @author Cyril Quinton
 */
public class AddMetaDataCommand extends ImageCommand {
    private final String key;
    private final MetaData metaData;
    public AddMetaDataCommand(PlaneDB image, String key, Object value) {
        super(image);
        this.key = key;
        metaData = new GenericMetaData(key, value);
         name = rb.getString("addMetaDataCmd");
    }
    public AddMetaDataCommand(PlaneDB image, MetaData metaData) {
        super(image);
        this.key = metaData.getName();
        this.metaData = metaData;
    }

    @Override
    public void execute() {
       image.addMetaData(metaData);
        
    }

    @Override
    public void undo() {
        image.removeMetaData(key);
        
    }

    @Override
    public void redo() {
        execute();
    }

   
    
}
