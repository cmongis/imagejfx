/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.core.project.query;

/**
 *
 * @author cyril
 */
public interface Phrasable {
    
    /**
     * returns a string that represent the action or description of a particular object
     * 
     * e.g. for a @Modifier that adds a metadata to a plane, phraseMe() could return :
     * 
     * "add the metadata *id* with the value *20* to a plane"
     * 
     * We encourage the use of simplified version of Markdown to simply parsing of important elements
     * of the phrasing
     * 
     * @return returns a string that represent the action or description of a particular object
     */
    public String phraseMe();
}
