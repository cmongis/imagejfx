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
import java.util.List;
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
    public List<double[]> w;
    
    public double[] states;

    
    public LSTMRnn(int inputs, int hidden, int outputs){
        this.inputs = inputs;
        this.hidden = hidden;
        this.outputs = outputs;
        
        this.u = new ArrayList<>(4);// Because of the four gates: input, forget, output, cell state
        this.w = new ArrayList<>(1);
        
        for(int i = 0; i < 4; i++){
            u.add(new ArrayList<>());
        }
        
        this.states = new double[this.hidden];
    }
    
    public void forwardProp(){
        
    }
    
    public void backwardProp(){
        
    }
    
    public void train(){
        
    }
    
    @Override
    public void initialize(){
        
        for(int s = 0; s < states.length; s++){
            states[s] = 0.0;
        }
        
        System.out.println("Cells states initialized");
        
        RandomDataGenerator generator = new RandomDataGenerator();
        int lower = -1;
        int upper = 1;
        
        for(int i = 0; i < this.inputs; i++){
            
            double[] weights0 = new double[this.hidden];
            double[] weights1 = new double[this.hidden];
            double[] weights2 = new double[this.hidden];
            double[] weights3 = new double[this.hidden];
        
            for(int j = 0; j < this.hidden; j++){
                
                double u0 = generator.nextUniform(lower, upper);
                double u1 = generator.nextUniform(lower, upper);
                double u2 = generator.nextUniform(lower, upper);
                double u3 = generator.nextUniform(lower, upper);
                                
                weights0[j] = u0;
                weights1[j] = u1;
                weights2[j] = u2;
                weights3[j] = u3;
            }
            
            this.u.get(0).add(weights0);
            this.u.get(1).add(weights1);
            this.u.get(2).add(weights2);
            this.u.get(3).add(weights3);
        }
        
        System.out.println("Input weight matrices initialized");
        
        for(int k = 0; k < this.hidden; k++){
            double[] weights = new double[this.hidden];
            for(int l = 0; l < this.hidden; l++){
                double rnd = generator.nextUniform(lower, upper);
                weights[l] = rnd;
            }
            this.w.add(weights);
        }
        
        System.out.println("Recurrent weight matrices initialized");
    }
}
