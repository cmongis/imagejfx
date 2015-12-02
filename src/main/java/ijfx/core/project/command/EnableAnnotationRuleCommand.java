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

import ijfx.core.project.AnnotationRule;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import java.util.ResourceBundle;

/**
 *
 * @author Cyril Quinton
 */
public class EnableAnnotationRuleCommand extends CommandAbstract {
    private final AnnotationRule rule;
    private final boolean enable; 
    public EnableAnnotationRuleCommand(AnnotationRule rule, boolean enable) {
        this.rule = rule;
        this.enable = enable;
        ResourceBundle rb = ProjectManagerService.rb;
        String cmdName = enable? rb.getString("enable") : rb.getString("disable");
        cmdName = cmdName + " " + rb.getString("annotationRule");
        name = cmdName;
        
        
    }

    @Override
    public void execute() {
        rule.setUnable(enable);
    }

    @Override
    public void undo() {
        rule.setUnable(!enable);
    }

    @Override
    public void redo() {
        execute();
    }
    
    
    
}
