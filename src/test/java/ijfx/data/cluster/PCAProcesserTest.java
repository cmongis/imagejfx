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
package ijfx.data.cluster;

import ijfx.core.project.BaseSciJavaTest;
import ijfx.service.cluster.PCAProcesser;
import org.junit.Test;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 *
 * @author Tuan anh TRINH
 */
public class PCAProcesserTest extends BaseSciJavaTest {

    @Test
    public void applyPCATest() throws Exception {
        ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource("./src/test/resources/pcaTest.arff");
        PCAProcesser pCAProcesser = new PCAProcesser();
        Instances data = dataSource.getDataSet();
        pCAProcesser.applyPCAtoInstances(data);
    }

}
