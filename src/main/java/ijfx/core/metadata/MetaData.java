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
package ijfx.core.metadata;

import java.util.HashMap;
import java.util.Map;
import mongis.utils.StringUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Cyril MONGIS, 2015
 */
public interface MetaData {

    

    /**
     * Transforms a metaDataSet to a string to string hashMap. This method can
     * be useful if one wants to write this metaDataSet to a file in a string
     * format.
     *
     * @param metaDataSet
     * @return
     */
    public static HashMap<String, String> metaDataSetToMap(Map<String, MetaData> metaDataSet) {
        HashMap<String, String> map = new HashMap<>();
        for (String key : metaDataSet.keySet()) {
            map.put(key, metaDataSet.get(key).getValue().toString());
        }
        return map;
    }

    public String getName();

    public void setName(String name);

    public void setValue(Object value);

    public String getStringValue();

    public Integer getIntegerValue();

    public Object getValue();

    public Double getDoubleValue();

    public boolean isNull();
    
    public final static int TYPE_STRING = 0;
    public final static int TYPE_INTEGER = 1;
    public final static int TYPE_DOUBLE = 2;
    public final static int TYPE_NOT_SET = -1;
    public final static int TYPE_UNKNOWN = 3;
    public final static int TYPE_NUMBER = 4;
    
    
    public int getType();

    public int getOrigin();

    public final static int ORIGIN_BASIC = 0;
    public final static int ORIGIN_RAW = 1;
    public final static int ORIGIN_ADDED = 2;
    public final static int ORIGIN_CALCULTAED = 3;

    public final static String WIDTH = "Width";
    public final static String HEIGHT = "Height";
    public final static String CHANNEL = "Channel";
    public final static String X_POSITION = "X";
    public final static String Y_POSITION = "Y";
    public final static String Z_POSITION = "Z";
    public final static String TIME = "Time";
    public final static String DATE = "Date";
    public final static String YEAR = "Year";
    public final static String MONTH = "Month";
    public final static String DAY = "Day";
    public final static String WELL_NAME = "Well";
    public final static String POSITION = "Pos";

    public final static String SLICE_NUMBER = "Slice number";
    public final static String FILE_NAME = "File Name";
    public final static String FOLDER_NAME = "Folder Name";
    public final static String ABSOLUTE_PATH = "Absolute path";
    public final static String CHANNEL_NAME = "Channel name";
    public final static String SEQUENCE_NUMBER = "Seq";

    public final static String METADATA_SET_TYPE_KEY = "*Metadataset type";
    
    public final static String METADATA_SET_TYPE_FILE = "file";
    public final static String METADATA_SET_TYPE_PLANE = "plane";
    public final static String METADATA_SET_TYPE_OBJECT = "object";
    
    public final static String NAME = "Name";
    
    public static String FILE_SIZE = "File size";

    public final static String PLANE_INDEX = "Plane Index";

    public final static String WAS_MODIFIED = "modified";

    public final static String SERIE_COUNT = "Series";
    public final static String SERIE = "Serie";
    public final static String CHANNEL_COUNT = "Channel number";

    public final static String ZSTACK_NUMBER = "Z Slice number";
    public final static String PIXEL_TYPE = "Pixel Type";
    public final static String BITS_PER_PIXEL = "Bits per pixels";
    public final static String TIME_COUNT = "Time frame number";

    public final static String DIMENSION_ORDER = "*Dimension order";
    public final static String DIMENSION_LENGHS = "*Dimension lengths";
    public final static String PLANE_NON_PLANAR_POSITION = "*Non planar position";
    public final static String SAVE_NAME = "*Save name"; // Metadata used for explorable that should be saved under an other name
    
    public final static String STATS_PIXEL_MIN = "Min";
    public final static String STATS_PIXEL_MAX = "Max";
    public final static String STATS_PIXEL_MEAN = "Mean";
    
    public final static String STATS_PIXEL_STD_DEV = "Standard deviation";
 
    public static final String LBL_CIRCULARITY = "Circularity";
    public static final String LBL_MEDIAN = "Median";
    public static final String LBL_MEAN = STATS_PIXEL_MEAN;
    public static final String LBL_CENTER_Y = "Center Y";
    public static final String LBL_AREA = "Area";
    public static final String LBL_MIN = STATS_PIXEL_MIN;
    public static final String LBL_MIN_FERET_DIAMETER = "Min. Feret Diameter";
    public static final String LBL_VARIANCE = "Variance";
    public static final String LBL_SOLIDITY = "Solidity";
    public static final String LBL_CENTROID = "Center of Gravity";
    public static final String LBL_THINNES_RATIO = "Thinnes ratio";
    public static final String LBL_ASPECT_RATIO = "Aspect ratio";
    public static final String LBL_LONG_SIDE_MBR = "Long Side MBR";
    public static final String LBL_SHORT_SIDE_MBR = "Short Side MBR";
    public static final String LBL_CENTER_X = "Center X";
    public static final String LBL_SD = "Std. Dev.";
    public static final String LBL_KURTOSIS = "Kurtosis";
    public static final String LBL_SKEWNESS = "Skewness";
    public static final String LBL_MBR = "Minimum Bounding Rectangle";
    public static final String LBL_PIXEL_COUNT = "Pixel count";
    public static final String LBL_MAX_FERET_DIAMETER = "Feret Diameter";
    public static final String LBL_CONVEXITY = "Convexity";
    public static final String LBL_MAX = STATS_PIXEL_MAX;
    
    public static final String COUNT = "Count";
    
    public final static String[] STATS_RELATED_METADATA = new String[] { STATS_PIXEL_MIN, STATS_PIXEL_MAX, STATS_PIXEL_MEAN, STATS_PIXEL_STD_DEV };
    
    public final static MetaData NULL = new GenericMetaData("", null);
    
    public static boolean notNull(MetaData data) {
        return data.isNull() == false;
    }
    
    public static boolean isNull(MetaData data) {
        return data.isNull();
    }
    
    public static boolean canDisplay(String metadataKey) {
        return metadataKey.startsWith("*") == false;
    }
    public static boolean canDisplay(MetaData m) {
        return canDisplay(m.getName());
    }
    
    public default String str() {
        return String.format("[MetaData Type = %s] %s = %s",(getType() == MetaData.TYPE_STRING ? "String" : "Number"),getName(),getStringValue());
    }
    
   
    // addedd an other useful comment
}
