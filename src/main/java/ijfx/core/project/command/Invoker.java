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
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;

/**
 *
 * @author Cyril Quinton
 */
public interface Invoker  {
    public static String IN_UNDO_REDO_STATUS_CHANGE = "undoRedoStatusChange";

    public static boolean executeCommandList(List<Command> cmdList, String name, Invoker invoker) {
        if (!cmdList.isEmpty()) {
           Command commandWrapped = getWrappedCommand(cmdList, name);
            invoker.executeCommand(commandWrapped);
            return true;
        }
        return false;
    }
    public static Command getWrappedCommand(List<Command> cmdList,String name) {
        return new CommandList(cmdList, name);
    }

    public static enum Operation {

        EXECUTE, UNDO, REDO
    };

    public void undo();

    public void redo();

    public void executeCommand(Command cmd);

    public ReadOnlyBooleanProperty undoDisableProperty();
    
    ReadOnlyBooleanProperty redoDisableProperty();

    public ReadOnlyStringProperty undoNameProperty();

    public ReadOnlyStringProperty redoNameProperty();

    public ReadOnlyFloatProperty progressProperty();

    public ReadOnlyStringProperty executedCommandProperty();
    
    public ReadOnlyProperty<Operation> onOperationProperty();

}
