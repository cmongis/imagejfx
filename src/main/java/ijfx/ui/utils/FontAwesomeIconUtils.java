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
package ijfx.ui.utils;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author Tuan anh TRINH
 */
public class FontAwesomeIconUtils {

    public static void getImageFromFAI(FontAwesomeIconView fontAwesomeIconView, double size, WritableImage wi) {
        Platform.runLater(() -> {
            final Canvas canvas = new Canvas(size, size);
            final GraphicsContext gc = canvas.getGraphicsContext2D();
            Font font = new Font(fontAwesomeIconView.getFont().getFamily(), size);
            String unicode = FontAwesomeIcon.valueOf(fontAwesomeIconView.getGlyphName()).characterToString();
            gc.setFont(font);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFill(Color.WHITE);
            gc.fillText(unicode, size / 2, size / 2);
            final SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            final WritableImage snapshot = canvas.snapshot(params, null);
            final java.awt.image.BufferedImage bufferedImage
                    = javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, null);
            SwingFXUtils.toFXImage(bufferedImage, wi);
        }
        );
    }

    public static  WritableImage FAItoImage(FontAwesomeIconView fontAwesomeIconView, int size) {
        fontAwesomeIconView.getStyleClass().add("icon-toolbar");
        WritableImage wi = new WritableImage(size, size);

        FontAwesomeIconUtils.getImageFromFAI(fontAwesomeIconView, size, wi);
        return wi;
    }
    
    public static Node getIconView(FontAwesomeIcon icon) {
        return new FontAwesomeIconView(icon);
    }
}
