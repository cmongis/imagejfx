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

public class WordPositionImpl implements WordPosition {

    private WordType wordType;
    private int startPos;
    private int endPos;

    @Override
    public WordType getWordType() {
        return wordType;
    }

    @Override
    public void setWordType(WordType wordType) {
        this.wordType = wordType;
    }

    @Override
    public int startPos() {
        return startPos;
    }

    @Override
    public int endPos() {
        return endPos;
    }

    @Override
    public boolean setPosition(int start, int stop) {
        if (StringPosition.checkPosition(start, stop)) {
            startPos = start;
            endPos = stop;
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(StringPosition o) {
        if (startPos > o.endPos()) {
            return 1;
        } else if (endPos < o.startPos()) {
            return -1;
        } else {
            return 0;
        }
    }

}
