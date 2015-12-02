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

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.project.DefaultModificationRequest;
import ijfx.core.project.KeyWordExpressionPosition;
import ijfx.core.project.MetaDataPosition;
import ijfx.core.project.ModificationRequest;
import ijfx.core.project.TagPosition;
import ijfx.core.project.WordPosition;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author Cyril Quinton
 */
public class DefaultModifier implements Modifier {

    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private ModificationRequest modificationRequest;
    private String nonParsedString;
    private List<MetaDataPosition> metaDataPositions = new ArrayList<>();
    private List<TagPosition> tagPositions = new ArrayList<>();
    private final ReadOnlyBooleanWrapper validSyntax = new ReadOnlyBooleanWrapper(false);

    private static final Logger logger = ImageJFX.getLogger();
    
    public DefaultModifier() {
        nonParsedString = new String();
    }

    public DefaultModifier(String nonParsedString) {
        parse(nonParsedString);
    }

    @Override
    public void parse(String nonParsedString) {
        this.nonParsedString = nonParsedString;
        metaDataPositions = QueryParser.parseMetaData(nonParsedString);
        tagPositions = QueryParser.parseTag(nonParsedString);
        QueryParser.suppressFakePosition(metaDataPositions, tagPositions);
        createModificationRequest(null);
        updateValidSyntax();
    }

    @Override
    public ModificationRequest getModificationRequest(PlaneDB plane) {
        createModificationRequest(plane);
        return modificationRequest;
    }

    @Override
    public ModificationRequest getModificationRequest() {
        if (modificationRequest == null) {
            parse(nonParsedString);
        };
        return modificationRequest;
    }

    @Override
    public String getNonParsedString() {
        return nonParsedString;
    }

    private void createModificationRequest(PlaneDB plane) {
        modificationRequest = new DefaultModificationRequest();

        ScriptEngine engine = scriptEngineManager.getEngineByName("nashorn");
        if (plane != null) {
            QueryService.putVariableInScriptEngine(engine, plane);
        }

        for (MetaDataPosition metaPos : metaDataPositions) {
            String key = getWord(metaPos.startKeyPos(), metaPos.endKeyPos());
            String value = getWord(metaPos.startValuePos(), metaPos.endValuePos());
            Object val;
            try {
                val = getEvaluatedValue(value, engine);
            } catch (Exception ex) {
                logger.log(Level.SEVERE,"Failed to parse js in modifier",ex);
                val = value;
            }
            MetaData meta = new GenericMetaData(key, val);
            String keyWord = getKeyWord(metaPos);
            if (!keyWord.isEmpty() && keyWord.equals(NOT)) {
                if (value.equals(ALL)) {
                    modificationRequest.addRemovingKey(key);
                } else {
                    modificationRequest.addRemovingMetaData(meta);
                }
            } else {
                modificationRequest.addAddingMetaData(meta);

            }

        }
        for (TagPosition tagPos : tagPositions) {
            String tag = getWord(tagPos.startTagPos(), tagPos.endTagPos());
            String keyWord = getKeyWord(tagPos);
            if (!keyWord.isEmpty() && keyWord.equals(NOT)) {
                modificationRequest.addRemovingTag(tag);
            } else {
                modificationRequest.addAddingTag(tag);
            }
        }

    }

    private Object getEvaluatedValue(String jsSyntax, ScriptEngine engine) throws ScriptException, NullPointerException {
        String completSyntax = "var value =" + jsSyntax;
        engine.eval(jsSyntax);
        Object val = engine.get("value");
        if (val == null) {
            throw new NullPointerException();
        }
        return val;

    }

    private String getKeyWord(KeyWordExpressionPosition pos) {
        return getWord(pos.startKeyWordPos(), pos.endKeyWordPos());
    }

    private String getWord(int start, int end) {
        return nonParsedString.substring(start, end);
    }

    private String convertTag(String keyWord, String tag) {
        if (remove(keyWord)) {
            modificationRequest.addRemovingTag(tag);
        } else {
            modificationRequest.addAddingTag(tag);
        }
        return keyWord + tag;
    }

    private boolean remove(String keyWord) {
        return keyWord != null && keyWord.contains(QueryParser.REMOVE_KEY_WORD);
    }

    @Override
    public void setValidSyntax(boolean valid) {
        validSyntax.set(valid);
    }

    @Override
    public List<MetaDataPosition> getMetaDataPositions() {
        return metaDataPositions;
    }

    @Override
    public List<TagPosition> getTagPositions() {
        return tagPositions;
    }

    @Override
    public List<WordPosition> getWordPositions() {
        List<WordPosition> wordPos = new ArrayList<>();
        wordPos.addAll(WordPosition.transformMetaPosToWordList(metaDataPositions));
        wordPos.addAll(WordPosition.transformTagToWordList(tagPositions));
        Collections.sort(wordPos);
        return wordPos;
    }

    @Override
    public ReadOnlyBooleanProperty validSyntaxProperty() {
        return validSyntax.getReadOnlyProperty();
    }

    // if no modification request were parsed from the string, the syntax is
    // invalid. else, the syntax is valid.
    private void updateValidSyntax() {
        validSyntax.setValue(!modificationRequest.isEmpty());
    }

    @Override
    public String getAddTagSyntax(String tag) {
        return "\"" + tag + "\"";
    }

    @Override
    public String getAddMetaDataSyntax(MetaData metaData) {
        return getAddMetaDataSyntax(metaData.getName(), metaData.getStringValue());
    }

    @Override
    public String getAddMetaDataSyntax(String key, String value) {
        return "\"" + key + "\"=\"" + value + "\"";
    }

    @Override
    public String getSeparator() {
        return " ";
    }

}
