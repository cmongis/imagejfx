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
package ijfx.core.metadata;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Orders metadata by priority list
 *
 * @author Cyril MONGIS, 2016
 */
public class MetaDataKeyPrioritizer implements Comparator<String> {

    final private String[] priority;

    public MetaDataKeyPrioritizer(String[] priority) {
        this.priority = priority;
    }
    
    /*
    public MetaDataKeyPrioritizer(Set<String> priority) {
        this.priority = priority;
    }*/

    @Override
    public int compare(String s1, String s2) {

        Integer is1 = priorityIndex(priority, s1);
        Integer is2 = priorityIndex(priority, s2);

        Integer c = s1.compareTo(s2);

        return 100 * (is2 - is1) + c;
    }

    private int priorityIndex(String[] set, String element) {
        int i = 0;
        for (String s : set) {
            if (s.equals(element)) {
                return 100 - i;
            }
            i++;
        }
        return 0;
    }
    
    public String[] getPriority() {
        return priority;
    }
    
    public boolean isSame(String[] newPriority) {
        return Arrays.equals(priority, newPriority);
    }

}
