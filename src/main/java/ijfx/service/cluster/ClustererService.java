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

import ijfx.ui.explorer.ObjectWrapper;
import java.util.List;
import net.imagej.ImageJService;
import weka.clusterers.Clusterer;

/**
 *
 * @author Tuan anh TRINH
 * @param <S>
 * @param <T>
 */
public interface ClustererService<S,T> extends ImageJService {

    public List<List<S>> buildClusterer(List<T> objectWrappers , List<String> attributsString);

    public  List<List<S>> buildClusterer(List<T> listExplorable, String metadataKey);
    
    public void setClusterer(Clusterer clusterer);
}
