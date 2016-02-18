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
package ijfx.core.project;

import ijfx.core.project.modifier.ModifierPlugin;
import ijfx.core.project.query.Selector;
import ijfx.core.project.query.Modifier;
import java.util.HashMap;
import org.scijava.event.EventService;
import org.scijava.event.SciJavaEvent;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class QueryMemoryService {

    
    @Parameter
    EventService eventService;
    
    private class SelectorMemory extends HashMap<Project, Selector> {
    }

    private class ModifierMemory extends HashMap<Project, ModifierPlugin> {
    }

    private SelectorMemory selectorMemory = new SelectorMemory();
    private ModifierMemory modifierMemory = new ModifierMemory();

    public void updateLastSelector(Project project, Selector selector) {
        selectorMemory.put(project, selector);
    }

    public void updateLastModifier(Project project, ModifierPlugin modifier) {
        modifierMemory.put(project, modifier);
        if(getLastSelector(project) != null) {
             eventService.publishLater(new PossibleAnnotationRuleEvent(new AnnotationRuleImpl(getLastSelector(project), modifier)));
        }
    }
    
    public ModifierPlugin getLastModifier(Project project) {
        return modifierMemory.get(project);
    }

    public Selector getLastSelector(Project project) {
        return selectorMemory.get(project);
    }

    public class PossibleAnnotationRuleEvent extends SciJavaEvent {

        AnnotationRule rule;

        public PossibleAnnotationRuleEvent(AnnotationRule rule) {
            this.rule = rule;
        }

        public AnnotationRule getRule() {
            return rule;
        }

    }

}
