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

import ijfx.service.overlay.OverlayStatService;

/**
 *
 * @author cyril
 */
public class MetaDataKeyPriority {

    public static final String[] FILE = {
        MetaData.FILE_NAME,
        MetaData.WIDTH,
        MetaData.HEIGHT,
        MetaData.BITS_PER_PIXEL,
        MetaData.SLICE_NUMBER,
        MetaData.SERIE_COUNT,
        MetaData.SERIE,
        MetaData.SLICE_NUMBER,
        MetaData.ZSTACK_NUMBER,
        MetaData.CHANNEL_COUNT,
        MetaData.TIME_COUNT,
        MetaData.LBL_MEAN,
        MetaData.LBL_MIN,
        MetaData.LBL_MAX};
    public static final String[] PLANE = {
        MetaData.FILE_NAME,
        MetaData.PLANE_INDEX,
        MetaData.CHANNEL,
        MetaData.TIME,
        MetaData.Z_POSITION,
         MetaData.BITS_PER_PIXEL,
            
    };
    public static final String[] OBJECT = {
        MetaData.FILE_NAME,
        MetaData.NAME,
        
        MetaData.PLANE_INDEX,
        MetaData.CHANNEL,
        MetaData.TIME,
        MetaData.Z_POSITION,
        MetaData.COUNT,
        MetaData.LBL_MEAN,
        MetaData.LBL_SD,
        MetaData.LBL_CIRCULARITY,
        MetaData.LBL_AREA,
        MetaData.LBL_CENTER_X,
        MetaData.LBL_CENTER_Y,
    };
    
    

    public static String[] getPriority(MetaDataSet m) {
        if (m == null) {
            return null;
        }
        
        MetaDataSetType t = m.getType();
        
        if(t == MetaDataSetType.PLANE) {
            return PLANE;
        }
        if(t == MetaDataSetType.OBJECT) {
            return OBJECT;
        }
        return FILE;
       
    }

}
