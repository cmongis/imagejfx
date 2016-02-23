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
package ijfx.core.project.modifier;

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.project.command.AddMetaDataCommand;
import ijfx.core.project.command.Command;
import ijfx.core.project.command.CommandList;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mongis.ndarray.Dimension;
import mongis.ndarray.NDimensionalArray;
import mongis.utils.ConditionList;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = ModifierPlugin.class)
public class MapDimensionsModifier implements ModifierPlugin{

    
    String order;
    
    String dimension;
    
    private static Pattern PATTERN = Pattern.compile("^mapDimensions:(\\w+),([\\d\\,])$");
    
    private static String NUMBER_SEPARATOR = ",";
    
    NDimensionalArray ndArray;
    
    Logger logger = ImageJFX.getLogger();
    
    
    public MapDimensionsModifier() {
        
    }
    public MapDimensionsModifier(String order, int... dimensionSize) {
        ndArray = new NDimensionalArray(order, dimensionSize);
    }
    
    
    @Override
    public boolean configure(String query) {
        Matcher m = PATTERN.matcher(query);
        if(m.matches()) {
            order = m.group(1);
            dimension = m.group(2);
            
            Integer[] dims = Arrays
                    .asList(dimension
                            .split(NUMBER_SEPARATOR))
                            .stream()
                            .map(n->Integer.parseInt(n))
                    .toArray(size->new Integer[size]);
            
            ndArray = new NDimensionalArray(order, dims);
            
            return true;
        }
        else {
            return false;
        }
    }
    
    private MetaData getPlaneProperty(PlaneDB db, Dimension d) {
        return db.getMetaDataSet().get(getPropertyName(d));
    }
    
    private String getPropertyName(Dimension d) {
        if(d.getName().equals("C")) return MetaData.CHANNEL;
        if(d.getName().equals("T")) return MetaData.TIME;
        if(d.getName().equals("Z")) return MetaData.Z_POSITION;
        return null;
    }
    

    @Override
    public Command getModifyingCommand(PlaneDB planeDB) {
       
        
        List<Command> commands = new ArrayList<>();
        
        getCalculatedIndexesMap(planeDB).forEach((propertyName,indx)->{
            commands.add(new AddMetaDataCommand(planeDB, new GenericMetaData(propertyName, indx)));
        });
        
        return new CommandList(commands);
    }
    
    private Map<String,Integer> getCalculatedIndexesMap(PlaneDB planeDB) {
        Map<String,Integer> map = new HashMap<>();
        
        // getting all the possibilities of index set
        long[][] possibilities = ndArray.get(0).generateAllPossibilities();
        
        // getting the original index of the plane (some planes could be swappared with modified plane
        // where the actual index is 0. By requesting the original, we make sure no mistake is made.
        int index = planeDB.getMetaDataSet().get(MetaData.PLANE_INDEX).getIntegerValue();
        
        long[] indexSet = possibilities[index];
        
        List<Command> commands = new ArrayList<>();
        
        // going through all dimension
        for(int i= 0;i!=ndArray.size();i++) {
            // getting the associted dimension with its properties
            Dimension d = ndArray.get(i);
            map.put(getPropertyName(d),(int)indexSet[i]);
        }
        
        return map;
    }

    @Override
    public boolean wasApplied(PlaneDB planeDB) {
        
        ConditionList areMetadataRight = new ConditionList();
        
        getCalculatedIndexesMap(planeDB).forEach((propertyName,index)->{
            areMetadataRight.add(planeDB.getMetaDataSet().get(propertyName).getIntegerValue().equals(index));
        });
        
        
        return areMetadataRight.isAllTrue();
    }

    @Override
    public String phraseMe() {
        return "";
    }
    
    
    
    public static int[] getDimensionIndexes(int[] dims, int planeIndex) {
        
        int[] indexes = new int[dims.length];
        
        for(int i = 0; i != planeIndex;i++) {
          //  System.out.println("");
            for(int y =0;y!=dims.length-1;y++) {
                if(indexes[y+1] == dims[y]-1) {
                    indexes[y]++;
                    indexes[y+1] = 0;
                }
                else {
                    indexes[y+1]++;
                }
            }
        }
        
        return indexes;
    }

    public String getOrder() {
        return order;
    }

    public String getDimension() {
        return dimension;
    }
    
    
    
    
    
    
    
}
