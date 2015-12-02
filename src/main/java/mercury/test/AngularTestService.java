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
package mercury.test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import ijfx.ui.main.ImageJFX;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mercury.core.Deferred;
import mercury.core.AngularMethod;
import mercury.core.JSONParameters;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class AngularTestService {
    
    
    @AngularMethod(description="Test basic parsing and object return")
    public void testJSONParsing(Deferred deferred, JSONParameters parameters) {

        String firstString = parameters.getFirstString();
        Integer firstInt = parameters.getFirstInt();
        Double firstDobule = parameters.getFirstDouble();
        
        String aString = parameters.getString("aString");
        Integer aInteger = parameters.getInt("aInt");
        Double aDouble = parameters.getDouble("aDouble");



        
        
        
        HashMap<String,Object> hash = parameters.getHash();

        
        String orginalJSON = parameters.getJSONString();
        
        //FakeObject mapObject = parameters.mapObject(FakeObject.class);

        //List<FakeObject> fakeObjectList = parameters.mapObjectList(FakeObject.class);
        
        
       
        deferred.notifySimpleJSON("this is","great");
        
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
        
        
        deferred.resolveSimpleJSON("it's the end","we know it");
        
        
    }
    
    @AngularMethod(description = "", sync = true)
    public String getIceCream(String flavour, int balls) {
        return new StringBuilder()
                .append("Here are ")
                .append(balls)
                .append(" of ")
                .append(flavour)
                .append(".").toString();
    
}
    
    
    
    
    
    
}
