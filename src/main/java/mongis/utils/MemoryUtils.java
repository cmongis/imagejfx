/*
    This file is part of PSFj.

    PSFj is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PSFj is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PSFj.  If not, see <http://www.gnu.org/licenses/>. 
    
	Copyright 2013,2014 Cyril MONGIS, Patrick Theer, Michael Knop
	
 */
package mongis.utils;

// TODO: Auto-generated Javadoc
/**
 * The Class MemoryUtils.
 */
public class MemoryUtils {

/**
 * Gets the available memory.
 *
 * @return the available memory
 */
public static long getAvailableMemory() {
		
		Runtime runtime = Runtime.getRuntime();
	
		int max = (int) (runtime.maxMemory()/1024/1024);
		int total = (int) (runtime.totalMemory()/1024/1024);
		int free = (int) (runtime.freeMemory()/1024/1024);
		
		free = free+ max - total;
		
		int used = max-free;
		
		
		return free;
		
	}
	
	/**
	 * Gets the total memory.
	 *
	 * @return the total memory
	 */
	public static long getTotalMemory() {
		return Runtime.getRuntime().maxMemory()/1024/1024;
	}
	public static long getMaximumMemory() {
		return Runtime.getRuntime().maxMemory()/1024/1024;
	}
}
