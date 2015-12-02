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
import ijfx.core.project.MetaDataPosition;
import ijfx.core.project.ReplaceStringPosition;
import ijfx.core.project.ReplaceStringPositionImpl;
import ijfx.core.project.StringPosition;
import ijfx.core.project.TagPosition;
import ijfx.core.project.WordPosition;
import ijfx.core.project.imageDBService.PlaneDB;
import static ijfx.core.project.imageDBService.PlaneDB.METADATASET_STRING;
import static ijfx.core.project.imageDBService.PlaneDB.TAG_STRING;
import ijfx.core.project.imageDBService.PlaneDBInMemory;
import ijfx.ui.main.ImageJFX;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyMapProperty;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This class performs query parsing. it generates javaScript styled queries
 * related to a {@link Project} object.
 *
 * @author Cyril Quinton
 */
public class DefaultSelector implements Selector, QueryParser {

    private List<MetaDataPosition> metaDataPositions = new ArrayList<>();
    private List<TagPosition> tagPositions = new ArrayList<>();
    private List<StringPosition> logicalOperatorPositions = new ArrayList<>();
    private String nonParsedString;
    private String parsedString;
    private ReadOnlyBooleanWrapper validSyntax = new ReadOnlyBooleanWrapper(false);
    public static String CHECK_NULL = "!= null";
    public static String AND = "&&";
    public static String OR = "||";
    public static String GET_META_VALUE = ".getValue()";
    public static String GET_STRING_VALUE = ".getStringValue()";
    private final ScriptEngine testScriptEngine;

    Logger logger = ImageJFX.getLogger();
    
    public DefaultSelector() {
        testScriptEngine = getEngine();
        testScriptEngine.put(METADATASET_STRING, new HashMap<String, MetaData>());
        testScriptEngine.put(TAG_STRING, new ArrayList<String>());
        nonParsedString = new String();

    }

    public DefaultSelector(String nonParsedString) {
        testScriptEngine = getEngine();
        testScriptEngine.put(METADATASET_STRING, new HashMap<String, MetaData>());
        testScriptEngine.put(TAG_STRING, new ArrayList<String>());
        parse(nonParsedString);
    }

    /**
     * Parse a query selector
     *
     * @param nonParsedString A string containing a request.
     * @return A string to be evaluated by a javaStript engine.
     */
    //the selector has this format:
    //"key" = "a value" or ("key2" > 42 and "key3" = \Regex) or "tag" 
   // @Override
    public void parse(String nonParsed) {
        nonParsedString = nonParsed;
        metaDataPositions = QueryParser.parseMetaData(nonParsed);
        tagPositions = QueryParser.parseTag(nonParsed);
        logicalOperatorPositions = QueryParser.getLogicalOperatorPosition(nonParsed);
        QueryParser.suppressFakePosition(metaDataPositions, tagPositions, logicalOperatorPositions);
        createJavaScriptString();
        updateValidSyntax();

    }

    private void createJavaScriptString() {
        List<ReplaceStringPosition> replaceList = getReplaceStringPosition();
        int i = 0;
        int s = 0;
        StringBuffer sb = new StringBuffer();
        for (ReplaceStringPosition replacement : replaceList) {
            int start = replacement.startPos();
            //append the part of the string that are not in parsed groups
            while (s < start) {
                sb.append(nonParsedString.subSequence(s, s + 1));
                s += 1;
            }
            sb.append(replacement.getReplacement());
            s = replacement.endPos();
        }
        //append tail
        while (s < nonParsedString.length()) {
            sb.append(nonParsedString.subSequence(s, s + 1));
            s += 1;
        }
        parsedString = sb.toString();

    }

    //returns a sorted list of replaceStringPosition
    private List<ReplaceStringPosition> getReplaceStringPosition() {
        List<ReplaceStringPosition> replaceList = new ArrayList<>();
        for (MetaDataPosition metaPos : metaDataPositions) {
            String keyWord = nonParsedString.substring(metaPos.startKeyWordPos(), metaPos.endKeyWordPos());
            String key = nonParsedString.substring(metaPos.startKeyPos(), metaPos.endKeyPos());
            String comparison = nonParsedString.substring(metaPos.startLogicalPos(), metaPos.endLogicalPos());
            String value = nonParsedString.substring(metaPos.startValuePos(), metaPos.endValuePos());
            String metaDataRewrited = rewriteMetaData(keyWord, key, comparison, value);
            replaceList.add(new ReplaceStringPositionImpl(metaPos.startPos(), metaPos.endPos(), metaDataRewrited));
        }
        for (TagPosition tagPos : tagPositions) {
            String keyWord = nonParsedString.substring(tagPos.startKeyWordPos(), tagPos.endKeyWordPos());
            String tag = nonParsedString.substring(tagPos.startTagPos(), tagPos.endTagPos());
            String rewritedTag = rewriteTag(keyWord, tag);
            replaceList.add(new ReplaceStringPositionImpl(tagPos.startPos(), tagPos.endPos(), rewritedTag));
        }
        for (StringPosition logicalPos : logicalOperatorPositions) {
            String logicalOperator = nonParsedString.substring(logicalPos.startPos(), logicalPos.endPos());
            String rewritedLogical = rewriteLogical(logicalOperator);
            replaceList.add(new ReplaceStringPositionImpl(logicalPos.startPos(), logicalPos.endPos(), rewritedLogical));

        }
        Collections.sort(replaceList);
        return replaceList;
    }

    private String formatSelector(String parsedSelector) {
        return "try {" + parsedSelector + ";" + "}catch(err) {1 == 0;}";
    }

