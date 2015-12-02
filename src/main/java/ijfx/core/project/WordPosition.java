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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Cyril Quinton
 */
public interface WordPosition extends StringPosition {
    public static List<WordPosition> transformTagToWordList(List<TagPosition> tagPositions) {
        List<WordPosition> wordPositions = new ArrayList<>();
        for (TagPosition tagPos: tagPositions) {
            WordPosition wordPos = new WordPositionImpl();
            wordPos.setPosition(tagPos.startTagPos(), tagPos.endTagPos());
            wordPos.setWordType(WordType.TAG);
            wordPositions.add(wordPos);
        }
        return wordPositions;
        
    }
    public static List<WordPosition> transformMetaPosToWordList(List<MetaDataPosition> metaDataPositions) {
        List<WordPosition> wordPositions = new ArrayList<>();
        for (MetaDataPosition metaPos: metaDataPositions) {
            WordPosition wordPos = new WordPositionImpl();
            wordPos.setPosition(metaPos.startKeyPos(), metaPos.endValuePos());
            wordPos.setWordType(WordType.METADATA);
            wordPositions.add(wordPos);
        }
        return wordPositions;
    }
    public static List<WordPosition> transformStringPosToWordList  (List<StringPosition> stringPosition, WordType type) {
        List<WordPosition> wordPositions = new ArrayList<>();
        for (StringPosition stringPos: stringPosition) {
            WordPosition wordPos = new WordPositionImpl();
            wordPos.setPosition(stringPos.startPos(), stringPos.endPos());
            wordPos.setWordType(type);
            wordPositions.add(wordPos);
        }
        return wordPositions;
    }
    public enum WordType {

        METADATA, TAG, LOGICAL
    };

    WordType getWordType();

    void setWordType(WordType wordType);
}
