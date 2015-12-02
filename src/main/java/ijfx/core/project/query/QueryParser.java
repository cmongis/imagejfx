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

import ijfx.core.project.DefaultMetaDataPosition;
import ijfx.core.project.MetaDataPosition;
import ijfx.core.project.StringPosition;
import ijfx.core.project.StringPositionImpl;
import ijfx.core.project.TagPosition;
import ijfx.core.project.TagPositionImpl;
import ijfx.core.project.WordPosition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 * An interface used to parse queries related to a {@link Project} object
 *
 * @author Cyril Quinton
 */
public interface QueryParser {
     public static String NON_PARSED_STRING = "nonParsed";
     public static String ENABLE = "enable";
     public static String NOT = "-";
     public static String ALL = "*";
    public static String BLANK_PATTERN1 = "\\s*";
    public static String FINITE_BLANK_PATTERN = "\\s{0,100}";
    public static String COMPARISON_OPERATOR_PATTERN1 = "(?:" + FINITE_BLANK_PATTERN + ")" + "(=+|<|>|<=|>=)" + "(?:" + FINITE_BLANK_PATTERN + ")";
    public static String COMPARISON_OPERATOR_PATTERN_NOT = "(" + "(?:" + FINITE_BLANK_PATTERN + ">" + FINITE_BLANK_PATTERN + ")" + "|" + "(?:" + FINITE_BLANK_PATTERN + "<" + FINITE_BLANK_PATTERN + ")" + "|" + "(?:" + FINITE_BLANK_PATTERN + "=" + FINITE_BLANK_PATTERN + ")" + ")";
    public static String KEY_PATTERN_3 = "(?:[\"])([^\"=<>]+)(?:[\"])";
    //public static String VALUE_PATTERN = KEY_PATTERN_3 + "|[^<>=\\s]+(?=$|\\s))";
    public static String VALUE_PATTERN = KEY_PATTERN_3;

    public static String LOGICAL_PATTERN = "\\s(or|and)\\s";
    public static String EQUAL = "equal";
    public static String INFERIOR = "inferior";
    public static String SUPERIOR = "superior";
    public static String DATE_PATTERN = "dd-MMM-yyyy";
    public static String REMOVE_KEY_WORD = "-";
    public static String KEY_WORD_PATTERN1 = "([" + REMOVE_KEY_WORD + "]?)";

    public enum ParsedType {

        REGEX, DATE, STRING, GENERIC
    };

    public void parse(String nonParsedString);

    public List<WordPosition> getWordPositions();

    public List<MetaDataPosition> getMetaDataPositions();

    public List<TagPosition> getTagPositions();

    public String getNonParsedString();

    public ReadOnlyBooleanProperty validSyntaxProperty();

    public void setValidSyntax(boolean valid);

    public static Object castString(String value) {
        Object object;
        try {
            object = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            try {
                object = Double.valueOf(value);
            } catch (NumberFormatException ee) {
                object = value;
            }
        }
        return object;
    }

    public static List<MetaDataPosition> parseMetaData(String exp) {
        String pattern = KEY_WORD_PATTERN1 + KEY_PATTERN_3 + COMPARISON_OPERATOR_PATTERN1 + VALUE_PATTERN;
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(exp);
        List<MetaDataPosition> posList = new ArrayList<>();
        while (m.find()) {
            boolean setted;
            MetaDataPosition metaDataPosition = new DefaultMetaDataPosition();
            setted = metaDataPosition.setPosition(m.start(), m.end());
            if (!setted) {
                break;
            }
            setted = metaDataPosition.setKeyWordPosition(m.start(1), m.end(1));
            if (!setted) {
                break;
            }

            setted = metaDataPosition.setKeyPosition(m.start(2), m.end(2));
            if (!setted) {
                break;
            }

            setted = metaDataPosition.setLogicalOperatorPosition(m.start(3), m.end(3));
            if (!setted) {
                break;
            }

            setted = metaDataPosition.setValuePosition(m.start(4), m.end(4));
            if (!setted) {
                break;
            }

            posList.add(metaDataPosition);
        }
        return posList;
    }

    public static List<TagPosition> parseTag(String exp) {
        //look for a key without =><
        Pattern p = Pattern.compile(KEY_WORD_PATTERN1 + KEY_PATTERN_3);
        Matcher m = p.matcher(exp);
        List<TagPosition> posList = new ArrayList<>();
        while (m.find()) {
            boolean setted;
            TagPosition tagPos = new TagPositionImpl();
            setted = tagPos.setPosition(m.start(), m.end());
            if (!setted) {
                break;
            }

            setted = tagPos.setKeyWordPosition(m.start(1), m.end(1));
            if (!setted) {
                break;
            }
            setted = tagPos.setTagPosition(m.start(2), m.end(2));
            if (!setted) {
                break;
            }
            posList.add(tagPos);

        }
        return posList;
    }

    public static List<StringPosition> getLogicalOperatorPosition(String exp) {
        List<StringPosition> logicalPosList = new ArrayList<>();
        Pattern p = Pattern.compile(LOGICAL_PATTERN);
        Matcher m = p.matcher(exp);
        while (m.find()) {
            boolean setted;
            StringPosition logicalPos = new StringPositionImpl();
            setted = logicalPos.setPosition(m.start(1), m.end(1));
            if (!setted) {
                break;
            }

            logicalPosList.add(logicalPos);
        }
        return logicalPosList;
    }

    //replace the operator and add blanks around.
    public static String replaceLogicalOperator(String exp, String original, String replacement, List<Integer> logicalOperatorPositions) {
        String around = "([^a-zA-Z" + original + "])";
        Pattern p = Pattern.compile(around + "(" + original + ")" + around);
        Matcher m = p.matcher(exp);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            logicalOperatorPositions.add(m.start());
            logicalOperatorPositions.add(m.end());
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static boolean overlap(int startPos1, StringPosition pos2) {
        return (startPos1 >= pos2.startPos() && startPos1 <= pos2.endPos());
    }
    //suppress logicalPosition that are in tagPosition or in metaDataPosition.
    //supress tagPositions that are in metaDataPosition
    public static void suppressFakePosition(List<MetaDataPosition> metaPositions, List<TagPosition> tagPositions, List<StringPosition> logicalPositions) {
        Iterator<StringPosition> it = logicalPositions.iterator();
        while (it.hasNext()) {
            StringPosition logicalPos = it.next();
            int startLogicalPos = logicalPos.startPos();
            for (StringPosition metaPos : metaPositions) {
                // the logical operator overlaps with the metadata position
                if (overlap(startLogicalPos, metaPos)) {
                    it.remove();
                }
            }
            for (StringPosition tagPos : tagPositions) {
                if (overlap(startLogicalPos, tagPos)) {
                    it.remove();
                }
            }
        }
        suppressFakePosition(metaPositions, tagPositions);
    }
    //supress tagPositions that are in metaDataPosition
    public static void suppressFakePosition(List<MetaDataPosition> metaPositions, List<TagPosition> tagPositions) {
        Iterator<TagPosition> it = tagPositions.iterator();
        while (it.hasNext()) {
            TagPosition tagPos = it.next();
            int startTagPos = tagPos.startPos();
            for (StringPosition metaPosition: metaPositions) {   
                // the logical operator overlaps with the metadata position
                if (overlap(startTagPos, metaPosition)) {
                    it.remove();
                }
            }
        }
    }

}
