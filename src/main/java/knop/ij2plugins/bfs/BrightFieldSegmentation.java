/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knop.ij2plugins.bfs;

import knop.ij2plugins.bfs.useful.Timer;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.plugin.MontageMaker;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import knop.ij2plugins.mser.MSER_;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.module.Module;
import org.scijava.module.process.InitPreprocessor;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author alex
 */
@Plugin(type = Command.class, headless = true, menuPath = "Plugins > KnopLab > Brightfield Segmentation")
public class BrightFieldSegmentation implements Command {

    /*------------------------------------
     Backgound correction parameters
     */
    // Split image parameters
//    protected double minSizeSplit; // Used for splitting the cells. Not implemented.
//    protected double maxSizeSplit;

    /*----------------------------------
     Segmentation: MSER parameters
     */
    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;

    @Parameter(label = "Tile dimension", autoFill = true)
    protected int tileDim = 40; // tile dimension used for the clustering step

    @Parameter(label = "Lambda", autoFill = true)
    protected int lambda = 5; // tile dimension used for the MSER

    @Parameter(label = "MSER minimum size", autoFill = true)
    protected int minSizeMSER = 400;

    @Parameter(label = "MSER maximum size", autoFill = true)
    protected int maxSizeMSER = 2500;

    @Parameter(label = "Maximum variation", autoFill = true)
    protected double maxVariation = 1.0;

    @Parameter(label = "Mininum of diversity", autoFill = true)
    protected double maxEcc = 0.7; // minimum of diversity

    @Parameter(label = "Dark to bright", autoFill = true)
    protected boolean darkToBright = true; // gradient thresholding from 0->255

    @Parameter(label = "Bright to dark", autoFill = true)
    protected boolean brightToDark = true; // gradient thresholding from 255->0

    @Parameter
    DatasetService service;


    /*-----------------------
     Image parameters
     */
    protected ImagePlus originalImage;
    protected ImagePlus segmentedImage;
    //protected double maxValue; // of Iorg
    //protected double minValue; // of Iorg
    protected final int maxIntensity = 255;

    /*-------------------------------------
     Plug-in information
     */
    protected String descriptionPlugin = "The bright field segmentation algorithm should"
            + "take bright field images in input."
            + "It first corrects the bagkground of the image."
            + "Then the corrected image is segmented using the MSER algorithm.";

    /*-----------------------
     Useful parameters
     */
    protected long t0, t1, totalTime; // used for time measurement

    protected static final String PATH = "/home/alex/NetBeansProjects/BrightFieldSegmentation/";
    //protected static final String PATH = "/home/alex/bfs";

    protected ArrayList<ImagePlus> toVisualize; // Store images to vizualise later
//    protected String SAVE_TO = System.getProperty("user.home") + File.separator;
    protected final boolean DEBUGGING = false;

    public static void main(String[] args) {

        ImagePlus test = IJ.openImage("./test.tif");
        
        IJ.save(test, "test2.tif");

    }

