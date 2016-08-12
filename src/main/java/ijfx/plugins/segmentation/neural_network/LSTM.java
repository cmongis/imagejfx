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
package ijfx.plugins.segmentation.neural_network;

import javafx.scene.Node;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.weights.HistogramIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;

import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
/**
 *
 * @author Pierre BONNEAU
 */
public class LSTM implements INN{
    
    private MultiLayerNetwork net;
    
    public LSTM(){
        MultiLayerConfiguration conf = configure();
        net = new MultiLayerNetwork(conf);
        net.init();
//        net.setListeners(new HistogramIterationListener(1));
//        net.setListeners(new ScoreIterationListener(1));
        //Print the  number of parameters in the network (and for each layer)
        Layer[] layers = net.getLayers();
        int totalNumParams = 0;
        for( int i=0; i<layers.length; i++ ){
            int nParams = layers[i].numParams();
//            System.out.println("Number of parameters in layer " + i + ": " + nParams);
            totalNumParams += nParams;
        }
        System.out.println("Total number of network parameters: " + totalNumParams);
    }
    
    @Override
    public MultiLayerNetwork getNN() {
        return this.net;
    }

    @Override
    public MultiLayerConfiguration configure() {
        
        //TEMPORARY CONFIGURATION VALUE
        int input = 1;
        int lstmLayerSize = 10;
        int output = 1;
        
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
                .learningRate(0.1)
                .rmsDecay(0.95)
                .seed(12345)
                .regularization(true)
			.l2(0.001)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.RMSPROP)
                .list()
                        .layer(0, new GravesLSTM.Builder().nIn(input).nOut(lstmLayerSize)
					.activation("tanh").build())
			.layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.XENT).activation("softmax")
					.nIn(lstmLayerSize).nOut(output).build())
                .backpropType(BackpropType.Standard)
                .pretrain(false)
                .backprop(true)
                .build();
        return conf;
    }

    @Override
    public void train(DataSet ds) {
        System.out.println("Fitting...");
        net.fit(ds);
    }

    @Override
    public void predict() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void load() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataSetIterator getDataSetIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    public INDArray output(INDArray input){
        return net.output(input);
    }

    @Override
    public INDArray output(DataSetIterator iter) {
        return net.output(iter);
    }

    @Override
    public Node getNode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        net = null;
    }
}