    public static String formatRegex(String key, String regex) {
        return formatGetKey(key) + CHECK_NULL + " " + AND + " " + regex + ".test("
                + formatGetKey(key) + GET_STRING_VALUE + ")";
    }

    public static String formatKey(String key) {
        return formatGetKey(key) + CHECK_NULL + " " + AND + " " + formatGetKey(key) + GET_META_VALUE;
    }

    public static String formatGetKey(String key) {
        return PlaneDBInMemory.METADATASET_STRING + "[\""
                + key + "\"]";
    }

    private String rewriteMetaData(String keyWord, String key, String operator, String value) {
        String operatorRewrited = rewriteComparisonOperator(operator);
        SimpleDateFormat f = new SimpleDateFormat(DATE_PATTERN);
        String metaDataRewrited = null;
        try {
            Date date = f.parse(value);
            long dateLong = date.getTime();
            metaDataRewrited = formatKey(key) + operatorRewrited + date;
        } catch (ParseException ex) {
            try {
                double nb = Double.parseDouble(value);
                metaDataRewrited = formatKey(key) + operatorRewrited + value;
            } catch (NumberFormatException numberEx) {
                if (value.matches("/.+/")) { // regex detected
                    if (operatorRewrited.equals("==")) {
                        metaDataRewrited = formatRegex(key, value);
                    } else {
                        metaDataRewrited = "";
                    }

                } else if (value.equals(ALL)) {
                    if (operatorRewrited.equals("==")) {
                        metaDataRewrited = formatGetKey(key) + CHECK_NULL;
                    } else {
                        metaDataRewrited = "";
                    }
                } else {
                    metaDataRewrited = formatKey(key) + operatorRewrited + "\"" + value + "\"";
                }
            }

        }
        if (keyWord.equals(NOT)) {
            metaDataRewrited = rewriteNotExpression(metaDataRewrited);
        }
        return metaDataRewrited;
        

    }
    private String rewriteNotExpression(String expression) {
        return "!(" + expression + ")";
    }

    private String rewriteComparisonOperator(String operator) {
        if (operator.equals("=")) {
            return "==";
        }
        return operator;
    }

    private String rewriteLogical(String logicalOperator) {
        switch (logicalOperator) {
            case "or":
                return OR;
            case "and":
                return AND;
        }
        return "";
    }

    private String rewriteTag(String keyWord, String tag) {
        if (tag == null) {
            return "";
        }
        String tagRewrited =  PlaneDBInMemory.TAG_STRING + ".contains(\"" + tag + "\")";
        if (keyWord.equals(NOT)) {
            tagRewrited = rewriteNotExpression(tagRewrited);
        }
        return tagRewrited;
    }

    private void checkSyntax() {
        if (parsedString != null) {

        }
    }

    
    public String getParsedSelector() {
        if (parsedString == null) {
            parse(nonParsedString);
        }
        return parsedString;
    }

    @Override
    public String getNonParsedString() {
        return nonParsedString;
    }

    @Override
    public void setValidSyntax(boolean valid) {
        validSyntax.set(valid);
    }

    
    public List<StringPosition> getLogicalOperatorPositions() {
        return logicalOperatorPositions;
    }

    
    public List<MetaDataPosition> getMetaDataPositions() {
        return metaDataPositions;
    }

    
    public List<TagPosition> getTagPositions() {
        return tagPositions;
    }

    public List<WordPosition> getWordPositions() {
        List<WordPosition> wordPos = new ArrayList<>();
        wordPos.addAll(WordPosition.transformStringPosToWordList(logicalOperatorPositions, WordPosition.WordType.LOGICAL));
        wordPos.addAll(WordPosition.transformMetaPosToWordList(metaDataPositions));
        wordPos.addAll(WordPosition.transformTagToWordList(tagPositions));
        Collections.sort(wordPos);
        return wordPos;
    }

    public ReadOnlyBooleanProperty validSyntaxProperty() {
        return validSyntax.getReadOnlyProperty();
    }

    private void updateValidSyntax() {
        validSyntax.set(isValidSyntax());
    }

    private boolean isValidSyntax() {
        if (parsedString == null) {
            return false;
        }
        try {
            Object response = testScriptEngine.eval(parsedString);
        } catch (ScriptException | ClassCastException ex) {
            return false;
        }
        return true;
    }

    
    public String getTagQuery(String tag) {
        return "\"" + tag + "\"";
    }

    
    public String getMetaDataQuery(MetaData metaData) {
        return getMetaDataQuery(metaData.getName(), metaData.getStringValue());
    }

    
    public String getMetaDataQuery(String key, String value) {
        return "\"" + key + "\"=\"" + value + "\"";
    }

    public String getSeparator() {
        return " ";
    }

    
    
    private ScriptEngine getEngine() {
        
        
        
        return new ScriptEngineManager().getEngineByName("nashorn");
    }

    @Override
    public String getQueryString() {
        return nonParsedString;
    }

    @Override
    public boolean matches(PlaneDB plane, String metaDataSetName) {
         ScriptEngine scriptEngine = getEngine();
        boolean queryResult = false;
        boolean validSyntax = false;
        Object response = new Object();
        
        //String metaDataSetName = null;
        
        ReadOnlyMapProperty<String, MetaData> set = metaDataSetName != null ? plane.getMetaDataSetProperty(metaDataSetName) : plane.getMetaDataSet();
        QueryService.putVariableInScriptEngine(scriptEngine, plane.getTags().get(), set.get());
        try {
            logger.info("Executing the following script : "+getParsedSelector());
            response = scriptEngine.eval(getParsedSelector());
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
        setValidSyntax(validSyntax);

        return queryResult;
    }

}
