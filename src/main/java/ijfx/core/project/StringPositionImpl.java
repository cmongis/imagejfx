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

/**
 *
 * @author Cyril Quinton
 */
public class StringPositionImpl implements StringPosition {

    protected int startPosition;
    protected int endPosition;
    protected int startKeyWordPosition;
    protected int endKeyWordPosition;

    @Override
    public int startPos() {
        return startPosition;
    }

    @Override
    public int endPos() {
        return endPosition;
    }

    @Override
    public boolean setPosition(int start, int stop) {
        if (StringPosition.checkPosition(start, stop)) {
            startPosition = start;
            endPosition = stop;

            return true;
        }
        return false;
    }

    @Override
    public int compareTo(StringPosition o) {
        if (this.startPosition < o.startPos()) {
            return -1;
        } else if (this.startPosition > o.endPos()) {
            return 1;
        } else {
            return 0;
        }
    }

}
