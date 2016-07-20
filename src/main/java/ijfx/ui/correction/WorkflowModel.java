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
package ijfx.ui.correction;

import ij.process.ImageProcessor;
import ijfx.ui.main.ImageJFX;
import io.datafx.controller.injection.scopes.FlowScoped;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import net.imagej.Dataset;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Tuan anh TRINH
 */
@FlowScoped
public class WorkflowModel {

    @Parameter(choices = {"Very Coarse", "Coarse", "Fine", "Very Fine"})
    private String min_scale_deformation_choice;

    /**
     * maximum scale deformation
     */
    @Parameter(choices = {"Very Coarse", "Coarse", "Fine", "Very Fine", "Super Fine"})
    private String max_scale_deformation_choice;

    /**
     * algorithm mode (fast, accurate or mono)
     */
    @Parameter(choices = {"Fast", "Accurate", "Mono"})
    private String modeChoice = "Mono";
    /**
     * image subsampling factor at the highest pyramid level
     */
    @Parameter
    private int maxImageSubsamplingFactor = 0;

    // Transformation parameters
    /**
     * divergence weight
     */
    @Parameter
    private double divWeight = 0;
    /**
     * curl weight
     */
    @Parameter
    private double curlWeight = 0;
    /**
     * landmarks weight
     */
    @Parameter
    private double landmarkWeight = 1.0;
    /**
     * image similarity weight
     */
    @Parameter
    private double imageWeight = 0.0;
    /**
     * consistency weight
     */
    @Parameter
    private double consistencyWeight = 10;
    /**
     * flag for rich output (verbose option)
     */
    //@Parameter
    private boolean richOutput = true;
    /**
     * flag for save transformation option
     */
    @Parameter
    private boolean saveTransformation = false;

    /**
     * minimum image scale
     */
    @Parameter
    private int min_scale_image = 0;
    /**
     * stopping threshold
     */
    @Parameter
    private static double stopThreshold = 1e-2;

    @Parameter(choices = {"0", "1", "2", "3", "4", "5", "6", "7"})
    String img_subsamp_fact;

    @Parameter(label = "Images Folder")
    File imagesFolder;

    @Parameter(label = "Landmarks File")
    File landmarksFile;

    @Parameter(label = "Flatfield Image", required = false)
    Dataset flatfield;

    ImageProcessor targetMskIP = null;
    ImageProcessor sourceMskIP = null;
    int max_scale_deformation;
    int min_scale_deformation;
    int mode;

    @Parameter
    Context context;

    private Dataset flatField;

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    private List<File> files;

    protected final static Logger LOGGER = ImageJFX.getLogger();

    public WorkflowModel() {
        LOGGER.info("Init WorkflowModel");
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Dataset getFlatField() {
        return flatField;
    }

    public void setFlatField(Dataset flatField) {
        this.flatField = flatField;
    }

}
