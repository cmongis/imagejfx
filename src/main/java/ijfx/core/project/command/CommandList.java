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

import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author Cyril Quinton
 */
public class CommandList extends CommandAbstract {
    private final List<Command> cmds;
    public CommandList(List<Command> cmds) {
        this.cmds = cmds;
    }
    public CommandList(List<Command> cmds, String name) {
        this.cmds = cmds;
        this.name = name;
    }
    
    @Override
    public void execute() {
        iterateOverCmds((Command t) ->t.execute(), false);
    }

    @Override
    public void undo() {
        iterateOverCmds((Command t) -> t.undo(), true);
    }

    @Override
    public void redo() {
        iterateOverCmds((Command t) -> t.redo(), false);
    }
    private void iterateOverCmds(Consumer<Command> c, boolean reverse) {
        int size = cmds.size();
        int i = reverse? size -1: 0;
        while (i>=0 && i < size) {
            c.accept(cmds.get(i));
            i = reverse? i -1: i+1;
            progressProperty.set(reverse? (size - i)/size: i/size);
        }
    }
    @Override
    public String getName() {
       if (cmds.size() == 1){
           return cmds.get(0).getName();
       }
        return name;
    }

    @Override
    public boolean containsMultipleCommands() {
        return true;
    }

    
    
}
