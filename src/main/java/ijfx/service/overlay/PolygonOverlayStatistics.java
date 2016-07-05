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
package ijfx.service.overlay;


import ij.blob.RotatingCalipers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

import javafx.scene.shape.Polygon;
import javafx.geometry.Point2D;


import net.imagej.display.ImageDisplay;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PolygonOverlay;

import net.imglib2.roi.PolygonRegionOfInterest;
import org.scijava.Context;

/**
 *
 * @author Pierre BONNEAU
 */
public class PolygonOverlayStatistics extends AbstractOverlayShapeStatistics{
    
    java.awt.Polygon shape;
    Polygon convexHull;

    /*
    public PolygonOverlayStatistics(ImageDisplay display, Overlay overlay, Context context){
        
        
      
        
        
        this.shape = getShape(getOverlay());
        this.convexHull = getConvexHull();
        
        super.area = setArea();
        super.minimumBoundingRectangle = setMinimumBoundingRectangle();
        super.centerOfGravity = setCenterOfGravity();
        super.feretDiameter = setFeretDiameter();
        super.minFeretDiameter = setMinFeretDiameter();
        super.longSideMBR = setLongSideMBR();
        super.shortSideMBR = setShortSideMBR();
        super.aspectRatio = super.setAspectRatio();
        super.convexity = setConvexity();
        super.solidity = setSolidity();
        super.circularity = setCircularity();
        super.thinnesRatio = setThinnesRatio();
    }*/
    
    
    public PolygonOverlayStatistics(Overlay overlay, Context context){
        
        super(overlay, context);
        
        this.shape = getShape(getOverlay());
       
        this.convexHull = getConvexHull();
        
        super.area = setArea();
        super.minimumBoundingRectangle = setMinimumBoundingRectangle();
        super.centerOfGravity = setCenterOfGravity();
        super.feretDiameter = setFeretDiameter();
        super.minFeretDiameter = setMinFeretDiameter();
        super.longSideMBR = setLongSideMBR();
        super.shortSideMBR = setShortSideMBR();
        super.aspectRatio = super.setAspectRatio();
        super.convexity = setConvexity();
        super.solidity = setSolidity();
        super.circularity = setCircularity();
        super.thinnesRatio = setThinnesRatio();
    }    
    
    
    public java.awt.Polygon getShape(Overlay overlay){
        
        PolygonRegionOfInterest roi = (PolygonRegionOfInterest) overlay.getRegionOfInterest();
        
        int npoints = roi.getVertexCount();
        int[] xpoints = new int[npoints];
        int[] ypoints = new int[npoints];
        
        for (int i = 0; i < npoints; i++) {
//            Point currentPoint = new Point((int)roi.getVertex(i).getDoublePosition(0), (int)roi.getVertex(i).getDoublePosition(1));
            xpoints[i] = (int)roi.getVertex(i).getDoublePosition(0);
            ypoints[i] = (int)roi.getVertex(i).getDoublePosition(1);

        }
        
        java.awt.Polygon shape = new java.awt.Polygon(xpoints, ypoints, npoints);
        
        return shape;
    }
    
    
    public Polygon setMinimumBoundingRectangle(){
        
        java.awt.geom.Point2D.Double[] mbr;
        try{
            mbr = RotatingCalipers.getMinimumBoundingRectangle(shape.xpoints, shape.ypoints);
        }
        catch(IllegalArgumentException e){
            return null;
        }

        
        Double[] p = new Double[8];
        for(int i = 0; i < mbr.length; i++){
            p[i*2] = mbr[i].x;
            p[i*2+1] = mbr[i].y;
        }

        Polygon polygon = new Polygon();
        polygon.getPoints().addAll(p);
        return polygon;
    }
    
    
    public double setArea() {
        if (shape==null) return Double.NaN;
        
        int carea = 0;
        int iminus1;

        for (int i=0; i<shape.npoints; i++) {
            iminus1 = i-1;
            if (iminus1<0) 
                iminus1=shape.npoints-1;
            carea += (shape.xpoints[i]+shape.xpoints[iminus1])*(shape.ypoints[i]-shape.ypoints[iminus1]);
        }

        return (Math.abs(carea/2.0));
    }
    
    
    public Point2D setCenterOfGravity(){
        
        int[] x = shape.xpoints;
        int[] y = shape.ypoints;
        int sumx = 0;
        int sumy = 0;
        double A = 0.0;
        
        int iplus1;
        
        for(int i = 0; i < shape.npoints; i++){
            iplus1 = i + 1;
            if (iplus1 == shape.npoints)
                iplus1 = 0;
            
            int cross = (x[i]*y[iplus1]-x[iplus1]*y[i]);
            sumx = sumx + (x[i]+x[iplus1])*cross;
            sumy = sumy + (y[i]+y[iplus1])*cross;
            A = A + cross;
        }
        A = 0.5*A;
        Point2D centerOfGravity = new Point2D(sumx/(6*A), sumy/(6*A));
        
        return centerOfGravity;
    }
    
    
    public double setFeretDiameter(){
        
        double diameter = 0.0;
        
        double dx, dy, d;
        
        for(int i = 0; i<shape.npoints;i++){
            for(int j = i; j<shape.npoints;j++){
                dx = shape.xpoints[i] - shape.xpoints[j];
                dy = shape.ypoints[i] - shape.ypoints[j];
                d = Math.sqrt(dx*dx + dy*dy);
                if (d>diameter)
                    diameter = d;
            }
        }
        return diameter;
    }
    
    
    public double setMinFeretDiameter(){
        
        double min = Double.MAX_VALUE;
        
        java.awt.geom.Point2D.Double[] mbr;
        try{
            mbr = RotatingCalipers.getMinimumBoundingRectangle(shape.xpoints, shape.ypoints);
        }
        catch(IllegalArgumentException e){
            mbr = null;
            e.printStackTrace();
        }
        
        double dxWidth = mbr[1].x - mbr[0].x;
        double dyWidth = mbr[1].y - mbr[0].y;
        
        double dxHeight = mbr[2].x - mbr[1].x;
        double dyHeight = mbr[2].y - mbr[1].y;
        
        double cx = Math.sqrt(dxWidth*dxWidth + dyWidth*dyWidth);
        double cy = Math.sqrt(dxHeight*dxHeight + dyHeight*dyHeight);
        
        int n = shape.npoints;
        
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i=0; i<n; i++) {
            x[i] = (shape.xpoints[i]-cx);
            y[i] = (shape.ypoints[i]-cy);
        }
        
