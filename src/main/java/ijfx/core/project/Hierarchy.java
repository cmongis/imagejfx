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
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class Hierarchy implements Iterable<String>{
    private ArrayList<String> hierarchyList;
    private int level;
    public Hierarchy() {
        this.hierarchyList = new ArrayList<>();
    }
    public Hierarchy(ArrayList<String> hierarchyList) {
        this.hierarchyList = hierarchyList;
        //by default the hierarchy level is set to the penultimate key in
        //in the hierarchy
        level = this.hierarchyList.size() - 2;
    }
    public Hierarchy(ArrayList<String> hierarchyList, int level) {
        this.hierarchyList = hierarchyList;
        this.level = level;
    }

    public List<String> getHierarchyList() {
        return hierarchyList;
    }

    public void setHierarchyList(ArrayList<String> hierarchyList) {
        this.hierarchyList = hierarchyList;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) throws IndexOutOfBoundsException{
        if (level < hierarchyList.size() && level > 0) {
        this.level = level;
        } else {
            throw new IndexOutOfBoundsException("the level is out of range");
        }
    }

    @Override
    public Iterator<String> iterator() {
        Iterator<String> it = new Iterator<String>() { 
           private int currentIndex = 0;
            @Override
            public boolean hasNext() {
                return currentIndex <= level && hierarchyList.get(currentIndex) != null;
            }

            @Override
            public String next() {
                return hierarchyList.get(currentIndex ++);
            }
        };
        return it;
    }
    
    
}
