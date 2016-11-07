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
package ijfx.core.project;

import junit.framework.Assert;
import mongis.utils.StringUtils;
import org.junit.Test;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class BasicTests {
    
    
    @Test
    public void testDecimalDisplay() {
        
        Assert.assertEquals("123.45", StringUtils.numberToString(123.455424234, 2));
        Assert.assertEquals("123",StringUtils.numberToString(123d,0));
        Assert.assertEquals("123",StringUtils.numberToString(123.1234,0));
        Assert.assertEquals("123.4",StringUtils.numberToString(123.4,3));
        
        
    }
}
