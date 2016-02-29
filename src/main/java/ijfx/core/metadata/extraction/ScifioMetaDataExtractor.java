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
package ijfx.core.metadata.extraction;

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.ui.main.ImageJFX;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.SCIFIO;
import io.scif.Writer;
import io.scif.config.SCIFIOConfig;
import io.scif.filters.ReaderFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import ijfx.core.metadata.extraction.completor.FromNameCompletor;
import mongis.ndarray.NDimensionalArray;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;

import org.scijava.log.LogService;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class ScifioMetaDataExtractor  {

    final SCIFIO scifio = new SCIFIO();
    SCIFIOConfig config;
    BioFormatExtractor bioformatExtractor = new BioFormatExtractor();
    Logger logger = ImageJFX.getLogger();
    public static void main(String... args) {

        ScifioMetaDataExtractor extractor = new ScifioMetaDataExtractor();

        extractor.extract(new File("/Users/cyril/test_img/vera/2014_08_13/H08-01_R3D.dv")).get(0).savePixels("test.png");

    }

    public ScifioMetaDataExtractor() {
        config = new SCIFIOConfig();
        config.checkerSetOpen(true);
        scifio.log().setLevel(LogService.INFO);
    }

   
    public PlaneList extract(File file) {
        logger.info("Starting extraction");
        //System.out.println("Reading " + file.getName() + "...");
       

      
        try {
         //  final Format format
            //      = scifio.format().getFormat(file.getAbsolutePath(),
            //           config);

            String path = file.getAbsolutePath();
            ReaderFilter r;
            
            if (path.endsWith("tif") || path.endsWith(".dv")) {
                logger.info("Using BioFormat extractor");
                PlaneList planeList = bioformatExtractor.extract(file);
                
                return planeList;

            } else {
                logger.info("Using SCIFIO Extractor.");
                r = scifio.initializer().initializeReader(file.getAbsolutePath(), config);
                logger.info("Plane count : " + r.getPlaneCount(0));

                logger.info("Format name : " + r.getFormatName());

                

            }

            // creating a list that will contains all the extracted planes
            PlaneList extractedPlanes = new PlaneList();

            ImageFile imageFile = new ImageFile(file.getAbsolutePath());
            imageFile.setImagePlaneList(extractedPlanes);
            FromNameCompletor completor = new FromNameCompletor();

            for (int img = 0; img != r.getImageCount(); img++) {
                // first, let's get the image orginazation and the
                // individual axis values.
                Metadata meta = r.getMetadata();
                ArrayList<CalibratedAxis> nonPlanarAxis = new ArrayList<CalibratedAxis>();

                // used to calculate all the combinaitions of axis (Z,Channel...)
                NDimensionalArray ndarray = new NDimensionalArray();

                ImageMetadata image = meta.get(img);

                // width of images
                int width = 0;
                int height = 0;
                logger.info(image.getAxes().size() + " axis found !");
                for (CalibratedAxis a : image.getAxes()) {
                    logger.info("\t Axe : " + a.type().toString());
                    String type = a.type().toString();
                    if (type.equals("X") || type.equals("Y")) {

                        if (type.equals("X")) {
                            width = (int) image.getAxisLength(a);
                        } else {
                            height = (int) image.getAxisLength(a);
                        }

                        continue;
                    }

                    // adding it as a non planar axis (channel, z, etc...)
                    nonPlanarAxis.add(a);
                    ndarray.add(type, (int) image.getAxisLength(a));
                    logger.info("Non planar axis found : " + a.type().toString());
                    //System.out.println(a);
                    //System.out.println("type : " + a.type());
                    //System.out.println("unit : " + a.unit());

                    //System.out.println("pace : " + a.calibratedValue(1));
                }
                if (ndarray.size() == 0) {
                    return extractedPlanes;
                }
                long[][] planeAxisSets = ndarray.get(0).generateAllPossibilities();

                if (planeAxisSets.length != image.getPlaneCount()) {
                    System.err.println("The axis possibilities doesn't equals the number of planes !");
                    return null;
                }

                for (int i = 0; i != image.getPlaneCount(); i++) {
                    MetaDataSet set = new MetaDataSet();
                    SCIFIOImagePlane plane = new SCIFIOImagePlane(r, img, i);
                    plane.setSourceFile(imageFile);
                    plane.setMetaDataSet(set);
                    set.put(new GenericMetaData(MetaData.WIDTH, width));
                    set.put(new GenericMetaData(MetaData.HEIGHT, height));
                    set.put(new GenericMetaData(MetaData.PLANE_INDEX, plane.getPlaneIndex()));
                    set.put(new GenericMetaData(MetaData.FILE_NAME, file.getName()));
                    set.put(new GenericMetaData(MetaData.FOLDER_NAME, file.getParentFile().getName()));
                    set.put(new GenericMetaData(MetaData.ABSOLUTE_PATH, file.getAbsolutePath()));

                    int j = 0;
                    for (CalibratedAxis a : nonPlanarAxis) {
                        String type = a.type().toString();
                        long id = planeAxisSets[i][j++];
                        set.put(new GenericMetaData(type, id));
                    }

                    extractedPlanes.add(plane);

                }

            }

            //Searching metadata in the name
            extractedPlanes.mergeMetaDataSet(completor.extract(imageFile));
            logger.info("Returning  " + extractedPlanes.size() + " planes.");
            return extractedPlanes;

        } catch (IOException ioe) {
            logger.warning("Coudn't read file...");
            ioe.printStackTrace();
        } catch (FormatException fe) {

            logger.warning("Counldn't read format...");
            fe.printStackTrace();
        } catch (NullPointerException ne) {
            logger.severe("Damn Reader !");
            ne.printStackTrace();
        } catch (Exception e) {
            logger.severe("An other error");
            e.printStackTrace();
        }
        return null;
    }

    public class SCIFIOImagePlane implements ImagePlane {

        MetaDataSet set;
        Reader reader;
        Metadata originalMetadata;

        int imageId;
        long planeId;

        ImageFile sourceFile;

        public void setSourceFile(ImageFile sourceFile) {
            this.sourceFile = sourceFile;
        }

        public SCIFIOImagePlane(Reader reader, int imageId, int planeId) {
            this.imageId = imageId;
            this.planeId = planeId;
            this.reader = reader;
            this.originalMetadata = reader.getMetadata();
           // System.out.println(originalMetadata.get(imageId).getPlaneCount());
        }

        @Override
        public void setMetaDataSet(MetaDataSet set) {
            this.set = set;
        }

        @Override
        public ImageFile getSourceFile() {
            return sourceFile;
        }

        @Override
        public MetaDataSet getMetaDataSet() {
            return set;
        }

        @Override
        public Object getPixels() {
            try {

                //System.out.println("Opening image " + imageId + ", plane " + planeId);
                //System.out.println("Planes : " + reader.getPlaneCount(imageId));
                return reader.openPlane(imageId, planeId);
            } catch (Exception fe) {
                fe.printStackTrace();
               // System.err.println("Couldn't read image !!!");

                return null;
            }
        }

        @Override
        public boolean savePixels(String path) {
            try {
                Writer writer = scifio.initializer().initializeWriter(reader.getMetadata(), path);
                // Plane plane = (Plane)getPixels();
                // if(plane == null) System.out.println("Why is it null ??? check the reader !");
                Plane plane = reader.openPlane(imageId, planeId);

              //  System.out.println(writer.getFormatName());
               // System.out.println(plane.getBytes().length);

                //System.out.println(imageId);
                writer.savePlane(0, 0, plane);
                writer.close();

                return true;
            } catch (FormatException ex) {
                ImageJFX.getLogger().log(Level.SEVERE,"Error when saving files. Saving format is not handled",ex);;
                ex.printStackTrace();

            } catch (IOException ex) {
                ImageJFX.getLogger().log(Level.SEVERE,null,ex);;
                ex.printStackTrace();
            }

            return false;
        }

        @Override
        public long getPlaneIndex() {
            return planeId;
        }

    }

    private AxisType[] axesToSplit(final ReaderFilter r) {
        final Set<AxisType> axes = new HashSet<AxisType>();
        final Metadata meta = r.getTail().getMetadata();
// Split any non-X,Y axis
        for (final CalibratedAxis t : meta.get(0).getAxesPlanar()) {
            final AxisType type = t.type();
            if (!(type == Axes.X || type == Axes.Y)) {
                axes.add(type);
            }
        }
// Ensure channel is attempted to be split
        axes.add(Axes.CHANNEL);
        return axes.toArray(new AxisType[axes.size()]);
    }
}
