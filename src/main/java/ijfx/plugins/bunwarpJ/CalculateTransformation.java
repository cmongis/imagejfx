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

import ij.ImagePlus;
import ijfx.plugins.adapter.IJ1Service;
import java.awt.Point;
import java.io.File;
import java.util.Stack;
import net.imagej.Dataset;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class)

public class CalculateTransformation implements Command {

    @Parameter(type = ItemIO.INPUT)
    Dataset sourceDataset;

    @Parameter(type = ItemIO.INPUT)
    Dataset targetDataset;

    @Parameter(label = "Landmarks File", persist = false)
    File landmarksFile = null;

    @Parameter
    private IJ1Service iJ1Service;

    @Parameter(type = ItemIO.OUTPUT)
    bunwarpj.Transformation transformation;
    /**
     * Image representation for source image
     */
    private ImagePlus sourceImp;
    /**
     * Image representation for target image
     */
    private ImagePlus targetImp;

    @Parameter
    bunwarpj.Param parameter;
    
    @Override
    public void run() {

        this.sourceImp = iJ1Service.getInput(sourceDataset).duplicate();
        this.targetImp = iJ1Service.getInput(targetDataset);
        Stack<Point> sourceStack = new Stack<>();
        Stack<Point> targetStack = new Stack<>();
        bunwarpj.MiscTools.loadPoints(landmarksFile.getAbsolutePath(), sourceStack, targetStack);

        transformation = bunwarpj.bUnwarpJ_.computeTransformationBatch(sourceImp.getWidth(), sourceImp.getHeight(), targetImp.getWidth(), targetImp.getHeight(), sourceStack, targetStack, parameter);

    }

}
