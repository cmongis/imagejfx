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

import ijfx.core.project.Changeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Cyril Quinton
 */
public class InvokerImpl implements Invoker {

    private final List<Command> tempCmdStack = new ArrayList<>();
    private final ListProperty<Command> cmdStack = new SimpleListProperty<>(this, "cmdStack", FXCollections.observableArrayList());
    private final ListProperty<Command> undoneCmdStack = new SimpleListProperty<>(this, "undenCmdStack", FXCollections.observableArrayList());
    private final SimpleFloatProperty progressProperty = new SimpleFloatProperty(this, "progressProperty");
    private final SimpleStringProperty executedCommandProperty = new SimpleStringProperty(this, "exectutedCommandProperty");
    private final ReadOnlyBooleanWrapper undoDisableProperty = new ReadOnlyBooleanWrapper(this, "undoDisable",true);
    private final ReadOnlyBooleanWrapper redoDisableProperty = new ReadOnlyBooleanWrapper(this, "redoDisable",true);
    private final ReadOnlyStringWrapper undoNameProperty = new ReadOnlyStringWrapper(this, "undoName");
    private final ReadOnlyStringWrapper redoNameProperty = new ReadOnlyStringWrapper(this, "redoName");
    private Changeable changeable;
    private boolean cmdStackFreed = false;

    
    
    ObjectProperty<Operation> operationProperty = new SimpleObjectProperty<>();
    
    public InvokerImpl() {
        listenToProperties();
    }

    public InvokerImpl(Changeable changeable) {
        this.changeable = changeable;
        listenToProperties();
    }

    private final void listenToProperties() {
        cmdStack.addListener(new ChangeListener<ObservableList<Command>>() {

            @Override
            public void changed(ObservableValue<? extends ObservableList<Command>> observable, ObservableList<Command> oldValue, ObservableList<Command> newValue) {
                undoDisableProperty.set(cmdStack.isEmpty());
                undoNameProperty.set(cmdStack.isEmpty() ? "" : cmdStack.get(cmdStack.size() - 1).getName());
                // every commands was undone, the object is back to its initial state. 
                if (changeable != null && !cmdStackFreed && cmdStack.isEmpty() == changeable.hasChanged()) {
                    changeable.setChanged(!cmdStack.isEmpty());

                }
            }
        });
        undoneCmdStack.addListener(new ChangeListener<ObservableList<Command>>() {

            @Override
            public void changed(ObservableValue<? extends ObservableList<Command>> observable, ObservableList<Command> oldValue, ObservableList<Command> newValue) {
                redoDisableProperty.set(undoneCmdStack.isEmpty());
                redoNameProperty.set(undoneCmdStack.isEmpty() ? "" : undoneCmdStack.get(undoneCmdStack.size() - 1).getName());
            }
        });
    }

    @Override
    public void executeCommand(Command cmd) {
        tempCmdStack.clear();
        tempCmdStack.add(cmd);
        
        executeCommand(Operation.EXECUTE);
    }

    @Override
    public void undo() {
        executeCommand(Operation.UNDO);
    }

    @Override
    public void redo() {
        executeCommand(Operation.REDO);
    }

    private void executeCommand(Operation operation) {
        List<Command> executedStack;
        List<Command> backUpStack;
        switch (operation) {
            case EXECUTE:
                executedStack = tempCmdStack;
                backUpStack = cmdStack;
                break;
            case UNDO:
                executedStack = cmdStack;
                backUpStack = undoneCmdStack;
                break;
            case REDO:
                executedStack = undoneCmdStack;
                backUpStack = cmdStack;
                break;
            default:
                executedStack = new Stack<>();
                backUpStack = new Stack<>();
        }
        Command cmd = executedStack.remove(executedStack.size() - 1);

        executedCommandProperty.set(cmd.getName());
        progressProperty.bind(cmd.progressProperty());

        switch (operation) {
            case EXECUTE:
                cmd.execute();
                break;
            case UNDO:
                cmd.undo();
                break;
            case REDO:
                cmd.redo();
                break;
        }
        
        //operationProperty.setValue(null);
        operationProperty.setValue(operation);
        
        // if the command fails it can't be redone or undone
        if (!cmd.failed()) {
            backUpStack.add(cmd);
            if (operation == Operation.EXECUTE) {
                undoneCmdStack.clear();
            }
        }
        
        changeable.setChanged(true);
        
        

    }

    @Override
    public FloatProperty progressProperty() {
        return progressProperty;
    }

    @Override
    public StringProperty executedCommandProperty() {
        return executedCommandProperty;
    }

    @Override
    public ReadOnlyBooleanProperty undoDisableProperty() {
        return undoDisableProperty.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyBooleanProperty redoDisableProperty() {
        return redoDisableProperty.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyStringProperty undoNameProperty() {
        return undoNameProperty.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyStringProperty redoNameProperty() {
        return redoNameProperty.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyProperty<Operation> onOperationProperty() {
        return operationProperty;
    }

}
