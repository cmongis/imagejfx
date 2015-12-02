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

import ijfx.core.metadata.MetaData;
import ijfx.core.project.command.AddMetaDataCommand;
import ijfx.core.project.command.Command;
import ijfx.core.project.command.Invoker;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.imageDBService.command.AddTagCommand;
import ijfx.core.project.imageDBService.command.RemoveTagCommand;
import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.scijava.Priority;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril Quinton
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class DefaultPlaneModifierService extends AbstractService implements PlaneDBModifierService {

   
    /*
    Map<Project,List<String>> lastAddedTags = new HashMap<>();
    
    @Parameter
    EventService eventService;
    */
    
    @Parameter
    ProjectManagerService projectService;

    @Override
    public void addTag(Project project, PlaneDB plane, String tag) {
        project.getInvoker().executeCommand(new AddTagCommand(plane, tag));
        
        //updateLastAddedTags(project, tag);
    }

    
    @Override
    public void addTag(Project project, PlaneDB plane, List<String> tags) {
        List<Command> cmdList = new ArrayList<>();
        for (String tag : tags) {
            cmdList.add(new AddTagCommand(plane, tag));
        }
        Invoker.executeCommandList(cmdList, ProjectManagerService.rb.getString("addMultipleTag"), project.getInvoker());
        //updateLastAddedTags(project, tags);
    }
    
    
     @Override
    public void addTag(Project project, List<PlaneDB> planeList, List<String> tags) {
        List<Command> cmdList  = new ArrayList<>();
        
        planeList.forEach(plane->{
            tags.forEach(tag->{
             cmdList.add(new AddTagCommand(plane, tag));
            });
        });
        
        Invoker.executeCommandList(cmdList, ProjectManagerService.rb.getString("addMultipleTag"), project.getInvoker());
        
    }

    @Override
    public void addTag(Project project, List<PlaneDB> planeList, String tag) {
        List<String> tagList = new ArrayList<>(planeList.size());
        tagList.add(tag);
        addTag(project,planeList,tagList);
        projectService.notifyProjectChange(project);
    }

    @Override
    public void removeTag(Project project, PlaneDB plane, String tag) {
        project.getInvoker().executeCommand(new RemoveTagCommand(plane, tag));
        projectService.notifyProjectChange(project);
    }

    @Override
    public void removeTag(Project project, PlaneDB plane, List<String> tags) {
        List<Command> cmdList = new ArrayList<>(tags.size());
        for (String tag : tags) {
            cmdList.add(new RemoveTagCommand(plane, tag));
        }
        Invoker.executeCommandList(cmdList, ProjectManagerService.rb.getString("removeMultipleTag"), project.getInvoker());
        
    }

    @Override
    public void replaceTag(Project project, PlaneDB plane, String oldTag, String newTag) {
        List<Command> cmdList = new ArrayList<>();
        cmdList.add(new RemoveTagCommand(plane, oldTag));
        cmdList.add(new AddTagCommand(plane, newTag));
        Invoker.executeCommandList(cmdList, ProjectManagerService.rb.getString("replaceTag"), project.getInvoker());
        projectService.notifyProjectChange(project);
    }
    
    

    @Override
    public void addMetaData(Project project, PlaneDB plane, MetaData metaData) {
        project.getInvoker().executeCommand(new AddMetaDataCommand(plane, metaData));
    }
    
    @Override
    public void addMetaData(Project project, List<PlaneDB> planeList, MetaData metaData) {
        
        List<Command> cmdList = new ArrayList<>(planeList.size());
        
        planeList.forEach(plane->cmdList.add(new AddMetaDataCommand(plane,metaData)));
        
        Invoker.executeCommandList(cmdList,String.format("Add metadata (%s = %s) to %d planes",metaData.getName(),metaData.getStringValue(),planeList.size()),project.getInvoker());
        
        projectService.notifyProjectChange(project);
    }

    
   

   
}
