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
package ijfx.ui.filter.string;

import ijfx.ui.filter.StringFilter;
import ijfx.ui.utils.BaseTester;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Tuan anh TRINH
 */
public class StringFilterTester extends BaseTester {

    StringFilter stringFilter;

    public static void main(String[] args) {
        launch(args);

    }

    public StringFilterTester() {
        super();
         addAction("Update values",this::updateValues);
    }
    
    @Override
    public void initApp() {
        stringFilter = new DefaultStringFilter();
        setContent(stringFilter.getContent());
        updateValues();
    }
    
    private void updateValues() {
          final SecureRandom random = new SecureRandom();

        List<String> collect = IntStream.range(0, 30)
                .mapToObj(i -> new BigInteger(130, random).toString(32))
                .collect(Collectors.toList());
        stringFilter.setAllPossibleValues(collect);
    }

}
