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

import ijfx.core.project.command.Command;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import net.imagej.ImageJService;

/**
 *
 * @author Cyril Quinton
 */
public interface ProjectManagerService  extends ImageJService{
    

    public static Locale currentLocale = Locale.ENGLISH;
    public static ResourceBundle rb = ResourceBundle.getBundle("ijfx.core.res.StringBundle", currentLocale);
    public String PM_CURRENT_PROJECT_CHANGE = "currentProjectChange";
    public String PM_NEW_PROJECT_EVENT = "newProjectEvent";
    public String PM_PROJECT_EMPTY_STATUS_CHANGE = "emptyStatusChange";
    public String PM_PROJECT_REMOVED_EVENT = "projectRemovedEvent";
    public static final String[] identifiersPriority = {PM_NEW_PROJECT_EVENT,PM_CURRENT_PROJECT_CHANGE , PM_PROJECT_EMPTY_STATUS_CHANGE,PM_PROJECT_REMOVED_EVENT};

    public static void setCommandName(List<Command> cmdList, String key) {
        int size = cmdList.size();
        if (size > 0) {
            cmdList.get(0).setName(rb.getString(key));
            if (size > 1) {
                cmdList.get(size - 1).setName(rb.getString(key));
            }
        }
    }
   

   

    void addProject(Project project);

    void removeProject(Project project);

    ReadOnlyObjectProperty<Project> currentProjectProperty();
    
    Project getCurrentProject();
    
    ReadOnlyListProperty<Project> getProjects();

    boolean hasProject();
    
    void setCurrentProject(Project project);

    public Set<String> getAllPossibleTag(Project project);

    public Set<String> getAllPossibleMetadataKeys(Project project);
   
    public void notifyProjectChange(Project project);
    
    
    

    

   
    

   

    



}
