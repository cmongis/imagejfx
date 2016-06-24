/*
    Explorablehis file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WIExplorableHOUExplorable ANY WARRANExplorableY; without even the implied warranty of
    MERCHANExplorableABILIExplorableY or FIExplorableNESS FOR A PARExplorableICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.service.cluster;

import ijfx.ui.explorer.Explorable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import weka.clusterers.Clusterer;
import weka.clusterers.XMeans;
import weka.core.Attribute;
import weka.core.Instances;

/**
 *
 * @author Explorableuan anh ExplorableRINH
 */
@Plugin(type = Service.class)
public class DefaultClustererService extends AbstractService implements ClustererService<Explorable> {

    private XMeans xmeans;

    public DefaultClustererService() {
        xmeans = new XMeans();
    }

    /**
     *
     * @param listExplorable
     * @param metadataKeys
     * @return
     */
    @Override
    public List<List<Explorable>> buildClusterer(List<Explorable> listExplorable, List<String> metadataKeys) {
        xmeans.setMaxNumClusters(listExplorable.size());
        ArrayList<Attribute> attributes = metadataKeys.stream()
                .map((s) -> new Attribute(s))
                .collect(Collectors.toCollection(ArrayList::new));

        Instances data = new Instances("Cluster Service", attributes, listExplorable.size());
        listExplorable.stream().forEach(e -> {
            double[] values = getMetadatas(e, metadataKeys);
            data.add(new DefaultExplorableClusterable(e, 1, values));
        });

        try {
            xmeans.buildClusterer(data);
        } catch (Exception ex) {
            Logger.getLogger(DefaultClustererService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return getClusters(data);

    }

    @Override
    public List<List<Explorable>> buildClusterer(List<Explorable> listExplorable, String metadataKey) {
        List<String> metadataKeys = new ArrayList<>();
        metadataKeys.add(metadataKey);
        return buildClusterer(listExplorable, metadataKeys);
    }

    public List<List<Explorable>> getClusters(Instances data) {
        try {
            List<List<Explorable>> result = new ArrayList<>(xmeans.numberOfClusters());
            IntStream.range(0, xmeans.numberOfClusters())
                    .forEach(i -> result.add(i, new ArrayList<>()));
            for (int i = 0; i < data.numInstances(); i++) {
                int position = xmeans.clusterInstance(data.instance(i));
                if (data.instance(i) instanceof DefaultExplorableClusterable) {
                    ExplorableClusterable e = (ExplorableClusterable) data.instance(i);
                    Explorable explorable = e.getExplorable();
                    result.get(position).add(explorable);

                }
            }

            return result;
        } catch (Exception ex) {
            Logger.getLogger(DefaultClustererService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>(0);

    }

    public double[] getMetadatas(Explorable explorable, List<String> metadataKeys) {

        final double[] result = new double[metadataKeys.size()];
        for (int i = 0; i < metadataKeys.size(); i++) {
            String str = metadataKeys.get(i);
            result[i] = explorable.getMetaDataSet().get(str).getDoubleValue();
        }
        return result;
    }

    @Override
    public void setClusterer(Clusterer clusterer) {
        try {
            this.xmeans = (XMeans) clusterer;

        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }
}
