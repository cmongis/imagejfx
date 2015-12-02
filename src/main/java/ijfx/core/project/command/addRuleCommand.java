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
public class addRuleCommand extends ProjectCommandImpl {
    public static String getCmdName(boolean add, boolean plural) {
         ResourceBundle rb = ProjectManagerService.rb;
        String cmdName = add? rb.getString("add") : rb.getString("remove");
        String end = plural? rb.getString("annotationRules"): rb.getString("annotationRule");
        cmdName = cmdName + " " + end;
        return cmdName;
    }
    private final AnnotationRule rule;
    private final boolean add;
    public addRuleCommand(Project project, AnnotationRule rule, boolean add) {
        super(project);
        this.rule = rule;
        this.add = add;
       
        name = getCmdName(add,false);
    }

    @Override
    public void execute() {
        execute(false);
    }
    
    @Override
    public void undo() {
        execute(true);
    }

    @Override
    public void redo() {
        execute();
    }
    
    private void execute(boolean inverted) {
        boolean adding = inverted? !add : add;
         if (adding) {
            project.addAnnotationRule(rule);
        }
        else {
            project.removeAnnotationRule(rule);
        }
    }
    
    
}
