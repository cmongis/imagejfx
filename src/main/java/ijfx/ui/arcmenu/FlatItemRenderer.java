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
package ijfx.ui.arcmenu;

import ijfx.ui.arcmenu.skin.ArcItemSkin;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class FlatItemRenderer extends ArcItemSkin {

    public FlatItemRenderer(ArcItem item) {
        super(item);
    }

    @Override
    public void setSkinnable(ArcItem item) {
        super.setSkinnable(item);
        setNode(render(item));
    }

    public Shape drawArc(double centerX, double centerY, double minRadius, double maxRadius, double startAngle, double length) {
        final Path path = new Path();

        final PolarSystem ps = new PolarSystem(null,null);

        final Point2D A = ps.degreeToPolar(minRadius, startAngle);
        final Point2D B = ps.degreeToPolar(maxRadius, startAngle);
        final Point2D C = ps.degreeToPolar(maxRadius, startAngle + length);
        final Point2D D = ps.degreeToPolar(minRadius, startAngle + length);

        final MoveTo start = new MoveTo(A.getX(), A.getY());
        final LineTo AB = new LineTo(B.getX(), B.getY());

        final ArcTo BC = new ArcTo();
        BC.setX(C.getX());
        BC.setY(C.getY());
        BC.setRadiusX(maxRadius);
        BC.setRadiusY(maxRadius);
        BC.setXAxisRotation(1);
        BC.setSweepFlag(true);

        LineTo CD = new LineTo(D.getX(), D.getY());

        final ArcTo DA = new ArcTo();
        DA.setX(A.getX());
        DA.setY(A.getY());
        DA.setRadiusX(minRadius);
        DA.setRadiusY(minRadius);

        path.getElements().addAll(start, AB, BC, CD, DA);

        return path;
    }

    public Shape render(ArcItem item) {
        final Arc arc = new Arc();
        final int position = getArcMenu().getItemPosition(item);
        final int maxItem = getArcMenu().getItemCount();
        final double minRadius = getArcMenu().getMinRadius();
        final double maxRadius = getArcMenu().getMaxRadius();
        
        arc.centerXProperty().bind(getArcMenu().getCenterX());
        arc.centerYProperty().bind(getArcMenu().getCenterY());
        //final double centerX = getArcMenu().getCenterX();
        //final double centerY = getArcMenu().getCenterY();
        final double startAngle = 360 * position / maxItem;
        final double length = 360 / maxItem;
        arc.setStrokeWidth(10.0f);
        arc.setStroke(Color.GOLD);
        arc.setFill(null);
        arc.setType(ArcType.OPEN);
        arc.setStrokeType(StrokeType.INSIDE);
       // arc.setCenterX(centerX);
       // arc.setCenterY(centerY);
        arc.setRadiusX(minRadius);
        arc.setRadiusY(minRadius);
        arc.setStartAngle(startAngle);
        arc.setLength(length);
        return arc;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Node createChoiceBox() {
        return null;
    }

}