        double xr, yr;
        for (double a=0; a<=90; a+=0.5) { // rotate calipers in 0.5 degree increments
            double cos = Math.cos(a*Math.PI/180.0);
            double sin = Math.sin(a*Math.PI/180.0);
            double xmin=Double.MAX_VALUE, ymin=Double.MAX_VALUE;
            double xmax=-Double.MAX_VALUE, ymax=-Double.MAX_VALUE;
            for (int i=0; i<n; i++) {
                xr = cos*x[i] - sin*y[i];
                yr = sin*x[i] + cos*y[i];
                if (xr<xmin)
                    xmin = xr;
                if (xr>xmax)
                    xmax = xr;
                if (yr<ymin)
                    ymin = yr;
                if (yr>ymax)
                    ymax = yr;
            }
            double width = xmax - xmin;
            double height = ymax - ymin;
            double min2 = Math.min(width, height);
            min = Math.min(min, min2);
        }        
        return min;
    }
    
    
    public double setLongSideMBR(){
        
        java.awt.geom.Point2D.Double[] mbr;
        try{
            mbr = RotatingCalipers.getMinimumBoundingRectangle(shape.xpoints, shape.ypoints);
        }
        catch(IllegalArgumentException e){
            mbr = null;
            e.printStackTrace();
        }
        
        double firstSide = Math.sqrt(Math.pow(mbr[1].x - mbr[0].x, 2) + Math.pow(mbr[1].y - mbr[0].y, 2));
        double secondSide = Math.sqrt(Math.pow(mbr[2].x - mbr[1].x, 2) + Math.pow(mbr[2].y - mbr[1].y, 2));
                
        return firstSide>secondSide?firstSide:secondSide;
    }
    
    
    public double setShortSideMBR(){
        
                java.awt.geom.Point2D.Double[] mbr;
        try{
            mbr = RotatingCalipers.getMinimumBoundingRectangle(shape.xpoints, shape.ypoints);
        }
        catch(IllegalArgumentException e){
            mbr = null;
            e.printStackTrace();
        }
        
        double firstSide = Math.sqrt(Math.pow(mbr[1].x - mbr[0].x, 2) + Math.pow(mbr[1].y - mbr[0].y, 2));
        double secondSide = Math.sqrt(Math.pow(mbr[2].x - mbr[1].x, 2) + Math.pow(mbr[2].y - mbr[1].y, 2));
        
        return firstSide < secondSide ? firstSide : secondSide;
    }

    // Based on Melkman's algorithm
    public Polygon getConvexHull(){

        ArrayList<Point2D> hullVertices = new ArrayList<>();
        //Initialize the hull
      
        //Check if there are at least 3 points and make sure that all points are not colinear
        if(this.shape.npoints < 3 )
            return null;
        
        //As in the Melkman's algorithm, I don't check if the first three points are colinear.
        //If they are, I assume they have been place in the correct order (e.g. 3rd point not between 1st and 2nd)
        
        
        else{
            if(position(toPoint2D(0), toPoint2D(1), toPoint2D(2)) > 0){
                hullVertices.add(toPoint2D(0));
                hullVertices.add(toPoint2D(1));
            }
            else{
                hullVertices.add(toPoint2D(1));
                hullVertices.add(toPoint2D(0));                
            }
            hullVertices.add(0, toPoint2D(2));
            hullVertices.add(hullVertices.size(), toPoint2D(2));
        }
        
        //Compute the hull
        if(this.shape.npoints >= 4){
            for(int i = 3; i < this.shape.npoints; i++){
                Point2D current = new Point2D(this.shape.xpoints[i], this.shape.ypoints[i]);
                
                
                Point2D first = hullVertices.get(0);
                Point2D second = hullVertices.get(1);
                Point2D last = hullVertices.get(hullVertices.size()-1);
                Point2D secondToLast = hullVertices.get(hullVertices.size()-2);
                
                if(position(first, second, current) > 0 && position(secondToLast, last, current) > 0)
                    continue;
                
                else{
                    while(position(secondToLast, last, current) <= 0){
                        last = secondToLast;
                        secondToLast = hullVertices.get(hullVertices.size()-3);
                        hullVertices.remove(hullVertices.size()-1);
                    }
                    hullVertices.add(hullVertices.size(), current);
                    
                    while(position(current, first, second) <= 0){
                        first = second;
                        second = hullVertices.get(2);
                        hullVertices.remove(0);
                    }
                    hullVertices.add(0, current);
                }
            }
            hullVertices.remove(0);
        }
        
        Double[] p = new Double[hullVertices.size()*2];
        Iterator<Point2D> verticesIt = hullVertices.iterator();

        int j = 0;
        
        while(verticesIt.hasNext()){
            Point2D currentPoint = verticesIt.next();
            p[j*2] = currentPoint.getX();
            p[j*2+1] = currentPoint.getY();
            j++;
        }

        Polygon polygon = new Polygon();
        polygon.getPoints().addAll(p);
        return polygon;
        
    }
    
    
    /*
    Return the sign of the signed area of the parallelogram given by the vectors pt2-pt1 and pt3-pt1.
    */
    public int position(Point2D pt1, Point2D pt2, Point2D pt3){
        double signedArea = ( (pt2.getX() - pt1.getX()) * (pt3.getY() - pt1.getY()) ) - ( (pt3.getX() - pt1.getX()) * (pt2.getY() - pt1.getY()) );
        return (int)Math.signum(signedArea);
    }
    
    
    /*
    Convert the i-th vertex of this attribute shape to a Point2D
    Input : index of the vertex as an integer
    Return : a Point2D
    */
    public Point2D toPoint2D(int i){
        return new Point2D(this.shape.xpoints[i], this.shape.ypoints[i]);
    }
    
    
    public double setConvexity(){
        return getConvexHullPerimeter()/getPerimeter();
    }
    
    public double getPerimeter(){
        
        double perimeter = 0.0;
        
        int i;
        int iminus1;
        
        for(i = 0; i < this.shape.npoints; i++){
            iminus1 = i-1;
            if(iminus1 < 0)
                iminus1 = this.shape.npoints-1;
            
            int x1 = this.shape.xpoints[iminus1];
            int y1 = this.shape.ypoints[iminus1];
            int x2 = this.shape.xpoints[i];
            int y2 = this.shape.ypoints[i];
            
            double edge = Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
            
            perimeter = edge + perimeter;
        }
        
        return perimeter;
    }
    
    
    public double getConvexHullPerimeter(){
        
        double perimeter = 0.0;

        int i;
        int iminus1;
        
        for(i = 0; i < this.convexHull.getPoints().size()/2; i++){
            iminus1 = i-1;
            if(iminus1 < 0)
                iminus1 = (this.convexHull.getPoints().size()/2)-1;
            
            double x1 = this.convexHull.getPoints().get(iminus1*2);
            double y1 = this.convexHull.getPoints().get(iminus1*2+1);
            double x2 = this.convexHull.getPoints().get(i*2);
            double y2 = this.convexHull.getPoints().get(i*2+1);
            
            double edge = Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
            
            perimeter = edge + perimeter;        
        }
        
        return perimeter;
    }
    
    
    public double getConvexHullArea(){
        
        double area = 0.0;
        
        int i;
        int iminus1;
        
        for(i = 0; i < this.convexHull.getPoints().size()/2; i++){
            iminus1 = i-1;
            if(iminus1 < 0)
                iminus1 = (this.convexHull.getPoints().size()/2)-1;
            double t = (this.convexHull.getPoints().get(i*2)+this.convexHull.getPoints().get(iminus1*2))
                    *(this.convexHull.getPoints().get(i*2+1)-this.convexHull.getPoints().get(iminus1*2+1));
            area += t;
        }            
        return Math.abs(area/2.0);
    }
    
    
    public double setSolidity(){
        return getArea()/getConvexHullArea();
    }
    
    
    public double setCircularity(){
        return Math.pow(getPerimeter(), 2)/getArea();
    }
    
    
    public double setThinnesRatio(){

        thinnesRatio = (4*Math.PI)/getCircularity();
        thinnesRatio = (thinnesRatio>1)?1:thinnesRatio;
        
        return thinnesRatio;        
    }
}

    

