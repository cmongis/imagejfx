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
package ijfx.core.project.query;

import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.project.AnnotationRule;
import ijfx.core.project.AnnotationRuleImpl;
import ijfx.core.project.ModificationRequest;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.ProjectModifierService;
import ijfx.core.project.command.AddMetaDataCommand;
import ijfx.core.project.command.Command;
import ijfx.core.project.command.EnableAnnotationRuleCommand;
import ijfx.core.project.command.Invoker;
import ijfx.core.project.command.ModifyMetaDataCommand;
import ijfx.core.project.command.RemoveMetaDataCommand;
import ijfx.core.project.command.addRuleCommand;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.imageDBService.command.AddTagCommand;
import ijfx.core.project.imageDBService.command.RemoveTagCommand;
import ijfx.ui.main.ImageJFX;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import mercury.core.MercuryTimer;
import org.scijava.Priority;
import org.scijava.event.EventService;
import org.scijava.event.SciJavaEvent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril Quinton
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class DefaultQueryService extends AbstractService implements QueryService {

    @Parameter
    private ProjectModifierService projectModifier;
    
     @Parameter
    EventService eventService;
    
   
    
    private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private String defaultMetaSetName = PlaneDB.MODIFIED_METADATASET_STRING;
    private final ResourceBundle rb;
    private int nbAutamaticallyAnnotatedPlane = 0;

    Logger logger = ImageJFX.getLogger();

    
    SelectorFactory selectorFactory = new DefaultSelectorFactory();

    public DefaultQueryService() {
        rb = ProjectManagerService.rb;
    }

    @Override
    public Selector getSelector(String query) {
        return selectorFactory.create(query);
    }

    @Override
    public Modifier getModifier(String modifyQuery) {
        return ModifierFactory.create(modifyQuery);
    }

    @Override
    public void modify(Project project, Modifier modifier) {
        List<PlaneDB> planes = project.getImages().filtered((PlaneDB t) -> t.selectedProperty().get());
        modify(project, planes, modifier);
    }

    @Override
    public void modify(Project project, List<PlaneDB> planes, Modifier modifier) {
        List<Command> cmds = new ArrayList<>();
        int nbModifiedPlane = 0;
        for (PlaneDB plane : planes) {
            List<Command> cmdList = modifyCmd(plane, modifier);
            if (!cmdList.isEmpty()) {
                nbModifiedPlane++;
                cmds.addAll(modifyCmd(plane, modifier));

            }
        }
        nbAutamaticallyAnnotatedPlane = nbModifiedPlane;
        executeModifyCmd(project, cmds);

    }

    @Override
    public boolean modify(Project project, PlaneDB plane, Modifier modifier) {
        List<Command> cmds;
        cmds = modifyCmd(plane, modifier);
        return executeModifyCmd(project, cmds);
    }

    private boolean executeModifyCmd(Project project, List<Command> cmds) {
        if (cmds.isEmpty()) {
            return false;
        } else {
            Invoker.executeCommandList(cmds, rb.getString("modify"), project.getInvoker());
            return true;
        }

    }

    @Override

    public AnnotationRule addAnnotationRule(Project project, Selector selector, Modifier modifier) {
        AnnotationRule rule = new AnnotationRuleImpl(selector, modifier);
        addAnnotationRule(project, rule, true);
        return rule;
    }

    @Override
    public void applyAnnotationRules(Project project) {
        applyAnnotationRules(project, project.getImages());
    }

    @Override
    public boolean applyAnnotationRules(Project project, PlaneDB plane) {
        List<Command> cmdList = applyRuleCmd(project, plane);
        if (cmdList.isEmpty()) {
            return false;
        } else {
            Invoker.executeCommandList(cmdList, rb.getString("applyRulesOnSinglePlane"), project.getInvoker());
            return true;
        }
    }

    @Override
    public void applyAnnotationRules(Project project, List<PlaneDB> planes) {
        List<Command> cmdList = applyAnnotationRulesCommand(project, planes);
        Invoker.executeCommandList(cmdList, rb.getString("applyRulesOnMultiplePlane"), project.getInvoker());

    }

    @Override
    public List<Command> applyAnnotationRulesCommand(Project project, List<PlaneDB> planes) {
        List<Command> cmdList = new ArrayList<>();
        int nbPlaneModified = 0;
        for (PlaneDB plane : planes) {
            List<Command> cmds = applyRuleCmd(project, plane);
            if (!cmds.isEmpty()) {
                nbPlaneModified++;
                cmdList.addAll(applyRuleCmd(project, plane));
            }
        }
        nbAutamaticallyAnnotatedPlane = nbPlaneModified;
        return cmdList;
    }

    private List<Command> applyRuleCmd(Project project, PlaneDB plane) {
        List<Command> cmdList = new ArrayList<>();
        for (AnnotationRule rule : project.getAnnotationRules().filtered((AnnotationRule t) -> t.unableProperty().get())) {
            if (query(plane, rule.getSelector())) {
                cmdList.addAll(modifyCmd(plane, rule.getModifier()));
            }
        }
        return cmdList;
    }

    
    
    
    private ScriptEngine getEngine() {
        return scriptEngineManager.getEngineByName("nashorn");
    }

    @Override
    public boolean query(PlaneDB plane, Selector selector, String metaDataSetName) {

        
        return selector.matches(plane,metaDataSetName);
        
        /*
        ScriptEngine scriptEngine = getEngine();
        boolean queryResult = false;
        boolean validSyntax = false;
        Object response = new Object();
        ReadOnlyMapProperty<String, MetaData> set = metaDataSetName != null ? plane.getMetaDataSetProperty(metaDataSetName) : plane.getMetaDataSet();
        QueryService.putVariableInScriptEngine(scriptEngine, plane.getTags().get(), set.get());
        try {
            logger().info("Executing the following script : "+selector.getParsedSelector());
            response = scriptEngine.eval(selector.getParsedSelector());
            if (response == null) {
                ImageJFX.getLogger();
                queryResult = false;
            } else {
                validSyntax = true;
                queryResult = (Boolean) response;
            }
        } catch (ScriptException ex) {
            ImageJFX.getLogger()
                    .log(Level.WARNING, "script exception");
            //ex.printStackTrace();
            validSyntax = false;
        } catch (ClassCastException ex) {
            ImageJFX.getLogger()
                    .log(Level.WARNING, "javascript response is not a boolean");
            validSyntax = false;

        }
        selector.setValidSyntax(validSyntax);

        return queryResult;*/
    }

    @Override
    public List<PlaneDB> query(Project project, Selector selector, boolean onSelection) {

        return query(project, selector, onSelection, defaultMetaSetName);
    }

    @Override
    public List<PlaneDB> query(Project project, Selector selector, boolean onSelection, String metaDataSetName) {
        ImageJFX.getLogger().info("Starting query");
        eventService.publish(new QueryStart());

        MercuryTimer timer = new MercuryTimer("query");
        List<PlaneDB> queryResultList = new ArrayList<>();
        List<PlaneDB> selectPlane = new ArrayList<>();
        List<PlaneDB> deselectPlane = new ArrayList<>();
        List<Command> cmds = new ArrayList<>();

        
                project.getImages().stream().parallel().forEach(plane -> {
                
                    
                
                if (!onSelection || onSelection && plane.selectedProperty().get()) {
                    boolean queryResult = query(plane, selector, metaDataSetName);
                   
                    if (queryResult) {
                        queryResultList.add(plane);
                    }
                    if (queryResult != plane.selectedProperty().get()) {
                        if (plane.selectedProperty().get()) {
                            deselectPlane.add(plane);
                        } else {
                            selectPlane.add(plane);
                        }
                    }

                }
                });
           

        projectModifier.selectPlane(project, deselectPlane, selectPlane);
        timer.elapsed("query");
        eventService.publish(new QueryStop());
        if(queryResultList.size() > 0) {
            
        }
        return queryResultList;

    }

    
    public Logger logger() {
        return ImageJFX.getLogger();
    }
    
    public class QueryStart extends SciJavaEvent {

    }

    public class QueryStop extends SciJavaEvent {

    }

    @Override
    public boolean query(PlaneDB plane, Selector selector) {
        return query(plane, selector, null);
    }

    private List<Command> modifyCmd(PlaneDB plane, Modifier modifier) {
        List<Command> cmds = new ArrayList<>();
        ModificationRequest request = modifier.getModificationRequest(plane);
        MetaDataSet metaDataSet = request.getAddMetaData();
        for (String key : metaDataSet.keySet()) {
            MetaData metaData = metaDataSet.get(key);
            if (plane.getMetaDataSet().containsKey(key)) {
                //check if the existing metadata is the same as the new one
                if (!plane.getMetaDataSet().get(key).equals(metaData)) {
                    cmds.add(new ModifyMetaDataCommand(plane, metaData));
                }
            } else {
                cmds.add(new AddMetaDataCommand(plane, metaData));
            }
        }
        metaDataSet = request.getRemoveMetaData();
        for (String key : metaDataSet.keySet()) {
            if (plane.getMetaDataSet().get(key) != null) {
                cmds.add(new RemoveMetaDataCommand(plane, metaDataSet.get(key)));
            }
        }
        for (String key: request.getRemoveKey()) {
            if (plane.getMetaDataSet().get(key) != null) {
                cmds.add(new RemoveMetaDataCommand(plane, key));
            }
        }
        List<String> tagList = request.getAddTag();
        for (String tag : tagList) {
            if (!plane.getTags().contains(tag)) {
                cmds.add(new AddTagCommand(plane, tag));
            }
        }
        tagList = request.getRemoveTag();
        for (String tag : tagList) {
            if (plane.getTags().contains(tag)) {
                cmds.add(new RemoveTagCommand(plane, tag));
            }
        }
        return cmds;
    }

    @Override
    public void enableAnnotationRule(Project project, AnnotationRule rule, boolean enable) {
        if (rule.unableProperty().get() != enable) {
            project.getInvoker().executeCommand(new EnableAnnotationRuleCommand(rule, enable));
        }
    }

    @Override
    public void addAnnotationRule(Project project, List<AnnotationRule> rules) {
        addAnnotationRules(project, rules, true);
    }

    @Override
    public void removeAnnotationRule(Project project, AnnotationRule rule) {
        addAnnotationRule(project, rule, false);
    }

    @Override
    public void removeAnnotationRule(Project project, List<AnnotationRule> rules) {
        addAnnotationRules(project, rules, false);
    }

    private void addAnnotationRule(Project project, AnnotationRule rule, boolean add) {
        project.getInvoker().executeCommand(getAddRuleCmd(project, rule, add));
    }

    private Command getAddRuleCmd(Project project, AnnotationRule rule, boolean add) {
        return new addRuleCommand(project, rule, add);
    }

    private void addAnnotationRules(Project project, List<AnnotationRule> rules, boolean add) {
        if (!rules.isEmpty()) {
            List<Command> cmdList = new ArrayList<>();
            for (AnnotationRule rule : rules) {
                cmdList.add(getAddRuleCmd(project, rule, add));
            }
            String cmdName = addRuleCommand.getCmdName(add, true);
            Invoker.executeCommandList(cmdList, cmdName, project.getInvoker());
        }
    }

    /*
    @Override
    public void modifyQueryObject(Project project, Selector query, String nonParsedNewVal) {
        project.getInvoker().executeCommand(new ModifyQueryCommand(query, nonParsedNewVal));
    }*/

    @Override
    public int getNbOfAnnotatedPlane() {
        return nbAutamaticallyAnnotatedPlane;
    }

    
    
    
}