    public static Runnable segment(final ImageJ ij, final Dataset dataset, final int minMSER, final int maxMSER, final int lambda, final int tileDim, final double variation) {
        return new Runnable() {
            public void run() {
                String path = "/home/cyril/test_img/";
                String outputPath = String.format("min=%d__max=%d__lambda=%d__tile=%d__var=%.3f.tif", minMSER, maxMSER, lambda, tileDim, variation);
                Timer t = new Timer();
                //BrightFieldSegmentation command = new BrightFieldSegmentation();

                t.elapsed("Module created");
                // preceprocessing the parameters
                final InitPreprocessor initPre = new InitPreprocessor();

                t.elapsed("Preprocessing");
                        // Retrieving the parameters

                // creating the map holding the parameters
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("dataset", dataset);
                params.put("minSizeMSER", minMSER);
                params.put("maxSizeMSER", maxMSER);
                params.put("lambda", lambda);
                params.put("tileDim", tileDim);
                params.put("maxVariation", variation);

                // preprocessParameters(params, info);
                t.elapsed("Second preprocessing");

                Module command = ij.module().createModule(ij.command().getCommand(BrightFieldSegmentation.class));
                initPre.process(command);
                // setting parameters
                command.setInputs(params);

                try {
                    // running command
                    command.run();

                } catch (Exception e) {
                    System.err.println("Error with " + outputPath);
                    e.printStackTrace();
                }
                t.elapsed("Command execution");

                // incrementing the sub total of this task
                //incrementSubProgress(subTotal);
                //output item
                // output
                Object output = command.getOutput("dataset");

                try {
                    ij.dataset().save((Dataset) output, path + outputPath);
                } catch (IOException ex) {
                    Logger.getLogger(BrightFieldSegmentation.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Coudln't save the poor lad.");
                }

                t.elapsed("Saved in");
                System.gc();
            }
        };
    }

    /**
     * Sets the defaults parameters that give good results with Florian's cell
     * images.
     */
    public void setDefaultParameters() {
        /*
         * Default parameters used in Matlab
         */

        this.lambda = 8;//8;
        this.minSizeMSER = 400;//200;
        this.maxSizeMSER = 8000;
        this.maxVariation = 1.00;
        this.maxEcc = 0.7;//0.00;
        this.darkToBright = true;
        this.brightToDark = true;

        this.tileDim = 100;

    }

    public BrightFieldSegmentation() {

    }

    /**
     * Runs the background correction followed by the actual MSER segmentation.
     */
    public void startSegmentation() {

        //
        this.totalTime = System.currentTimeMillis();

        Timer t = new Timer();
        t.start();

        ImageStatistics stats = originalImage.getStatistics();

        t.elapsed("Getting image statistics");
        Double minValue;
        Double maxValue;

        if (this.originalImage.getBytesPerPixel() != 1) {
            //IJ.run(this.originalImage, "8-bit", "");

            this.originalImage.setProcessor(this.originalImage.getProcessor().convertToByte(true));
            t.elapsed("Image converted to 8-bits");
        }
        
        

        // that a real name
        ImagePlus backgroundCorrectedImage = this.originalImage.duplicate();
        
        t.elapsed("Image duplication");
        // getDimension: 
        //   ->Returns the dimensions of this image (width, height, nChannels, nSlices, nFrames) 
        //     as a 5 integers large array. 

        // getting min and max values
        ImageProcessor ip = originalImage.getProcessor();
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        //IJ.run(this.originalImage, "8-bit", "");

        /*
         * We put in IorgDouble each pixel value of Iorg that we divide by the maximum intensity
         *   -> get a intensity which range = [0, 1]. This increases the precision of 
         *   computation.
         * 
         * The min and max values of the image are computed in this same nested loop
         */
        double[][] IorgDouble = new double[height][width];

        Double iorgMax = null;
        Double iorgMin = null;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                final double p = 1.0 * ip.getPixel(w, h) / this.maxIntensity;
                IorgDouble[h][w] = p;//(double) ip.getPixel(w, h) / (double) this.maxIntensity;
                if (iorgMax == null) {

                    iorgMax = p;//IorgDouble[h][w];
                    iorgMin = p;//IorgDouble[h][w];
                } else if (p > iorgMax) {
                    iorgMax = p;//IorgDouble[h][w];
                } else if (p < iorgMin) {
                    iorgMin = p;//IorgDouble[h][w];
                }
            }
        }
        maxValue = iorgMax;
        minValue = iorgMin;

        t.elapsed("intensity conversion");

        //ArrayComparator.writeArray(IorgDouble, "compare_tile=30/IorgDouble_Java.txt");
        /*
         * Compute the background
         */
        BackgroundEstimator bgest = new BackgroundEstimator(IorgDouble, height,
                width, this.tileDim);
        Double[][] bg;
        try {
            bg = bgest.estimate_Background();
        }
        catch(NullPointerException e) {
           e.printStackTrace();
           throw new SegmentationException("Error when estimating the background : the tile dimension may be wrong.", e);
        }
        t.elapsed("bgest");

        /*
         * CORRECT THE IMAGE
         */
        ip = backgroundCorrectedImage.getProcessor().duplicate();
        
        
        
        double pixel;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                if (bg[h][w] == null) {
                    bg[h][w] = 1.0; // in case null value remain. That should never happen though.
                }
                if (bg[h][w] < minValue) {
                    bg[h][w] = minValue;
                }
                if (bg[h][w] > maxValue) {
                    bg[h][w] = maxValue;
                }

                // correction by dividing pixel background
                pixel = IorgDouble[h][w] / bg[h][w];

