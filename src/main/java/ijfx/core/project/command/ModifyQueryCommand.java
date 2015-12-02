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

import ijfx.core.project.query.Modifier;
import ijfx.core.project.query.QueryParser;
import ijfx.core.project.query.Selector;

/**
 *
 * @author Cyril Quinton
 */
public class ModifyQueryCommand extends CommandAbstract {
    private final Selector query;
    private final String newNonParsedVal;
    private String nonParsedValBU;
    public ModifyQueryCommand(Selector query, String newNonParsedVal){
        this.query = query;
        this.newNonParsedVal = newNonParsedVal;
        String cmdName = rb.getString("modify") + " ";
        String resourceKey;
        if (query instanceof  Selector) {
            resourceKey = "selector";
        } else if (query instanceof Modifier) {
            resourceKey = "modifier";
        } else {
            resourceKey = "query";
        }
        cmdName += rb.getString(resourceKey);
        this.name = cmdName;
    }
    @Override
    public void execute() {
        if (nonParsedValBU == null) {
        nonParsedValBU = query.getQueryString();
        }
        query.parse(newNonParsedVal);
    }

    @Override
    public void undo() {
        query.parse(nonParsedValBU);
    }

    @Override
    public void redo() {
        execute();
    }
    
}
