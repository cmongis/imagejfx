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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.commons.math3.analysis.function.Tanh;
import org.apache.commons.math3.random.RandomDataGenerator;

/**
 *
 * @author Pierre BONNEAU
 */
public class LSTMRnn implements NeuralNet{
    
    public int inputs;
    public int hidden;
    public int outputs;
    
    public List<List<double[]>> u;
    public List<List<double[]>> w;
    public List<double[]> p;
    
    public double[] cell_states;
    public double[] cell_outputs;
    
    public final int GATES = 4;
    public final int PEEPHOLES = 3;

    Sigmoid sigmoid;
    Tanh tanh;
    
    public LSTMRnn(int inputs, int hidden, int outputs){
        this.inputs = inputs;
        this.hidden = hidden;
        this.outputs = outputs;
        
        /*four matrices for the four gates: input, forget, output, cell state*/
        this.u = new ArrayList<>(GATES);
        this.w = new ArrayList<>(GATES);
        
        /*matrices of weights for the peephole connections (to the input, forget, output gates only)*/
        this.p = new ArrayList<>(PEEPHOLES);
        
        for(int i = 0; i < GATES; i++){
            u.add(new ArrayList<>(this.inputs));
            w.add(new ArrayList<>(this.hidden));
        }
        
        this.cell_states = new double[this.hidden];
        this.cell_outputs = new double[this.hidden];
        
        sigmoid = new Sigmoid();
        tanh = new Tanh();
    }
    
    @Override
    public void forwardProp(List<List<Double>> inputList){
        
        double[] newStates = new double[this.hidden];
        double[] newOutputs = new double[this.hidden];
        
        Iterator<List<Double>> it_seq = inputList.iterator();
        
        while(it_seq.hasNext()){
            List<Double> inputs = it_seq.next();
            for(int c = 0; c < this.hidden; c++){
                
                /*weighted external input for input, forget and output gates*/
                double ig_inputWeight = 0.0;
                double fg_inputWeight = 0.0;
                double og_inputWeight = 0.0;
                double cell_inputWeight = 0.0;
                
                for(int i = 0; i < this.inputs; i++){
                    
                    Double in = inputs.get(i);
                    
                    ig_inputWeight += u.get(0).get(i)[c] * in;
                    fg_inputWeight += u.get(1).get(i)[c] * in;
                    og_inputWeight += u.get(2).get(i)[c] * in;
                    cell_inputWeight += u.get(3).get(i)[c] * in;
                }
                /*weighted t-1 cell outputs*/
                double ig_outputWeight = 0.0;
                double fg_outputWeight = 0.0;
                double og_outputWeight = 0.0;
                double cell_outputWeight = 0.0;
                
                for(int j = 0; j < this.hidden; j++){
                    ig_outputWeight += w.get(0).get(j)[c] * cell_outputs[c];
                    fg_outputWeight += w.get(1).get(j)[c] * cell_outputs[c];
                    og_outputWeight += w.get(2).get(j)[c] * cell_outputs[c];
                    cell_outputWeight += w.get(3).get(j)[c] * cell_outputs[c];
                }
                
                /*peephole connections*/
                double ig_peephole = p.get(0)[c] * cell_states[c];
                double fg_peephole = p.get(1)[c] * cell_states[c];

                
                double inputGate = sig(ig_inputWeight + ig_outputWeight + ig_peephole);
                double forgetGate = sig(fg_inputWeight + fg_outputWeight + fg_peephole);
                
                /*external input gate*/
                double eig = tanh(cell_inputWeight + cell_outputWeight);
                
                /*update the cell state with the new values*/
                newStates[c] = this.cell_states[c]*forgetGate + eig*inputGate;
                
                double og_peephole = p.get(2)[c] * newStates[c];
                
                double outputGate = sig(og_inputWeight + og_outputWeight + og_peephole);                
                
                /*output of the cell*/
                newOutputs[c] = tanh(newStates[c])*outputGate;
            }
            this.cell_states = newStates;
            this.cell_outputs = newOutputs;
        }
    }
    
    public void backwardProp(){
        
    }
    
    public void train(){
        
    }
    
    @Override
    public void initialize(){
        
        /*initialize the cells state and outputs to 0*/
        for(int s = 0; s < this.hidden; s++){
            cell_states[s] = 0.0;
            cell_outputs[s] = 0.0;
        }
        
        System.out.println("Cells states initialized");
        
        RandomDataGenerator generator = new RandomDataGenerator();
        int lower = -1;
        int upper = 1;
        
        /*initialize the input weight matrices u (0 to 3, respectively input, forget, output, cell gates)*/
        for(int i = 0; i < this.inputs; i++){
            
            double[] weightsU0 = new double[this.hidden];
            double[] weightsU1 = new double[this.hidden];
            double[] weightsU2 = new double[this.hidden];
            double[] weightsU3 = new double[this.hidden];
        
            for(int j = 0; j < this.hidden; j++){
                
                double rnd0 = generator.nextUniform(lower, upper);
                double rnd1 = generator.nextUniform(lower, upper);
                double rnd2 = generator.nextUniform(lower, upper);
                double rnd3 = generator.nextUniform(lower, upper);
                                
                weightsU0[j] = rnd0;
                weightsU1[j] = rnd1;
                weightsU2[j] = rnd2;
                weightsU3[j] = rnd3;
            }
            
            this.u.get(0).add(weightsU0);
            this.u.get(1).add(weightsU1);
            this.u.get(2).add(weightsU2);
            this.u.get(3).add(weightsU3);
        }
        
        System.out.println("Input weights matrices initialized");
        
        /*initialize the recurrent weight matrices w (0 to 3, respectively input, forget, output, cell gates)*/        
        for(int k = 0; k < this.hidden; k++){
            
            double[] weightsW0 = new double[this.hidden];
            double[] weightsW1 = new double[this.hidden];
            double[] weightsW2 = new double[this.hidden];
            double[] weightsW3 = new double[this.hidden];
            
            for(int l = 0; l < this.hidden; l++){
                
                double rnd0 = generator.nextUniform(lower, upper);
                double rnd1 = generator.nextUniform(lower, upper);
                double rnd2 = generator.nextUniform(lower, upper);
                double rnd3 = generator.nextUniform(lower, upper);
                
                weightsW0[l] = rnd0;
                weightsW1[l] = rnd1;
                weightsW2[l] = rnd2;
                weightsW3[l] = rnd3;
            }
            this.w.get(0).add(weightsW0);
            this.w.get(1).add(weightsW1);
            this.w.get(2).add(weightsW2);
            this.w.get(3).add(weightsW3);
        }
        
        System.out.println("Recurrent weights matrices initialized");
        
        /*initialize the peephole connections weight matrices p (0 to 2, respectively input, forget, output gates)*/
        double[] weightsP0 = new double[this.hidden];
        double[] weightsP1 = new double[this.hidden];
        double[] weightsP2 = new double[this.hidden];
        
        for(int n = 0; n < this.hidden; n++){
            
            double rnd0 = generator.nextUniform(lower, upper);
            double rnd1 = generator.nextUniform(lower, upper);
            double rnd2 = generator.nextUniform(lower, upper);
            
            weightsP0[n] = rnd0;
            weightsP1[n] = rnd1;
            weightsP2[n] = rnd2;
        }
        
        this.p.add(0, weightsP0);
        this.p.add(1, weightsP0);
        this.p.add(2, weightsP1);
        
        System.out.println("Peephole weights matrices initialized");
    }
    
    public double sig(double x){
        return this.sigmoid.value(x);
    }
    
    public double tanh(double x){
        return this.tanh.value(x);
    }
}
