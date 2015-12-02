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
package ijfx.ui.deprecated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class OrderService extends HashMap<String,Double> {
    
    
    private OrderService() {
        super();
    }
    
    protected static OrderService instance;
    
    public static OrderService getInstance() {
        if(instance == null) {
            instance = new OrderService();
        }
        return instance;
    }
    
    
    public OrderService setOrder(String id, Double order) {
        put(id, order);
        return this;
    }
    
    public OrderService setOrder(Node node, Double order) {
         return setOrder(node.getId(),order);
    }
    
    public Double getOrder(String id) {
        if(containsKey(id)== false) {
            put(id,1.0);
        }
        return get(id);
    }
    
    public Collection<Node> orderNode(Collection<Node> nodeList) {
        
        ArrayList<Node> sortedNodes = new ArrayList<>(nodeList);
        
        Collections.sort(sortedNodes,(n1,n2)->getOrder(n1.getId()).compareTo(getOrder(n2.getId())));
       
        return sortedNodes;
    }
    
    public void sortChildren(ObservableList<Node> nodes) {
        Collections.sort(nodes,(n1,n2)->getOrder(n1.getId()).compareTo(getOrder(n2.getId())));
    }
    
    
    
}
