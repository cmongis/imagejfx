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

import ijfx.plugins.commands.BinaryToOverlay;
import ijfx.plugins.segmentation.neural_network.INN;
import ijfx.plugins.segmentation.neural_network.LSTM;
import ijfx.plugins.segmentation.neural_network.NNType;
import ijfx.service.ImagePlaneService;
import ijfx.service.overlay.OverlayDrawingService;
import ijfx.service.overlay.OverlayShapeStatistics;
import ijfx.service.overlay.OverlayStatService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;

import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.event.OverlayCreatedEvent;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PointOverlay;

import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.util.ModelSerializer;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.event.EventService;
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
public class MLSegmentationService extends AbstractService implements ImageJService{

    private final double COLOR = 1.0;

    /**
     * Width of the membrane pattern
     */
    private Property<Integer> membraneWidth = new SimpleObjectProperty<>(0);
    private Property<Integer> searchRadius = new SimpleObjectProperty<>(40);
    
    private List<ProfilesSet> testData = new ArrayList<>();
    private List<ProfilesSet> trainingData = new ArrayList<>();
    private List<List<int[]>> confirmationSet = new ArrayList<>();
    private List<Dataset> imgDatasets;
    
    private INN net;
    
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
    
    @Parameter
    EventService eventService;
    
    /**
     * Generate the different intensity profiles from drawn objects for each ImageDisplay
     */
    public void generateTrainingSet(){
        
        double maxDiameter = 0.0;
        
        List<ImageDisplay> displays = imageDisplayService.getImageDisplays();
        imgDatasets = new ArrayList<>(displays.size());
        List<Dataset> labeledDs = new ArrayList<>(displays.size());
        List<List<Overlay>> overlaySet = new ArrayList<>();

        displays.stream().forEach(id -> {
            imgDatasets.add(imageDisplayService.getActiveDataset(id));
            overlaySet.add(overlayService.getOverlays(id));
        });
        
        int numProfiles = 0;
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
//            uis.show(labeledDs.get(ds));
            ProfilesSet profiles = new DefaultProfilesSet(centers, (int)maxDiameter, context);
            generateConfirmationSet(profiles, labeledDs.get(ds));
            numProfiles += profiles.size();
            trainingData.add(profiles);
        }
        eventService.publish(new ProfileEvent(ProfileType.TRAIN, numProfiles));
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
    
    public void generateTestSet(){
        
        List<ImageDisplay> displays = imageDisplayService.getImageDisplays();
        imgDatasets = new ArrayList<>(displays.size());
        List<Dataset> labeledDs = new ArrayList<>(displays.size());
        List<List<Overlay>> overlaySet = new ArrayList<>();

        displays.stream().forEach(id -> {
            imgDatasets.add(imageDisplayService.getActiveDataset(id));
            overlaySet.add(overlayService.getOverlays(id));
        });
        
        List<List<Point2D>> seeds = seeding();
        int numProfiles = 0;
        for(int ds = 0; ds < imgDatasets.size(); ds++){
            
            List<Point2D> centers = seeds.get(ds);

            ProfilesSet profiles = new DefaultProfilesSet(centers, searchRadius.getValue(), context);
            numProfiles += profiles.size();
            testData.add(profiles);
        }
        eventService.publish(new ProfileEvent(ProfileType.TEST, numProfiles));
    }
    
    public void initModel(){
        setModel(buildNN(NNType.LSTM));
    }
    
    public INN buildNN(NNType nnType){
        INN nn;
        switch (nnType){
            case LSTM : nn = new LSTM(); break;
            case BLSTM : nn = new LSTM(); break;
            default:throw new AssertionError(nnType.name());
        }
        return nn;
    }
    
    public Property<NNType> nnType(){
        return this.nnType;
    }
    
    public void train(){        
        DataSetIterator iter = new ProfileIterator(trainingData, confirmationSet, imgDatasets, true);
        int iEpoch = 0;
        int nEpochs = 50;
        
        while(iEpoch < nEpochs){
            System.out.printf("EPOCH %d\n",iEpoch);

            Evaluation eval = new Evaluation();
            while(iter.hasNext()){
                DataSet ds = iter.next();
                net.train(ds);
                
                INDArray predict2 = net.output(ds.getFeatureMatrix());
                INDArray labels2 = ds.getLabels();
//                eval.evalTimeSeries(labels2, predict2);                
            }

            iter.reset();
//            System.out.println(eval.stats());
            iEpoch++;
        }
        System.out.println("Fitting : DONE");
    }
    
