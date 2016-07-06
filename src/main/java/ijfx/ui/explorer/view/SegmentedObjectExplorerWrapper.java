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
import ijfx.service.batch.SegmentedObject;
import ijfx.ui.explorer.AbstractExplorable;
import ijfx.ui.main.ImageJFX;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import mongis.utils.CallbackTask;
import net.imagej.Dataset;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PolygonOverlay;
import net.imglib2.roi.PolygonRegionOfInterest;

/**
 *
 * @author cyril
 */
public class SegmentedObjectExplorerWrapper extends AbstractExplorable {

    private final SegmentedObject object;

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
    }

    @Override
    public void open() throws Exception {
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
