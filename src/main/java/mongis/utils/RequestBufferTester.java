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
package mongis.utils;

import ijfx.ui.utils.BaseTester;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class RequestBufferTester extends BaseTester{

    RequestBuffer buffer = new RequestBuffer(1);
    
    public RequestBufferTester() {
        
        super();
        addAction("Launch", this::test);
        
    }

    
    
    
    
    @Override
    public void initApp() {
        
    }
    
    
    public void test() {
        for(int i = 0;i!=20000;i++) {
            buffer.queue(this::showSomething);
        }
    }
    
    public void showSomething() {
        System.out.println("I'm the only one ?");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
