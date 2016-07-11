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

import ijfx.plugins.segmentation.neural_network.LSTMRnn;
import ijfx.plugins.segmentation.neural_network.NeuralNet;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Pierre BONNEAU
 */

@Plugin(type = Command.class, menuPath = "Analyze>Segmentation>Test Profiles selection")
public class ProfilesCommandTester implements Command{
    
    @Parameter
    SegmentationService segmentationService;
    
    @Override
    public void run() {
        
        ProfilesSet trainingSet = segmentationService.generateTrainingSet();
        segmentationService.generateConfirmationSet(trainingSet);
        
        NeuralNet nn = new LSTMRnn(1, 4, 1);
        nn.initialize();
        System.out.println("Network initialized!");
        
    }
}