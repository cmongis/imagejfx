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
package ijfx.plugins.segmentation;

import java.util.List;
import net.imagej.Dataset;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author Pierre BONNEAU
 */
public class ProfileIterator implements DataSetIterator{
    
    private final int NUM_CLASSES = 1;
    
    private List<ProfilesSet> allProfilesSet;
    private List<List<int[]>> labelsSet;
    private List<Dataset> imgDatasets;

    private int currentDatasetIdx;
    private int profileLength;
    
    public ProfileIterator(List<ProfilesSet> allProfilesSet, List<List<int[]>> labelsSet, List<Dataset> imgDatasets){
        
        this.allProfilesSet = allProfilesSet;
        this.labelsSet = labelsSet;
        this.imgDatasets = imgDatasets;
        
        currentDatasetIdx = 0;
        profileLength = 0;
    }

    @Override
    public boolean hasNext() {
        return currentDatasetIdx == allProfilesSet.size();
    }

    @Override
    public DataSet next() {
        return next(currentDatasetIdx);
    }
    @Override
    public DataSet next(int num) {
        ProfilesSet profiles = allProfilesSet.get(num);
        List<double[]> intensities = profiles.getPointsAsIntensities(imgDatasets.get(num));
        
        INDArray input = Nd4j.create(new int[]{profiles.size(), NUM_CLASSES, profileLength}, 'f');
        INDArray labels = Nd4j.create(new int[]{profiles.size(), NUM_CLASSES, profileLength}, 'f');
        
        for(int i = 0; i < profiles.size(); i++){
            
            double[] p = intensities.get(i);
            int[] l = labelsSet.get(num).get(i);
            
            for(int j = 0; j < profileLength; j++){
                input.putScalar(new int[]{i, 0, j}, p[j]);
                labels.putScalar(new int[]{i, 0, j}, l[j]);
            }
        }
        
        currentDatasetIdx++;
        
        return new DataSet(input, labels);
    }

    @Override
    public int totalExamples() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int inputColumns() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int totalOutcomes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int batch() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int cursor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int numExamples() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getLabels() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