                // correct value aberrations
                if (pixel > 1.0) {
                    pixel = 1.0;
                } else if (pixel < 0.0) {
                    pixel = 0.0;
                }

                // Restore from double to in precision -> [0-255]
                ip.putPixelValue(w, h, pixel * this.maxIntensity);
            }
        }

        t.elapsed("bgcorrection");

        /*
         * SEGMENT THE IMAGE
         */
        segmentedImage = new ImagePlus("Segmented", backgroundCorrectedImage.getProcessor());
        segmentedImage.setTitle("Segmented image");

        MSER_ mser = new MSER_(this.lambda, this.minSizeMSER,
                this.maxSizeMSER, this.maxVariation, this.maxEcc,
                this.darkToBright, this.brightToDark);

        segmentedImage = mser.runMser(segmentedImage);
        bg = null;
        mser = null;
        t.elapsed("cell plitting");
        System.gc();
        t.elapsed("memory cleaning");
        postPrecess(segmentedImage);

        t.elapsed("post precess");

        t.total("total time");
        //IJ.log("\nSegmentation is over");
    }

    
    @Parameter(label = "Min. Perimeter (Filtering)", autoFill = true)
    int minimumPerimeter = 100;
    
    @Parameter(label = "Max. Parameter (Filtering)" , autoFill = true)
    int maximumPerimeter = 200;
    
    @Parameter(label = "Min. Circularity (Filtering)", autoFill = true)
    double minCircularity = 1;
    
    @Parameter(label = "Max. Circularity (Filtering)", autoFill = true)
    double maxCirculatiry = 40;
    
    @Parameter(label = "Skip post-processing",autoFill = true) 
    boolean skipPostprocessing = false;
    

    
    public void postPrecess(ImagePlus imp) {
        if(skipPostprocessing) return;
        ManyBlobs blobs = new ManyBlobs(imp);
        imp.getProcessor().invert();
        blobs.findConnectedComponents();

        ByteProcessor ip = new ByteProcessor(imp.getWidth(), imp.getHeight());

        for (Blob b : blobs) {
            ip.setColor(255);
           // System.out.println(b.getCircularity());
            if (b.getCircularity() > minCircularity && b.getCircularity() < maxCirculatiry && b.getPerimeter() > minimumPerimeter && b.getPerimeter() < maximumPerimeter) {
                //ip.setRoi(b.getOuterContour());
                ip.fillPolygon(b.getOuterContour());
            }
        }
        ip.dilate();
        imp.setProcessor(ip);
        //imp.show();

    }

    /**
     * Takes all ImagePlus for the ArrayList if each step images. Then displays
     * them in a montage.
     */
    public void visualize() {

        if (this.toVisualize.size() != 3) {
            //IJ.log("ERROR with number of images to visualize. Cancelling the visualization");
            return;
        }

        ImagePlus iStack = this.toVisualize.get(0).createImagePlus();

        ImageStack stack = new ImageStack(this.toVisualize.get(0).getDimensions()[0],
                this.toVisualize.get(0).getDimensions()[1]);

        stack.addSlice(this.toVisualize.get(0).getProcessor());
        stack.addSlice(this.toVisualize.get(1).getProcessor());
        stack.addSlice(this.toVisualize.get(2).getProcessor());

        /* Uncomment to save all iages from the montage.
         IJ.save(this.toVisualize.get(0), this.SAVE_TO + "last_" + "Iorg_Java" + ".png");
         IJ.save(this.toVisualize.get(1), this.SAVE_TO + "last_" + "bg_corrected_Java" + ".png");
         IJ.save(this.toVisualize.get(2), this.SAVE_TO + "last_" + "mser_Java" + ".png");
         */
        iStack.setStack(stack);
        iStack.setDimensions(1, this.toVisualize.get(0).getSlice(), 1);

        MontageMaker mk = new MontageMaker();
        ImagePlus montage = mk.makeMontage2(iStack, 3, 1, 3d, 1, 3, 1, 100, true);
        //makeMontage2(ImagePlus imp, int columns, int rows, double scale, int first, int last, int inc, int borderWidth, boolean labels)

        //IJ.save(montage, this.SAVE_TO + "montage.tif");
        //IJ.save(montage, "/home/alex/bfs/montage.png");
        //IJ.save(montage, "/home/alex/bfs/montage.tif");
    }

    /**
     * USEFUL / DEBUGGING Used for debugging sClass and sType in dbscan()
     *
     * @param sClass array used for the background correction's clustering of
     * the tiles.
     * @param sType array used for the background correction's clustering of the
     * tiles.
     */
    public void printSizes(double[] sClass, double[] sType) {

        double sumClass = 0d, sumType = 0d;
        for (int kk = 0; kk < sClass.length; kk++) {
            //System.out.println(Integer.toString(kk+1) + ": " + sClass[kk]);
            sumClass += sClass[kk];
        }
        for (int kk = 0; kk < sType.length; kk++) {
            //System.out.println(Integer.toString(kk+1) + ": " + sClass[kk]);
            sumType += sType[kk];
        }

        System.out.println("sumClass= " + sumClass);
        System.out.println("sumType= " + sumType);
    }

    /**
     * Displays an information (error message) window showing how to call this
     * plug-in as an ImageJ macro.
     */
    public void showMacroManual() {
        String man = "<html></strong><u>Manual : Bright Field Segmentation</u></strong><br/>"
                + "<br/><u>Syntax :</u><br/>"
                + "  run(\"Bright_Field_Segmentation\", \" -parameter1 value1  -parameter2 value2\") <br/>"
                + "OR from the command line : "
                + "  java -jar BrightFieldSegmentation.jar -parameter1 value1  -parameterN valueN <br/>"
                + "<br/><u>Available Parameters (and default values) :</u> <br/>"
                + " -file : image file  path <br/>"
                + " -tileDim : tile dimension used for clustering (30) <br/>"
                + " -lambda : lambda for MSER (5)<br/>"
                + " -minSize : minimum size for MSER (30) <br/>"
                + " -maxSize : maximum size for MSER (4000) <br/>"
                + " -maxVar : maximum variation for MSER (1.00) <br/>"
                + " -minDiv : minimum diversity for MSER (0.7) <br/>"
                + " -darkToBright : process MSER from dark to bright - true or false (true)<br/>"
                + " -brightToDark : process MSER from bright to dark - true or false (false)<br/>"
                + "<br/>The file parameter is mandatory whereas the others are not and default values will be considered<br/>"
                + "</html>";

        JOptionPane.showMessageDialog(null,
                man,
                "Need parameters",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Displays an error window for boolean parameters.
     *
     * @param parameter the wrong parameter
     */
    public void dialogErrorboolean(String parameter) {
        JOptionPane.showMessageDialog(null,
                parameter + " should be either \"true\" or \"false\".",
                "Wrong argument",
                JOptionPane.ERROR_MESSAGE
        );
        showMacroManual();

    }

    /**
     * Displays an error window for Number parameters.
     *
     * @param parameter the wrong parameter
     */
    public void dialogErrorNumber(String parameter) {
        JOptionPane.showMessageDialog(null,
                parameter + " should be a number.",
                "Wrong argument",
                JOptionPane.ERROR_MESSAGE
        );
        showMacroManual();
    }

    @Override
    public void run() {

        //setDefaultParameters();
        RandomAccessibleInterval<UnsignedShortType> r = (RandomAccessibleInterval<UnsignedShortType>) dataset.getImgPlus();
        long begin = System.currentTimeMillis();
        originalImage = ImageJFunctions.wrapUnsignedShort(r, "");
        System.out.println(System.currentTimeMillis() - begin + "ms to convert");

        startSegmentation();
        //ByteImagePlus segmentedImage = (ByteImagePlus) ImageJFunctions.wrapByte(this.segmentedImage);
        Img img = ImageJFunctions.wrapByte(this.segmentedImage);
        dataset = service.create(img);
        System.out.println("The dataset is set !");
       // dataset.setImgPlus((ImgPlus<? extends RealType<?>>) ImageJFunctions.wrapByte(segmentedImage));

        //ImageJFunctions.show((RandomAccessibleInterval) ImageJFunctions.wrap(originalImage));
        //startSegmentation();
    }

    
    public class SegmentationException extends RuntimeException {
        public SegmentationException(String message,Exception e) {
            super(message);
            setStackTrace(e.getStackTrace());
           
            
        }
    }

    
    
}