    public List<List<Point2D>> seeding(){
        //TODO seeding algorithm. Temp manual alternative
        
        List<List<Point2D>> seeds = new ArrayList();
        
        List<ImageDisplay> displays = imageDisplayService.getImageDisplays();
        imgDatasets = new ArrayList<>(displays.size());
        List<List<Overlay>> overlaySet = new ArrayList<>();
        
        displays.stream().forEach(id -> {
            imgDatasets.add(imageDisplayService.getActiveDataset(id));
            overlaySet.add(overlayService.getOverlays(id));
        });
        
        for(int ds = 0; ds < imgDatasets.size(); ds++){
            List<Point2D> currSeeds = new ArrayList();
            
            for(Overlay o : overlaySet.get(ds)){
                if(o instanceof PointOverlay){
                    double[]point = ((PointOverlay) o).getPoints().get(0);
                    Point2D point2D = new Point2D(point[0], point[1]);
                    currSeeds.add(point2D);
                }
            }
            seeds.add(currSeeds);
        }
        return seeds;
    }
    
    public INDArray classify(){
        DataSetIterator iter = new ProfileIterator(testData, imgDatasets);
        INDArray predict = net.output(iter);
        
//        int size0 = predict.size(0);
//        int size1 = predict.size(1);
//        int size2 = predict.size(2);
//        INDArray element = predict.getRow(0);
//        System.out.println("predict DONE");
        return predict;
    }

    public INDArray dummyClassification(){
        DataSetIterator iter = new ProfileIterator(testData, imgDatasets);
        INDArray predict = net.output(iter);
        INDArray dummy = Nd4j.create(new int[]{predict.size(0), predict.size(1), predict.size(2)}, 'f');
        for(int i = 0; i < predict.size(0); i++){
            for(int j  = 0; j < predict.size(2); j++){
                if(j < 10 || j > 14)
                    dummy.putScalar(new int[]{i, 0, j}, 0.0);
                else
                    dummy.putScalar(new int[]{i, 0, j}, 1.0);
            }
        }
        return dummy;
    }
    
    public void segment(){
        
    }
    
    public void dummySegmentation(){
        
        List<Dataset> predictDataset = new ArrayList<>(imgDatasets.size());
        List<List<Overlay>> overlaySet = new ArrayList<>();
        
        INDArray predict = dummyClassification();
        int currIdx = 0;
        
        for(int ds = 0; ds < imgDatasets.size(); ds++){
            List<List<int[]>> profilesSet = testData.get(ds).getProfiles();
            
            predictDataset.add(imagePlaneService.createEmptyPlaneDataset(imgDatasets.get(ds)));
            
            Dataset currDataset = predictDataset.get(ds);
            
            RandomAccess<RealType<?>> randomAccess = currDataset.randomAccess();
                    
            for(List<int[]> p : profilesSet){
                for(int i = 0; i < p.size(); i++){
                    randomAccess.setPosition(p.get(i)[0], 0);
                    randomAccess.setPosition(p.get(i)[1], 1);
                    
                    double value = predict.getRow(currIdx).getDouble(i);
                    if(value != 0)
                        randomAccess.get().setReal(value);
                }
            }
            Overlay[] overlays = BinaryToOverlay.transform(context, currDataset, true);
//            List<Overlay> overlayList = Arrays.asList(BinaryToOverlay.transform(context, currDataset, true));
            overlayStatService.setRandomColor(Arrays.asList(overlays));
            ImageDisplay display = new DefaultImageDisplay();
            context.inject(display);
            display.display(currDataset);
            for(Overlay o : overlays){
                display.display(o);
            }
            uis.show(display);
        }
    }
    
    public void saveModel(){
        try{
            File tmpFile = File.createTempFile("model", null);
            ModelSerializer.writeModel(net.getNN(), tmpFile, true);
        }
        catch(IOException ioe){
        }
    }
    
    public void loadModel(File file){
        try{
            ModelSerializer.restoreMultiLayerNetwork(file);
        }
        catch(IOException ioe){
        }
    }
    
    public void clearData(){
        try{
            testData.clear();
            trainingData.clear();
            confirmationSet.clear();
            imgDatasets.clear();
        }
        catch(NullPointerException npe){
            Logger.getLogger(MLSegmentationService.class.getName()).log(Level.SEVERE, null, npe);
        }
    }
    
    public void clearAll(){
        clearData();
        try{
            net.clear();
        }
        catch(NullPointerException npe){
            Logger.getLogger(MLSegmentationService.class.getName()).log(Level.SEVERE, null, npe);
        }
    }
       
    public Property<Integer> membraneWidthProperty(){
        return this.membraneWidth;
    }
    
    public Integer membraneWidth(){
        return membraneWidth.getValue();
    }
    
    public Property<Integer> searchRadiuProperty(){
        return this.searchRadius;
    }
    
    public Integer searchRadius(){
        return searchRadius.getValue();
    }
    
    public List<ProfilesSet> getTrainingSet(){
        return this.trainingData;
    }
    
    public List<ProfilesSet> getTestSet(){
        return this.testData;
    }
    
    public INN getModel(){
        return this.net;
    }
    
    public void setModel(INN net){
        this.net = net;
    }
    
    public Integer datasetSize(List<ProfilesSet> dataset){
        int size = 0;
        for(ProfilesSet p: dataset){
            size += p.size();
        }
        return size;
    }
}