/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.core.project;

import ijfx.core.metadata.MetaData;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.project_manager.project.SimplerCounter;
import ijfx.ui.project_manager.project.TreeItemUtils;
import ijfx.ui.main.ImageJFX;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.project_manager.projectdisplay.PlaneOrMetaData;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.SCIFIO;
import io.scif.bf.BioFormatsFormat;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TreeItem;
import loci.formats.FormatTools;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJService;
import net.imagej.ImgPlusService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.DefaultAxisType;

import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.io.IOService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptService;
import org.scijava.service.AbstractService;

import org.scijava.service.Service;
import org.scijava.ui.UIService;

/**
 *
 * @auimport org.scijava.ui.UIService; thor cyril
 */
@Plugin(type = Service.class)
public class ProjectToImageJService extends AbstractService implements ImageJService {

    @Parameter
    DatasetService datasetService;

    @Parameter
    IOService ioService;

    @Parameter
    ImgPlusService imgPlusService;

    @Parameter
    ScriptService scriptService;

    private final static Logger logger = ImageJFX.getLogger();

    public Dataset convert(TreeItem<PlaneOrMetaData> root) {

        TreeItem item = root;

        // getting the deepest level
        
        logger.info("Converting root "+root.getValue().getMetaData());
        
        int deepest = TreeItemUtils.getDeepestLevel(root, 0);
        logger.info("Deepest tree level :  " + deepest);
        // the number of dimensions is equivalent to the number of level
        // in the tree + 2 for the X and Y axis
        long[] dims;
        
        // if it's a leaf directly selected, there two dimensions
        if (deepest == 0) {
            dims = new long[2];
        } // if the selected part of the tree is not a leaf,
        // the number of dimensions equals the 2 + the number
        // of node levels (we don't count the leaves level)
        else {
            dims = new long[deepest + 2 - 1];
        }

        logger.info("Dimensions "+Arrays.toString(dims));
        
        // axes
        AxisType[] axes = new AxisType[dims.length];

        // getting a leaf to get the width
        PlaneDB leaf = getALeaf(root).getValue().getPlaneDB();
        logger.info("Leaf found  :"+leaf);
        // preparing X and Y axis
        int width = leaf.getMetaDataSet().get(MetaData.WIDTH).getIntegerValue();
        int height = leaf.getMetaDataSet().get(MetaData.HEIGHT).getIntegerValue();

        dims[0] = width;
        dims[1] = height;

        axes[0] = Axes.X;
        axes[1] = Axes.Y;

        final int pixelType = FormatTools.UINT16;

        // feeling the other dimensions size
        if (deepest != 0) {
            for (int i = 0; i != deepest - 1; i++) {
                dims[i + 2] = TreeItemUtils.getBrotherLevelMaxChildrenNumber(root, i);
                final int index = i;
                TreeItemUtils.goThroughLevel(root, i + 1, metadataItem -> {

                    if (axes[index + 2] != null) {
                        return;
                    }
                    MetaData metadata = (MetaData) metadataItem.getValue().getMetaData();

                    if (metadata.getName().toLowerCase().contains("channel")) {
                        axes[index + 2] = Axes.CHANNEL;
                    } else {
                        axes[index + 2] = new DefaultAxisType(metadata.getName());
                    }

                });
                //axes[i + 2] = new DefaultAxisType("Awesome",false);

            }
        }
        
        

        for (int i = 0; i != dims.length; i++) {
            logger.info(String.format("Dimesion %d : %d", i, dims[i]));
        }

        // creating the dataset that will hold the newly formed image
        Dataset dataset = createDataset(pixelType, dims, axes);

        // going through the deepest level (-1) that hold the
        // last metadata and should contain only one plane
        final SimplerCounter counter = new SimplerCounter();
        
        // if it's not a single plane
        if (deepest > 0) {
            TreeItemUtils.goThroughLevel(root, deepest - 1, child -> {
                if (child.getChildren().size() == 1 && child.getChildren().get(0).getValue().isPlane()) {

                    // caculating the dimensions indexes of the child
                    List<Long> indexes = getLeafValues(root, child);

                    // transforming the list into an array of long
                    Long[] indexesAsLong = new Long[indexes.size()];
                    indexes.toArray(indexesAsLong);

                    // getting the plane index using a simpler counter
                    int planeIndex = counter.getCount();

                    // inserting the plane
                    insertPlaneIntoDataset(child.getChildren().get(0), dataset, planeIndex, pixelType);

                    // incrementing the counter
                    counter.increment();
                }
            });
        } else {

            insertPlaneIntoDataset(root, dataset, 0, pixelType);

        }

        datasetService.getContext().getService(UIService.class).show(dataset);
        datasetService.getContext().getService(UiContextService.class).leave("image-browser").enter("imagej").update();
        return dataset;
    }

