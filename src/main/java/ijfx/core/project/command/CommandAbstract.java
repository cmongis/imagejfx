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

import ijfx.core.project.ProjectManagerService;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;

/**
 *
 * @author Cyril Quinton
 */
public abstract class CommandAbstract implements Command {
    protected String name = "";
    protected ResourceBundle rb = ProjectManagerService.rb;
    protected SimpleBooleanProperty runningProperty = new SimpleBooleanProperty(false);
    protected FloatProperty progressProperty = new SimpleFloatProperty();
    protected boolean failed = false;
    @Override
    public String getName(){
        return name;
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public BooleanProperty runningProperty() {
        return runningProperty;
    }
     @Override
    public boolean containsMultipleCommands() {
        return false;
    }
    @Override
    public FloatProperty progressProperty() {
       return progressProperty;
    }
    @Override 
    public boolean failed() {
        return failed;
    }
}
