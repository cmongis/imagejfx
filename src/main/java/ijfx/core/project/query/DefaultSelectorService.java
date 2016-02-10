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
package ijfx.core.project.query;

import ijfx.ui.main.ImageJFX;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javafx.util.Callback;
import org.scijava.InstantiableException;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class DefaultSelectorService extends AbstractService implements SelectorService {

    List<Selector> selectorList;

    @Parameter
    PluginService pluginService;

    @Override
    public List<Selector> getSelectors() {
        //if (selectorList == null) {
       // List<Selector> pluginsOfClass = pluginService.createInstancesOfType(Selector.class);
        //pluginsOfClass.sort((p1,p2)->Double.compare(p1.getPriority(),p2.getPriority()));
        //System.out.println(pluginsOfClass.size());
        //selectorList = new ArrayList<>();
        
        selectorList = pluginService.createInstancesOfType(Selector.class);
        
        Callback<Selector,Double> getSelectorPriority = selector->{
            return selector.getClass().getAnnotation(Plugin.class).priority();
        };
        
       selectorList.sort((s1,s2)->{
           return Double.compare(getSelectorPriority.call(s1), getSelectorPriority.call(s2));
       });
        
        
       
        /*
        for(PluginInfo<SciJavaPlugin> infos : pluginsOfClass) {
            try {
                selectorList.add((Selector)infos.createInstance());
            } catch (InstantiableException ex) {
                ImageJFX.getLogger().log(Level.WARNING,"Error when creating Selector",ex);
            }
        }*/
        
            //selectorList = pluginService.;
            Collections.reverse(selectorList);
            
            System.out.println("Here is the list of selectors");
        selectorList.forEach(s->System.out.println(s.getClass().getSimpleName()+ " with priorty "+getSelectorPriority.call(s)));
       // }

        return selectorList;
    }

    @Override
    public Selector createSelector(Class<? extends Selector> clazz) {
        try {
            return (Selector) pluginService.getPlugin(clazz).createInstance();
        } catch (InstantiableException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public SelectorFactory getSelectorFactory() {
        return new SelectorServiceFactory();
    }

    private class SelectorServiceFactory implements SelectorFactory {

        @Override
        public Selector create(String query) {
            
            System.out.println("Creating selector");
            for(Selector selector : getSelectors()) {
                System.out.println("Testing "+selector.getClass().getSimpleName());
                if(selector.canParse(query))
                    return createSelector(selector.getClass());
                else {
                    System.out.println("result negative");
                }
            }
            return null;
            /*
            return getSelectors().stream().filter(selector->
              selector.canParse(query)
            ).findFirst().orElse(null);*/
        }

    }

}
