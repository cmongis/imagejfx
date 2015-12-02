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
 * An Exception which should be raised when a String describing a modifier do
 * not have the good format and therefore, cannot be parsed. 
 * <br>
 * The format of a modifier is as follows: "$key = 'value'" or "$key = 42".
 * when one value is missing, this exception should be raised.
 * @author Cyril MONGIS, 2015
 */
public class ModifierFormatException extends Exception {

    /**
     * Creates a new instance of <code>ModifierFormatException</code> without
     * detail message.
     */
    public ModifierFormatException() {
    }

    /**
     * Constructs an instance of <code>ModifierFormatException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ModifierFormatException(String msg) {
        super(msg);
    }
}
