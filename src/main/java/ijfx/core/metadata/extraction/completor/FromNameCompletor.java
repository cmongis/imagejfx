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
package ijfx.core.metadata.extraction.completor;

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ijfx.core.metadata.extraction.ImageFile;
import ijfx.core.metadata.extraction.MetaDataCompletor;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class FromNameCompletor implements MetaDataCompletor {

    public static final Pattern wellNamePattern = Pattern.compile("Well(\\w\\d{1,2})");
    public static final Pattern positionPattern = Pattern.compile("Point(\\d{1,4})");
    public static final Pattern sequencePatern = Pattern.compile("Seq(\\d{1,4})");
    public static final Pattern stackPattern = Pattern.compile("ZStack(\\d{1,4})");

    ArrayList<NamePattern> otherPatterns = new ArrayList<>();

    private class NamePattern {

        public String id;
        public Pattern pattern;

        public NamePattern(String id, Pattern p) {
            this.id = id;
            this.pattern = p;
        }

    }

    public FromNameCompletor() {
        addPattern("Channel", "\\-C(\\d{1})");
        addPattern("ID", "ID(\\d{1,6})");
        addPattern("ID", "[\\-\\_](\\d{1,6})[\\-\\_]");
        addPattern("Well", "^(\\w+\\d{1,2})[\\-\\_]");
        addPattern("Well", "-Well(\\w+\\d{1,2})");
        addPattern("Exposition", "\\-C\\d+\\-\\w+([\\d\\.])s");
        addPattern(MetaData.CHANNEL_NAME, "\\-C\\d+\\-(\\w+)");
        addPattern("Prefix", "^(\\w+)[^\\w^\\d]");
        addPattern("AfterPrefix", "^\\w+[^\\w^\\d](\\w+)[^\\w^\\d]");
    }

    public void addPattern(String id, String pattern) {
        otherPatterns.add(new NamePattern(id, Pattern.compile(pattern)));
    }

    public void applyOtherPatterns(final MetaDataSet set, final String fileName) {

        otherPatterns.forEach((pattern) -> {
            String value = getValue(pattern.pattern, fileName);
            if (value != null) {

                set.put(new GenericMetaData(pattern.id, value));
            }
        });

    }

    public MetaDataSet extract(ImageFile file) {
        return extract(file.getSourceFile().getName());
    }

    public MetaDataSet extract(String fileName) {
        MetaDataSet set = new MetaDataSet();

        String well = getValue(wellNamePattern, fileName);
        String position = getValue(positionPattern, fileName);
        String sequence = getValue(sequencePatern, fileName);
        String zStack = getValue(stackPattern, fileName);

        applyOtherPatterns(set, fileName);

        if (well != null) {
            set.put(new GenericMetaData(MetaData.WELL_NAME, well));

            if (zStack == null) {
                set.put(new GenericMetaData(MetaData.ZSTACK_NUMBER, "none"));
            } else {
                set.put(new GenericMetaData(MetaData.ZSTACK_NUMBER, zStack));
            }

        }
        if (position != null) {
            set.put(new GenericMetaData(MetaData.POSITION, position));
        }

        if (sequence != null) {
            set.put(new GenericMetaData(MetaData.SEQUENCE_NUMBER, sequence));
        }

        return set;
    }

    public String getValue(Pattern p, String string) {
        Matcher m = p.matcher(string);
        if (m.find()) {

            if (m.groupCount() > 0) {
                return m.group(1);
            } else {
                return null;
            }
        }
        return null;
    }

}
