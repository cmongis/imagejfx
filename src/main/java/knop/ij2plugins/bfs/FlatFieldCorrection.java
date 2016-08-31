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
import ij.plugin.ImageCalculator;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.io.File;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author alex
 */
@Plugin(type = Command.class, headless = true, menuPath = "KnopLab > Flatfield Correction")
public class FlatFieldCorrection implements Command {

    /*     
     Parser for command / Macro arguments.
     */
  



    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;

    @Parameter(label = "Flatfield Image", autoFill = false)
    File flatfield;

    @Parameter
    DatasetService service;

    @Parameter
    ImageJ ij;

    @Parameter(label = "Restore original bit depth", autoFill = true)
    boolean keepOriginalBitDepth = true;

    @Parameter(label = "Multiply", autoFill = true)
    boolean multiply = true;

    protected final int SUCCESS = 0, ABORT = 1;
    protected final String PATH_CORRECTED_IMAGES = "Corrected_images";
    protected final String SUFFIX_CORRECTED_FLAT_FIELD = "_ffCorr";
    protected final String SUFFIX_CORRECTED_DARK_CURRENT = "_dcCorr";

    protected boolean DARK_CORRECTED = false;

    /*
     Data
     */
    ImagePlus imageOrigin = null, imageFlatField = null, imageDarkCurrent = null;
    String pathSaving;
    protected boolean DISPLAY = false; // DIsplays the corrected images or not.

    public static void main(String[] args) {

    }

    public FlatFieldCorrection() {
    }

    /**
     * ImageJ 's method used in plug-in and macro mode. Called after the
     * constructor.
     *
     * @param args
     */
    @Override
    public void run() {
        RandomAccessibleInterval<UnsignedShortType> r = (RandomAccessibleInterval<UnsignedShortType>) dataset.getImgPlus();
        long begin = System.currentTimeMillis();
        ImagePlus wrapImage = ImageJFunctions.wrap(r, "");

        setOriginImage(wrapImage);
        this.imageFlatField = IJ.openImage(flatfield.getAbsolutePath());
        ImagePlus result = correct(this.imageOrigin, this.imageFlatField);
        Img img = ImageJFunctions.wrap(result);
        dataset = service.create(img);
    }

    public void setOriginImage(ImagePlus imp) {
        imageOrigin = imp;
    }

    /**
     * Applies the correction algorithm.
     *
     * @return ImagePlus the flat-field corrected image.
     */
    public ImagePlus runCorrection() {

        Timer t = new Timer();
        t.start();
        this.imageOrigin.setProcessor(imageOrigin.getProcessor().convertToFloat());
        t.elapsed("32 bit conversion");
        ImageCalculator ic = new ImageCalculator();
        ImagePlus imageDarkCorrected = null, imageCorrected = null;

        imageDarkCorrected = imageOrigin;

       // ImageCalculator imageCalculator = new ImageCalculator();
        // Correct the flat field
        t.elapsed("Dark current substraction");

        imageFlatField.setProcessor(imageFlatField.getProcessor().convertToFloat());

        t.elapsed("image division");
        imageCorrected.setTitle(this.imageOrigin.getTitle() + "-Corrected");

        t.total("DONE\n");
        return imageCorrected;
    }

    public ImagePlus correct(ImagePlus image, ImagePlus flatfieldImp) {

        ImageStack stack = image.getStack();
        if (stack == null) {
            return correct(image, flatfieldImp);
        }
        ImageStack result = new ImageStack(image.getWidth(), image.getHeight());
        ImageProcessor flatfieldImageProcessor = flatfieldImp.getProcessor();
        for (int i = 1; i != stack.getSize() + 1; i++) {
            result.addSlice(correct(stack.getProcessor(i), flatfieldImageProcessor));
        }

        return new ImagePlus("corrected image", result);

    }

   

    public ImageProcessor correct(ImageProcessor imageProcessor, ImageProcessor flatfieldProcessor) {

        boolean is16bit = imageProcessor.getBitDepth() == 16;
        Timer t = new Timer();

        if (is16bit) {
            imageProcessor = imageProcessor.convertToFloat();
            t.elapsed("32 bit conversion");
        }

        float[] pix1 = (float[]) imageProcessor.getPixels();
        float[] pix2 = (float[]) flatfieldProcessor.getPixels();
        float[] pix3;

        FloatProcessor result = new FloatProcessor(imageProcessor.getWidth(), imageProcessor.getHeight());

        t.elapsed("creating blank image.");
        if (pix1.length != pix2.length) {

            System.err.println("Pixel length not equals !");
            return result;
        }

        pix3 = (float[]) result.getPixels();
        if (multiply) {
            for (int i = 0; i != pix1.length; i++) {
                float r = pix1[i] * pix2[i];
                pix3[i] = r;
            }
        } else {
            for (int i = 0; i != pix1.length; i++) {
                float r = pix1[i] / pix2[i];
                pix3[i] = r;
            }
        }
        t.elapsed("correction");
        if (keepOriginalBitDepth && is16bit) {
            
            ImageProcessor ip = new ShortProcessor(imageProcessor.getWidth(),imageProcessor.getHeight());
            
            short[] shortPixels = (short[]) ip.getPixels();
            
            for(int i = 0;i!=pix3.length;i++) {
                shortPixels[i] = (short)Math.round(pix3[i]);
            }
            t.elapsed("16-bit back conversion");
            
            return ip;
            
           
        }

        return result;

    }

}
