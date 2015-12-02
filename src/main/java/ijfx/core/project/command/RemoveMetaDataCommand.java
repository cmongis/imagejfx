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

import ijfx.core.metadata.MetaData;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.imageDBService.command.ImageCommand;

/**
 *
 * @author Cyril Quinton
 */
public class RemoveMetaDataCommand extends ImageCommand {

    private MetaData metaDataBU;
    private final String key;
    private MetaData metaData;

    public RemoveMetaDataCommand(PlaneDB image, String key) {
        super(image);
        this.key = key;
        name = rb.getString("removeMetaData");
    }

    public RemoveMetaDataCommand(PlaneDB image, MetaData metaData) {
        this(image, metaData.getName());
        this.metaData = metaData;
    }

    @Override
    public void execute() {
        if (metaData != null) {
            metaDataBU = image.removeMetaData(metaData);
        } else {
            metaDataBU = image.removeMetaData(key);
        }
    }

    @Override
    public void undo() {
        if (metaDataBU != null) {
            image.addMetaData(metaDataBU);
        }
    }

    @Override
    public void redo() {
        execute();
    }

}
