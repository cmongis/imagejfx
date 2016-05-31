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
package ijfx.plugins.bunwarpJ;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ijfx.plugins.adapter.AbstractImageJ1PluginAdapter;
import static ijfx.plugins.bunwarpJ.bUnwarpJ_.modesArray;
import static ijfx.plugins.bunwarpJ.bUnwarpJ_.sMinScaleDeformationChoices;
import java.util.Arrays;
import net.imagej.Dataset;
import net.imagej.app.AboutImageJ;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Plugins>Pipeline")
public class Pipeline extends AbstractImageJ1PluginAdapter implements Command {

    public static String[] modesArray = {"Fast", "Accurate", "Mono"};
    public static String[] sMinScaleDeformationChoices = {"Very Coarse", "Coarse", "Fine", "Very Fine"};
    public static String[] sMaxScaleDeformationChoices = {"Very Coarse", "Coarse", "Fine", "Very Fine", "Super Fine"};
    @Parameter
    CommandService commandService;

    @Parameter
    ModuleService moduleService;
    @Parameter
    Dataset sourceDataset;
    @Parameter
    Dataset targetDataset;

    ImagePlus targetImp;
    ImagePlus sourceImp;
    ImageProcessor targetMskIP = null;
    ImageProcessor sourceMskIP = null;

    @Parameter(choices = {"Fast", "Accurate", "Mono"})
    String modeChoice;
    @Parameter (choices = {"0","1","2","3","4","5","6","7"})
    String img_subsamp_fact;
    /**
     * minimum scale deformation
     */
    @Parameter(choices = {"Very Coarse", "Coarse", "Fine", "Very Fine"})
    private String min_scale_deformation_choice;
    /**
     * maximum scale deformation
     */
    @Parameter(choices = {"Very Coarse", "Coarse", "Fine", "Very Fine", "Super Fine"})
    private String max_scale_deformation_choice;
    @Parameter
    double divWeight;
    @Parameter
    double curlWeight;
    @Parameter
    double landmarkWeight;
    @Parameter
    double imageWeight = 1.0;
    @Parameter
    double consistencyWeight = 10.0;
    @Parameter
    double stopThreshold = 0.01;

    @Override
    public void run() {
        ImagePlus[] imageList = new ImagePlus[]{sourceImp, targetImp};
        this.sourceImp = getInput(sourceDataset);
        this.targetImp = getInput(targetDataset);
        int mode = Arrays.asList(modesArray).indexOf(modeChoice);
        int max_scale_deformation = Arrays.asList(sMaxScaleDeformationChoices).indexOf(max_scale_deformation_choice);
        int min_scale_deformation = Arrays.asList(sMinScaleDeformationChoices).indexOf(min_scale_deformation_choice);
        Transformation transformation = bUnwarpJ_.computeTransformationBatch(targetImp, sourceImp, targetMskIP, sourceMskIP, mode, Integer.parseInt(img_subsamp_fact), min_scale_deformation, max_scale_deformation, divWeight, curlWeight, landmarkWeight, imageWeight, consistencyWeight, stopThreshold);
        MainDialog dialog = new MainDialog(imageList, mode, max_scale_deformation, min_scale_deformation, max_scale_deformation, divWeight, curlWeight, landmarkWeight, imageWeight, consistencyWeight, stopThreshold, false, false, sourceImp, targetImp, modeChoice);
        final ModuleInfo myInfo = commandService.getCommand(bUnwarpJ_.class);
        moduleService.run(myInfo, true);

    }

    @Override
    public ImagePlus processImagePlus(ImagePlus input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
