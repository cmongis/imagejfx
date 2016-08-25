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
package ijfx.ui.explorer.view;

import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.metadata.MetaDataSetUtils;
import ijfx.service.ImagePlaneService;
import ijfx.service.batch.SegmentedObject;
import ijfx.service.overlay.OverlayDrawingService;
import ijfx.service.overlay.OverlayUtilsService;
import ijfx.service.preview.PreviewService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.explorer.AbstractExplorable;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import mongis.utils.CallbackTask;
import net.imagej.Dataset;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ColorTable8;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class SegmentedObjectExplorerWrapper extends AbstractExplorable {

    private final SegmentedObject object;

    @Parameter
    private ImagePlaneService imagePlaneService;

    @Parameter
    private OverlayDrawingService overlayDrawingService;

    @Parameter
    private PreviewService previewService;

    @Parameter
    private OverlayUtilsService overlayUtilsService;

    @Parameter
    private LoadingScreenService loadingScreenService;

    public SegmentedObjectExplorerWrapper(SegmentedObject object) {
        this.object = object;
    }

    @Override
    public String getTitle() {
        return object.getOverlay().getName();
    }

    @Override
    public String getSubtitle() {
        return object.getMetaDataSet().get(MetaData.FILE_NAME).getStringValue();
    }

    @Override
    public String getInformations() {
        return "";
    }

    @Override
    public Image getImage() {
        try {
            long[] nonPlanarPosition = MetaDataSetUtils.getNonPlanarPosition(getMetaDataSet());
            Dataset dataset = imagePlaneService.openVirtualDataset(getFile());
            Dataset extractedObject = overlayDrawingService.extractObject(object.getOverlay(), dataset, nonPlanarPosition);
            double min = object.getMetaDataSet().get(MetaData.STATS_PIXEL_MIN).getDoubleValue();
            double max = object.getMetaDataSet().get(MetaData.STATS_PIXEL_MAX).getDoubleValue();
            Image image = previewService.datasetToImage((RandomAccessibleInterval<? extends RealType>) extractedObject, new ColorTable8(), min, max);
            Double sampleFactor = 100 * 100 / image.getWidth() / image.getHeight();
            sampleFactor = sampleFactor > 1 ? 1 : sampleFactor;
            return resample(image, sampleFactor);

        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Error when accessing file " + getFile().getAbsolutePath(), ioe);
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error when getting image for " + getTitle(), e);
            return null;
        }
    }

    private Image resample(Image input, double scaleFactor) {
        final int W = (int) input.getWidth();
        final int H = (int) input.getHeight();
        final double S = scaleFactor;
        final int HSample = (int) (H * scaleFactor) + 1;
        final int WSample = (int) (W * scaleFactor) + 1;

        WritableImage output = new WritableImage(
                WSample,
                HSample
        );

        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                final int argb = reader.getArgb(x, y);
                for (int dy = 0; dy < S; dy++) {
                    for (int dx = 0; dx < S; dx++) {
                        writer.setArgb((int) (x * S) + dx, (int) (y * S) + dy, argb);
                    }
                }
            }
        }

        return output;
    }

    /*
    @Override
    public Image getImage() {

        Overlay overlay = object.getOverlay();

        if (overlay instanceof PolygonOverlay) {
            PolygonOverlay po = (PolygonOverlay) overlay;

            PolygonRegionOfInterest roi = po.getRegionOfInterest();
            Polygon pol = new Polygon();

            for (int i = 0; i != roi.getVertexCount(); i++) {
                pol.addPoint(new Double(roi.getVertex(i).getDoublePosition(0)).intValue(), new Double(roi.getVertex(i).getDoublePosition(1)).intValue());
            }

            Rectangle bounds = pol.getBounds();

            //for(int i = 0;i!=)
            int border = 10;

            Canvas canvas = new Canvas(bounds.getWidth() + border * 2, bounds.getHeight() + border * 2);
            GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();

            final double xBounds = bounds.getX();
            final double yBounds = bounds.getY();
            System.out.println(bounds);
            double[] xpoints = IntStream.of(pol.xpoints).mapToDouble(x -> x - xBounds + border).toArray();
            double[] ypoints = IntStream.of(pol.ypoints).mapToDouble(y -> y - yBounds + border).toArray();

            graphicsContext2D.setFill(Color.TRANSPARENT);
            graphicsContext2D.fill();
            graphicsContext2D.setFill(Color.WHITE);

            graphicsContext2D.fillPolygon(xpoints, ypoints, xpoints.length);

            final WritableImage image = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            final SnapshotParameters params = new SnapshotParameters();

            params.setFill(Color.TRANSPARENT);
            CallbackTask run = new CallbackTask()
                    .run(() -> canvas.snapshot(params, image));

            Platform.runLater(run);

            try {
                run.get();
            } catch (Exception ex) {
                ImageJFX.getLogger().log(Level.SEVERE, null, ex);
                return null;
            }

            return image;

        } else {
            return null;
        }
    }**/
    @Override
    public void open() throws Exception {
        new CallbackTask<File, Void>()
                .setInput(getFile())
                .run(f -> {
                    overlayUtilsService.openOverlay(f, object.getOverlay());
                    return null;
                })
                .submit(loadingScreenService)
                .setName("Opening file and object...")
                .setInitialProgress(0.5)
                .start();
    }

    @Override
    public Dataset getDataset() {
        return null;
    }

    @Override
    public MetaDataSet getMetaDataSet() {
        return object.getMetaDataSet();
    }
}
