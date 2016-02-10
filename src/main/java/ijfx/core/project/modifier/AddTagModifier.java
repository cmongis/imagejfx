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
import ijfx.core.project.command.CommandList;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.imageDBService.command.AddTagCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import mongis.utils.ConditionList;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = ModifierPlugin.class)
public class AddTagModifier implements ModifierPlugin {

    List<String> tags;
    
    public static final Pattern TAG_MODIFIER_PATTERN = Pattern.compile("add tags:(.*)");
    public static final String TAG_SEPARATOR = ",";
    public static final String PHRASE = "adds the tags %s";
    @Override
    public boolean configure(String query) {
        
        Matcher m = TAG_MODIFIER_PATTERN.matcher(query);
        
        if(m.matches()) {
            tags = Arrays.asList(m.group(1).split(TAG_SEPARATOR)).stream().map(s->s.trim()).collect(Collectors.toList());
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public Command getModifyingCommand(PlaneDB planeDB) {
        List<Command> commands = new ArrayList<>(tags.size());
        tags
                .forEach(tag->commands.add(new AddTagCommand(planeDB,tag)));
        return new CommandList(commands);
    }

    @Override
    public boolean wasApplied(PlaneDB planeDB) {
        
        
        return planeDB.getTags().containsAll(tags);
        
        
    }

    @Override
    public String phraseMe() {
       return String.format(PHRASE,String.join(TAG_SEPARATOR, tags));
    }
    
}
