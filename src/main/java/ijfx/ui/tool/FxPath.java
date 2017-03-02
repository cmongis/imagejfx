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
package ijfx.ui.tool;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class FxPath {

    ArrayList<Point2D> pathOnScreen = new ArrayList<>();
    ArrayList<Point2D> pathOnImage = new ArrayList<>();

    int numPoints = 0;
    
    public ArrayList<Point2D> getPathOnScreen() {
        return pathOnScreen;
    }

    public ArrayList<Point2D> getPathOnImage() {
        return pathOnImage;
    }

    public void newPoint(Point2D onScreen, Point2D onImage) {
        pathOnImage.add(onImage);
        pathOnScreen.add(onScreen);
        numPoints++;
    }

    public static double[] xList(List<Point2D> pointList) {
        double[] xList = new double[pointList.size()];

        for (int i = 0; i != xList.length; i++) {
            xList[i] = pointList.get(i).getX();
        }

        return xList;
    }

    public static double[] yList(List<Point2D> pointList) {
        double[] yList = new double[pointList.size()];

        for (int i = 0; i != yList.length; i++) {
            yList[i] = pointList.get(i).getY();
        }

        return yList;
    }
    
    public int size() {
        return numPoints;
    }

    public static Polygon toPolygon(List<Point2D> pointList) {

        Polygon polygon = new Polygon();

        for (Point2D p : pointList) {
            polygon.addPoint((int) Math.round(p.getX()), (int) Math.round(p.getY()));
        }

        return polygon;

    }

    public static Point2D getFirst(List<Point2D> pointList) {
        return pointList.get(0);
    }

    public static Point2D getLast(List<Point2D> pointList) {
        int last = pointList.size() - 2;
        return pointList.get(last > 0 ? last : 0);
    }

    public static Rectangle2D toRectangle(List<Point2D> pointList) {
        return toRectangle(getFirst(pointList), getLast(pointList));
    }

    public static Rectangle2D toRectangle(Point2D p1, Point2D p2) {

        double x;
        double y;

        double w;
        double h;

        if (p1.getX() < p2.getX()) {
            x = p1.getX();
        } else {
            x = p2.getX();
        }

        if (p1.getY() < p2.getY()) {
            y = p1.getY();
        } else {
            y = p2.getY();
        }

        w = Math.abs(p1.getX() - p2.getX());
        h = Math.abs(p1.getY() - p2.getY());

        return new Rectangle2D(x, y, w, h);

    }

    public void removeLast() {
        pathOnScreen.remove(pathOnScreen.size()-1);
        pathOnImage.remove(pathOnImage.size()-1);
    }
    
}
