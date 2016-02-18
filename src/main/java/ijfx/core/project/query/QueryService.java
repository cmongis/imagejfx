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
import ijfx.core.project.AnnotationRule;
import ijfx.core.project.Project;
import ijfx.core.project.command.Command;
import ijfx.core.project.imageDBService.PlaneDB;
import static ijfx.core.project.imageDBService.PlaneDB.METADATASET_STRING;
import static ijfx.core.project.imageDBService.PlaneDB.TAG_STRING;
import ijfx.core.project.modifier.ModifierPlugin;
import java.util.List;
import java.util.Map;
import javax.script.ScriptEngine;
import net.imagej.ImageJService;

/**
 *
 * @author Cyril Quinton
 */


public interface QueryService extends ImageJService{

    public static void putVariableInScriptEngine(ScriptEngine scriptEngine, PlaneDB plane) {
        putVariableInScriptEngine(scriptEngine, plane.getTags().get(), plane.getMetaDataSet().get());
    }

    public static void putVariableInScriptEngine(ScriptEngine engine, PlaneDB plane, String metaDataSetName) {
        putVariableInScriptEngine(engine, plane.getTags().get(), plane.getMetaDataSetProperty(metaDataSetName).get());

    }

    public static void putVariableInScriptEngine(ScriptEngine scriptEngine, List<String> tags, Map<String, MetaData> set) {
        scriptEngine.put(METADATASET_STRING, set);
        scriptEngine.put(TAG_STRING, tags);
    }

    Selector getSelector(String query);

    List<PlaneDB> query(Project project, Selector selector, boolean onSelection);

    List<PlaneDB> query(Project project, Selector selector, boolean onSelection, String metaDataSetName);

    boolean query(PlaneDB plane, Selector selector);

    boolean query(PlaneDB plane, Selector selector, String metaDataSetName);

    ModifierPlugin getModifier(String modifyQuery);

    void modify(Project project, Modifier modifier);

    void modify(Project project, List<PlaneDB> planes, Modifier modifier);

    boolean modify(Project project, PlaneDB plane, Modifier modifier);

    AnnotationRule addAnnotationRule(Project project, Selector selector, ModifierPlugin modifier);

    void removeAnnotationRule(Project project, AnnotationRule rule);

    void addAnnotationRule(Project project, List<AnnotationRule> rules);

    void removeAnnotationRule(Project project, List<AnnotationRule> rules);

    //void modifyQueryObject(Project project, Selector query, String nonParsedNewVal);

    /**
     *
     * @param project
     */
    void applyAnnotationRules(Project project);

    /**
     *
     * @param project
     * @param plane
     * @return true if the plane was modified, false otherwise. 0 means that
     * either no planes were selected by the selectors in the rules or that no
     * rules are enable or that the modification requests are either empty or
     * request something irrelevant (ie adding a metaData when it already
     * exists). Typically, when you run this function 2 times in a row, the
     * second time should return false because all modifications were performed
     * during the first time.
     */
    boolean applyAnnotationRules(Project project, PlaneDB plane);

    /**
     *
     * @param project The project that contains the planes. The annotation rules
     * of the project are used to modify the plane data
     * @param planes a list of <code>PlaneDB</code> to be modified by the rules
     */
    void applyAnnotationRules(Project project, List<PlaneDB> planes);

    List<Command> applyAnnotationRulesCommand(Project project, List<PlaneDB> planes);

    void enableAnnotationRule(Project project, AnnotationRule rule, boolean enable);

    /**
     * Gives the number of modified plane by the last call any modifying
     * function
     *
     * @return the number of modified planes.
     */
    int getNbOfAnnotatedPlane();
}