    private void insertPlaneIntoDataset(TreeItem<? extends PlaneOrMetaData> childItem, Dataset dataset, int planeIndex, int pixelType) {
        PlaneDB planeDB = childItem.getValue().getPlaneDB();
        try {

            Plane plane = getPlane(planeDB.getFile(), planeDB.getPlaneIndex());
            dataset.setPlaneSilently(planeIndex, getConvertedArray(pixelType, plane.getBytes()));

        } catch (IOException ioe) {
            logger.log(Level.SEVERE, String.format("Couldn't extract plane %d from %s", planeIndex, planeDB.getFile().getName()));

        } catch (FormatException fe) {
            logger.log(Level.SEVERE, String.format("Couldn't find the right format for plane %d from %s", planeIndex, planeDB.getFile().getName()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("Error when inserting plane %s from %s into dataset", planeIndex, planeDB.getFile().getName()));
        }

    }

    private long[] getPlanePosition(TreeItem root, TreeItem leaf) {

        int deepestLevel = TreeItemUtils.getDeepestLevel(root, 0);

        return new long[deepestLevel + 2];
    }

    private long getIndex(long[] dims, long[] indexes) {
        int finalIndex = 0;
        for (int i = 2; i != dims.length; i++) {
            if (i + 1 == dims.length) {
                finalIndex += indexes[i - 2];
            } else {
                finalIndex += indexes[i - 1] * dims[i + 1];
            }

            finalIndex += dims[i] * indexes[i - 2];
        }

        return finalIndex;
    }

    private List<Long> getLeafValues(TreeItem root, TreeItem item) {
        ArrayList<Long> list = new ArrayList<>();

        TreeItem parent = item.getParent();
        TreeItem child = item;

        while (child != root && parent != null) {
            list.add((long) parent.getChildren().indexOf(child));

            child = parent;
            parent = parent.getParent();

        }

        Collections.reverse(list);
        //list.remove(0);

        return list;

    }

    public <T> TreeItem<? extends T> getALeaf(TreeItem<? extends T> root) {
        TreeItem<? extends T> item = root;
        while (item.isLeaf() == false) {
            item = item.getChildren().get(0);

        }

        return item;
    }

    public static Reader currentReader;
    public static File currentFile;
    public static SCIFIO scifio;

    public SCIFIO getSCIFIO() {
        if (scifio == null) {
            scifio = new SCIFIO(context());
        }
        return scifio;
    }

    private Reader reader = null;

    private synchronized Plane getPlane(File file, long planeIndex) throws IOException, io.scif.FormatException {

        Reader r = getReader(file);

        if (r != null) {
            logger.info("Opening plane 0");
            Plane plane = r.openPlane(0, planeIndex);
            logger.info("Plane opened. Returning plane");
            return plane;
        }

        return null;

    }

    private Reader getReader(File file) {

        // if the reader is already reading the file
        if (reader != null) {
            logger.info("Reader already created");
            if (new File(reader.getCurrentFile()).equals(file)) {
                logger.info("Reader set on the same file. Returning the pre-existing reader");
                return reader;
            } else {
                reader = null;
            }
        }

        logger.info("Creating a new Reader");
        // trying creating a reader with scifio
        try {
            reader = scifio().initializer().initializeReader(file.getAbsolutePath());
            logger.info("Reader created with SCIFIO");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            reader = null;
        }

        // but sometimes, it does work for DV files for examples, so we call the Bioformat Reader directlry
        if (reader == null) {

            try {

                Format format = new BioFormatsFormat();
                getContext().inject(format);

                reader = format.createReader();
                logger.info("Reader created with BioFormat.");

            } catch (FormatException ex) {
                logger.severe("Couldn't get a Reader for the image " + file.getName());
                logger.log(Level.SEVERE, null, ex);

            }
        }

        if (reader != null) {

            try {
                logger.info("Setting source file");
                reader.setSource(file.getAbsolutePath());
                logger.info("Source file set :-)");
                return reader;
            } catch (IOException ex) {
                logger.severe("Couldn't set the image as source of the reader" + file.getName());
                logger.log(Level.SEVERE, null, ex);
                reader = null;
            }

        }

        return reader;
    }

    public SCIFIO scifio() {
        if (scifio == null) {
            scifio = new SCIFIO(getContext());
        }
        return scifio;
    }

    public synchronized Dataset getDataset(File file, int planeIndex) throws IOException, FormatException {

        Plane plane = getPlane(file, planeIndex);
        if (plane == null) {
            return null;
        }
        logger.info("Creating Dataset from plane");
        Dataset dataset = createDataset(plane);
        logger.info("Dataset Created");
        int pixelType = plane.getImageMetadata().getPixelType();
        logger.info("Pixel type : " + pixelType);
        dataset.setPlane(0, getConvertedArray(pixelType, plane.getBytes()));
        return dataset;

    }

    public Dataset getDataset(PlaneDB planedb) throws IOException, FormatException {
        return getDataset(planedb.getFile(), (int) planedb.getPlaneIndex());
    }

    private Dataset createDataset(Plane plane) {
        logger.info("Getting metadata :-D");
        long[] dims = new long[2];
        dims[0] = plane.getImageMetadata().getAxisLength(Axes.X);
        dims[1] = plane.getImageMetadata().getAxisLength(Axes.Y);
        logger.info(String.format("Dimensions : %d x %d", dims[0], dims[1]));
        AxisType[] axes = new AxisType[]{Axes.X, Axes.Y};
        return createDataset(plane, dims, axes);
    }

    private Dataset createDataset(int pixelType, long[] dims, AxisType[] axisTypes) {
        Dataset dataset = null;
        switch (pixelType) {
            case FormatTools.UINT8:
                dataset = datasetService.create(new ByteType(), dims, "", axisTypes);
                break;
            case FormatTools.UINT16:
                dataset = datasetService.create(new ShortType(), dims, "", axisTypes);
                break;
            case FormatTools.FLOAT:
                dataset = datasetService.create(new FloatType(), dims, "", axisTypes);
                break;
            case FormatTools.DOUBLE:
                dataset = datasetService.create(new DoubleType(), dims, "", axisTypes);
                break;
        }

        return dataset;
    }

    private Dataset createDataset(Plane plane, long[] dims, AxisType[] axisTypes) {
        int pixelType = plane.getImageMetadata().getPixelType();
        return createDataset(pixelType, dims, axisTypes);
    }

    private Object getConvertedArray(int pixelType, byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.rewind();
        switch (pixelType) {
            case FormatTools.UINT8:
                return bytes;
            case FormatTools.UINT16:
                short[] short_array = new short[bytes.length / 2];
                buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(short_array);
                return short_array;

            case FormatTools.INT32:
                int[] int_array = new int[bytes.length / 4];
                buffer.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(int_array);
                return int_array;
            case FormatTools.FLOAT:
                float[] float_array = new float[bytes.length / 4];
                buffer.order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(float_array);
                return float_array;
            case FormatTools.DOUBLE:
                double[] double_array = new double[bytes.length / 8];
                buffer.order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer().get(double_array);
                return double_array;
        }
        return null;

    }

}
