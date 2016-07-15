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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.Instances;

/**
 *
 * @author Tuan anh TRINH
 */
public class PCAProcesser {

    private StringProperty xAxeProperty;
    private StringProperty yAxeProperty;

    public PCAProcesser() {
        xAxeProperty = new SimpleStringProperty();
        yAxeProperty = new SimpleStringProperty();
    }

    public List<ObjectClusterable> applyPCA(List<ObjectClusterable> objectClusterables, List<String> attributsString) throws Exception {

        ArrayList<Attribute> attributes = attributsString.stream().map(e -> new Attribute(e)).collect(Collectors.toCollection(ArrayList::new));
        Arrays.toString(attributes.toArray());
        Instances data = new Instances("Cluster Service", attributes, objectClusterables.size());

        data.addAll(objectClusterables);
        return mapToObjectClusterable(applyPCAtoInstances(data));
    }

    public Instances applyPCAtoInstances(Instances data) throws Exception {
        Ranker ranker = new Ranker();
        AttributeSelection selector = new AttributeSelection();
        selector.SelectAttributes(data);
        selector.setSearch(ranker);
        CustomPrincipalComponents pca = new CustomPrincipalComponents();
        selector.setEvaluator(new InfoGainAttributeEval());
        selector.setSearch(ranker);
        pca.buildEvaluator(data);
        Instances instances = pca.transformedData(data);
        setxAxe(instances.attribute(0).name());
        setyAxe(instances.attribute(1).name());

        return instances;
    }
    
    public List<ObjectClusterable> mapToObjectClusterable(Instances instances){
        return instances
                .stream()
                .map(e -> new DefaultObjectClusterable(((DefaultObjectClusterable) e).getObject(), 1, e.toDoubleArray()))
                .collect(Collectors.toList());
    }

    public StringProperty xAxeProperty() {
        return xAxeProperty;
    }

    public StringProperty yAxeProperty() {
        return yAxeProperty;
    }

    public void setxAxe(String s) {
        xAxeProperty.set(s);
    }

    public void setyAxe(String s) {
        yAxeProperty.set(s);
    }
}
