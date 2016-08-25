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
package ijfx.ui.tool.overlay;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import net.imagej.overlay.RectangleOverlay;

/**
 *
 * @author cyril
 */
@Deprecated
public class RectangleRepresentation extends OverlayRepresentationBase<RectangleOverlay> {

    
    Rectangle rectangle = new Rectangle();
    
    Path path = new Path();
    
    ObservableList<Node> group = FXCollections.observableArrayList();
    Group subGroup = new Group();
    MoveablePoint a = new MoveablePoint();
    MoveablePoint b = new MoveablePoint();
    
    
    
    public RectangleRepresentation() {
        subGroup.getChildren().add(rectangle);
        //group.setAutoSizeChildren(false);
       
        //MoveablePoint c = new MoveablePoint();
        //MoveablePoint d = new MoveablePoint();
        
        DoubleBinding width = Bindings.createDoubleBinding(this::getRectangleWidth, a.xProperty(),b.yProperty());
        DoubleBinding height = Bindings.createDoubleBinding(this::getRectangleHeight, a.yProperty(),b.yProperty());
        
        rectangle.xProperty().bind(a.xProperty());
        rectangle.yProperty().bind(a.yProperty());
        rectangle.setStroke(Color.YELLOW);
        rectangle.setStrokeWidth(2);
        rectangle.widthProperty().bind(width);
        rectangle.heightProperty().bind(height);
//c.setY(200);
        //d.setY(200);
        subGroup.getChildren().addAll(a,b);
        group.addAll(subGroup);
        
    }
    
    
    
    //@Override
    public ObservableList<Node> getRepresentation() {
        return group;
    }

    @Override
    public void update(RectangleOverlay overlay) {
        
        double x = a.getX() + subGroup.getTranslateX();
        double y = a.getY() + subGroup.getTranslateY();
        
        double width = rectangle.getWidth();
        double height = rectangle.getHeight();
        
        
        
        
        
        System.out.printf("Final : (%.0f x %.0f)\n",x,y);
       
        
    }

    @Override
    public void updateFrom(RectangleOverlay overlay) {
        double width = overlay.getExtent(0);
        double height = overlay.getExtent(1);
        
        System.out.printf("overlay : %.3f x %.0f\n",width,height);
        
        a.setX(0);
        a.setY(0);
        b.setX(width*zoomProperty().getValue());
        b.setY(height*zoomProperty().getValue());
        

        
    }
    
    private double getRectangleWidth() {
      
        return b.getX() - a.getX();
       
        
    }
    private double getRectangleHeight() {
        
        return b.getY() - a .getY();
    }

    @Override
    public ObservableList<Node> getControllers() {
        return null;
    }
    
    
    

   
    
}
