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
package ijfx.ui.arcmenu;

import ijfx.ui.arcmenu.skin.ArcItemCircleSkin;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;

/**
 *
 * @author cyril
 */
public class PopArcMenu extends PopupControl {

    private double minRadius = 45.0f;
    private double maxRadius = 70.0f;
    private final DoubleProperty centerX  = new SimpleDoubleProperty(0.0f);
    private final DoubleProperty centerY = new SimpleDoubleProperty(0.0f);

   
    
    private final ArrayList<ArcItem> items = new ArrayList<>();

    
    public PopArcMenu() {
        super();
        setSkin(createDefaultSkin());
        
        
     
        
        
    }
    
    @Override
    public Skin<?> createDefaultSkin() {
        return new ArcMenu(this);

    }

    /**
     * Add an ArcMenuItem
     *
     * @param item
     * @return the menu iteself
     */
    public PopArcMenu addItem(ArcItem item) {
        items.add(item);
        item.setArcRenderer(this);
        item.setSkin(new ArcItemCircleSkin(item));
        return this;
    }

    public ArrayList<ArcItem> getItems() {
        return items;
    }

    /**
     *
     * @param items
     */
    public void addAll(ArcItem... items) {
        for (ArcItem item : items) {
            addItem(item);
        }

    }

    public void build() {
        ((ArcMenuSkin) getSkin()).build();
    }

    /**
     *
     * @return
     */
    public double getMinRadius() {
        return minRadius;
    }

    /**
     *
     * @param minRadius
     */
    public void setMinRadius(double minRadius) {
        this.minRadius = minRadius;
    }

    /**
     *
     * @return
     */
    public double getMaxRadius() {
        return maxRadius;
    }

    /**
     *
     * @param maxRadius
     */
    public void setMaxRadius(double maxRadius) {
        this.maxRadius = maxRadius;
    }

    /**
     *
     * @return
     */
    public ReadOnlyDoubleProperty getCenterX() {
        return  centerX;
    }

    /**
     *
     * @param centerX
     */
    protected void setCenterX(double centerX) {
        this.centerX.setValue(centerX);
    }

    /**
     *
     * @return
     */
    public ReadOnlyDoubleProperty getCenterY() {
        return centerY;
    }

    /**
     *
     * @param centerY
     */
    protected void setCenterY(double centerY) {
        this.centerY.setValue(centerY);
    }
    
    public List<Node> getChildren() {
        return ((ArcMenu)getSkin()).getChildren();
    }

    
    int getItemPosition(ArcItem item) {
        return getItems().indexOf(item);
    }

    int getItemCount() {
        return getItems().size();
    }
    
    public void show(Node node) {
        super.show(node, node.getScene().getWindow().getX(), node.getScene().getWindow().getY());
    }
    
    public void show(MouseEvent event) {
        
        if(event.getButton() != MouseButton.SECONDARY) return;
        System.out.println("showing for sure");
        Rectangle2D visualBounds = Screen.getScreensForRectangle(event.getScreenX(), event.getScreenY(), 20, 20).get(0).getVisualBounds();
        
        Node node = (Node) event.getTarget();
        Scene scene = node.getScene();
        
        setStyle("-fx-background-color:blue");
      
        
        
        
        double margin = 400;
        
        
        //xProperty().bind(scene.getWindow().xProperty());
        
       double wx,wy,ww,wh;
        
        wx = scene.getWindow().getX();
        wy = scene.getWindow().getY();
        ww = scene.getWidth();
        wh = scene.getHeight();
        
        
        setPrefWidth(ww);
        setPrefHeight(wh);
        
        double x = event.getSceneX();
        double y = event.getSceneY();
        System.out.println(getHeight());
        System.out.println(y);
        x-= ww/2;
        y-= wh/2;
       
      
        setCenterX(x);
        setCenterY(y);
        
        super.show(scene.getWindow(),wx,wy);
        setX(wx);
        setY(wy);
        setAnchorX(wx);
        setAnchorY(wy);
        
        List<String> styleClasses = getScene().getRoot().getStyleClass();
        System.out.println("It does something !");
        if(styleClasses.contains(TRANSPARENT_CLASS) == false) styleClasses.add(TRANSPARENT_CLASS);
      
        //super.show(((Node)event.getTarget()).getScene().getWindow(),visualBounds.getMinX()+(margin/2),visualBounds.getMinY()+(margin / 2));
    }
    
    
    
    public static final String TRANSPARENT_CLASS = "transparent";
    
}
