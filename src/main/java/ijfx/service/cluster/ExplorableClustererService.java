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
package ijfx.service.cluster;

import ijfx.core.listenableSystem.MetaDataSetUtils;
import ijfx.ui.explorer.Explorable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.imagej.ImageJService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import weka.core.Attribute;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Service.class)
public class ExplorableClustererService extends AbstractService implements ImageJService {

    @Parameter
    ClustererService clustererService;

    public ExplorableClustererService() {
    }

    public List<List<? extends Explorable>> clusterExplorable(List<? extends Explorable> listExplorable, List<String> metadataKeys) {
        ArrayList<Attribute> attributes = metadataKeys.stream()
                .map((s) -> new Attribute(s))
                .collect(Collectors.toCollection(ArrayList::new));

        Set<Attribute> setAttributes = new HashSet<>(attributes);
        if (setAttributes.size() != attributes.size()) {
            List<List<? extends Explorable>> result = new ArrayList<>();
            result.add(listExplorable);
            return result;
        }
        List<ObjectClusterable> objectClusterables = 
                listExplorable.stream()
                .map(e -> new DefaultObjectClusterable(e, 1, MetaDataSetUtils.getMetadatas(e, metadataKeys))).collect(Collectors.toList());

        return clustererService.buildClusterer(objectClusterables, metadataKeys);
       
    }

   
}
