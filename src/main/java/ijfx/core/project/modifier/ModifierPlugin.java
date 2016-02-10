/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.core.project.modifier;

import ijfx.core.project.command.Command;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.query.Phrasable;
import org.scijava.plugin.SciJavaPlugin;

/**
 *
 * @author cyril
 */
public interface ModifierPlugin extends SciJavaPlugin, Phrasable{

    /**
     * configure the modifier from a string query. This
     * command is useful for saving and sharing rules via text files.
     * @param query string that represent the modification
     * @return true if the string parsing and configuration was successful
     */
    public boolean configure(String query);
    
    /**
     * Returns a command that can be used by the UI and undone by the user if wanted
     * @param planeDB
     * @return a Command representing the modification on a particular plane
     */
    public Command getModifyingCommand(PlaneDB planeDB);
    
    /**
     * Returns true if the application of the @ModifierPlugin will change something on the plane
     * @param planeDB to modify
     * @return true is the plugin was applied on the plane
     */
    public boolean wasApplied(PlaneDB planeDB);
    
    /**
     * Execute the modification on a PlaneDB object.
     * Note that the modification becomes irreversible.
     * @param planeDB
     */
    default public void execute(PlaneDB planeDB) {
        getModifyingCommand(planeDB).execute();
    }
}
