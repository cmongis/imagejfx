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

import ijfx.plugins.segmentation.neural_network.INN;
import ijfx.plugins.segmentation.neural_network.LSTM;
import ijfx.plugins.segmentation.neural_network.NNType;
import ijfx.service.ImagePlaneService;
import ijfx.service.overlay.OverlayDrawingService;
import ijfx.service.overlay.OverlayShapeStatistics;
import ijfx.service.overlay.OverlayStatService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.Overlay;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.ui.UIService;

/**
 *
 * @author Pierre BONNEAU
 */
@Plugin(type = Service.class)
public class SegmentationService extends AbstractService implements ImageJService{

    private final double COLOR = 1.0;

    /**
     * Width of the membrane pattern
     */
    private final Property<Integer> membraneWidth = new SimpleObjectProperty<>(0);
    
    private List<ProfilesSet> trainingData;
    private List<List<int[]>> confirmationSet;
    private List<Dataset> imgDatasets;
    
    private INN nn;
    
    private Property<NNType> nnType =  new SimpleObjectProperty<>();
    
    @Parameter
    OverlayService overlayService;
    
    @Parameter
    OverlayStatService overlayStatService;
    
    @Parameter
    OverlayDrawingService overlayDrawingService;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    ImagePlaneService imagePlaneService;
    
    @Parameter
    Context context;
    
    @Parameter
    UIService uIService;
    
    @Parameter
    UIService uis;
    
    /**
     * Generate the different intensity profiles from drawn objects for each ImageDisplay
     */
    public void generateTrainingSet(){
        
        trainingData = new ArrayList<>();
        confirmationSet = new ArrayList<>();
        
        double maxDiameter = 0.0;
        
        List<ImageDisplay> displays = imageDisplayService.getImageDisplays();
        imgDatasets = new ArrayList<>(displays.size());
        List<Dataset> labeledDs = new ArrayList<>(displays.size());
        List<List<Overlay>> overlaySet = new ArrayList<>();

        displays.stream().forEach(id -> {
            imgDatasets.add(imageDisplayService.getActiveDataset(id));
            overlaySet.add(overlayService.getOverlays(id));
        });
        
        for(int ds = 0; ds < imgDatasets.size(); ds++){
            
            labeledDs.add(imagePlaneService.createEmptyPlaneDataset(imgDatasets.get(ds)));
            ArrayList<Point2D> centers = new ArrayList<>();
            
            for(Overlay o : overlaySet.get(ds)){
                
                OverlayShapeStatistics stats = overlayStatService.getShapeStatistics(o);
                centers.add(stats.getCenterOfGravity());
                
                if(stats.getFeretDiameter() > maxDiameter)
                    maxDiameter = stats.getFeretDiameter();
                
                overlayDrawingService.drawOverlay(o, OverlayDrawingService.OUTLINER, labeledDs.get(ds), COLOR);
                
            }
            uis.show(labeledDs.get(ds));
            ProfilesSet profiles = new DefaultProfilesSet(centers, (int)maxDiameter, context);
            generateConfirmationSet(profiles, labeledDs.get(ds));
            trainingData.add(profiles);
        }
    }
    
    /**
     * Generate the corresponding sequences of labels (0, 1) for a set of profiles, depending on if the pixel belongs to the membrane or not
     * 
     * @param trainingSet Set of intensity profiles extracted from an image, for the training of the neural network and for that we need to label.
     * @param labeledDs Dataset where pixels belonging to membranes have been drawn.
     */
    public void generateConfirmationSet(ProfilesSet trainingSet, Dataset labeledDs){
        
        RandomAccess<RealType<?>> randomAccess = labeledDs.randomAccess();
        
        List<int[]> labeledProfiles = new ArrayList<>(trainingSet.getProfiles().size());
        
        for(int i =  0; i < trainingSet.getProfiles().size(); i++){
            
            List<int[]> profile = trainingSet.getProfiles().get(i);
            int[] labels = new int[trainingSet.getMaxLenght()];
            Arrays.fill(labels, 0);
            
            for(int j = 0; j < profile.size(); j++){
                
                int[] point = profile.get(j);
                
                randomAccess.setPosition(point[0], 0);
                randomAccess.setPosition(point[1], 1);
                
                double pixelValue = randomAccess.get().getRealDouble();
                
                if(pixelValue == COLOR){
                    
                    for(int k = j-membraneWidth.getValue()/2; k <= j+membraneWidth.getValue()/2; k++){
                        if(k < 0 ||  k > labels.length)
                            continue;
                        else
                            labels[k] = 1;
                    }
                    
                    break;
                }
                labeledProfiles.add(labels);
            }
            confirmationSet.add(labeledProfiles);
        }
    }
    
    public Property<Integer> membraneWidthProperty(){
        return this.membraneWidth;
    }
    
    public List<ProfilesSet> getTrainingSet(){
        return this.trainingData;
    }
    
    public INN buildNN(NNType nnType){
        INN nn = null;
        switch (nnType){
            case LSTM : nn = new LSTM(); break;
            case BLSTM : nn = new LSTM(); break;
        }
        return nn;
    }
    
    public Property<NNType> nnType(){
        return this.nnType;
    }
    
    public void train(){
        this.nn = buildNN(nnType().getValue());
        
        DataSetIterator iter = new ProfileIterator(trainingData, confirmationSet, imgDatasets);
        
        boolean next = iter.hasNext();
        System.out.println("Fitting : DONE");
        int iEpoch = 0;
        int nEpochs = 3;
        
        while(iEpoch < nEpochs){
            System.out.printf("* = * = * = * = * = * = * = * = * = ** EPOCH %d ** = * = * = * = * = * = * = * = * = * = * = * = * = * =\n",iEpoch);

        while(iter.hasNext()){
            DataSet ds = iter.next();
            nn.train(ds);
        }
        iter.reset();
        nEpochs++;
                    System.out.printf("* = * = * = * = * = * = * = * = * = ** EPOCH COMPLETE** = * = * = * = * = * = * = * = * = * = * = * = * = * =\n",iEpoch);

        }
        System.out.println("Fitting : DONE");
    }
//    public ProfilesSet generateTestSet(){
//        return null;
//    }
//    
//    public void saveParameters(){
//        
//    }
//    
//    public void loadParameter(){
//        
//    }
//    
//    public void clearAll(){
//        
//    }
}