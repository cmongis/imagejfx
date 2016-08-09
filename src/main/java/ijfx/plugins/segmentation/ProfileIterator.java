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
    private final int MINIBATCH_SIZE = 50;
    
    private List<ProfilesSet> allProfilesSet;
    private List<List<int[]>> labelsSet;
    private List<Dataset> imgDatasets;
    
    boolean train;
    
    private int currDatasetIdx;
    private int currProfileIdx;
    private int profileLength;
    
    public ProfileIterator(List<ProfilesSet> allProfilesSet, List<List<int[]>> labelsSet, List<Dataset> imgDatasets, boolean train){
        this.allProfilesSet = allProfilesSet;
        this.labelsSet = labelsSet;
        this.imgDatasets = imgDatasets;
        
        this.train = train;
        
        currDatasetIdx = 0;
        currProfileIdx = 0;
        profileLength = 0;
    }
    
    public ProfileIterator(List<ProfilesSet> allProfilesSet, List<Dataset> imgDatasets){
        this(allProfilesSet, null, imgDatasets, false);        
    }

    @Override
    public boolean hasNext() {
        return allProfilesSet.size() - currDatasetIdx > 0;
    }

    @Override
    public DataSet next() {
        return next(currDatasetIdx);
    }
    @Override
    public DataSet next(int num) {
        ProfilesSet profiles = allProfilesSet.get(num);
        
        int startIdx = currProfileIdx;
        int endIdx;
        int currMinibatchSize;
        if(profiles.size() - currProfileIdx >= MINIBATCH_SIZE){
            endIdx = currProfileIdx + MINIBATCH_SIZE;
            currMinibatchSize = MINIBATCH_SIZE;
        }
        else{
            endIdx = profiles.size();
            currMinibatchSize = profiles.size() - currProfileIdx;
            currDatasetIdx++;
        }
            
        List<double[]> intensities = profiles.getPointsAsIntensities(imgDatasets.get(num), startIdx, endIdx);
        
        profileLength = profiles.getMaxLenght();
        
        INDArray input = Nd4j.create(new int[]{currMinibatchSize, NUM_CLASSES, profileLength}, 'f');
        INDArray labels = Nd4j.create(new int[]{currMinibatchSize, NUM_CLASSES, profileLength}, 'f');
        INDArray masks = Nd4j.create(new int[]{currMinibatchSize, profileLength}, 'f');        

        int c = 0;
        if(train){
            for(int i = currProfileIdx; i < currProfileIdx+currMinibatchSize; i++, c++){
                double[] p = intensities.get(c);
                int[] l = labelsSet.get(num).get(i);
                int[] m = profiles.getMasks().get(i);
                
                for(int j = 0; j < profileLength; j++){
                    input.putScalar(new int[]{c, 0, j}, p[j]);
                    labels.putScalar(new int[]{c, 0, j}, l[j]);
                    masks.putScalar(new int[]{c, j}, m[j]);
                }
            }
        }
        else{
            for(int i = currProfileIdx; i < currProfileIdx+currMinibatchSize; i++, c++){
                double[] p = intensities.get(c);
                int[] m = profiles.getMasks().get(i);
                
                for(int j = 0; j < profileLength; j++){
                    input.putScalar(new int[]{c, 0, j}, p[j]);
                    masks.putScalar(new int[]{c, j}, m[j]);
                }
            }
        }
        
        currProfileIdx = endIdx;
        
        return new DataSet(input, labels, masks, masks);
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
        currDatasetIdx = 0;
        currProfileIdx = 0;
    }

    @Override
    public int batch() {
        return MINIBATCH_SIZE;
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
