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
public class DefaultMetaDataPosition extends KeyWordStringPosition implements MetaDataPosition {

    private int startKeyPosition;
    private int endKeyPosition;
    private int startValuePosition;
    private int endValuePosition;
    private int startLogicalOperatorPosition;
    private int endLogicalOperatorPosition;

    @Override
    public int startKeyPos() {
        return startKeyPosition;
    }

    @Override
    public int endKeyPos() {
        return endKeyPosition;
    }

    @Override
    public int startValuePos() {
        return startValuePosition;
    }

    @Override
    public int endValuePos() {
        return endValuePosition;
    }

    @Override
    public int startLogicalPos() {
        return startLogicalOperatorPosition;
    }

    @Override
    public int endLogicalPos() {
        return endLogicalOperatorPosition;
    }

    @Override
    public boolean setKeyPosition(int start, int stop) {
        if (StringPosition.checkPosition(start, stop)) {
            startKeyPosition = start;
            endKeyPosition = stop;
            return true;
        }
        return false;
    }

    @Override
    public boolean setValuePosition(int start, int stop) {
        if (StringPosition.checkPosition(start, stop)) {
            startValuePosition = start;
            endValuePosition = stop;
            return true;
        }
        return false;
    }

    @Override
    public boolean setLogicalOperatorPosition(int start, int stop) {
        if (StringPosition.checkPosition(start, stop)) {
            startLogicalOperatorPosition = start;
            endLogicalOperatorPosition = stop;
            return true;
        }
        return false;
    }

}
