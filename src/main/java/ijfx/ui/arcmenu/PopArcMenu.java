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
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.PopupWindow;
import javafx.stage.Screen;

/**
 *
 * @author cyril
 */
public class PopArcMenu extends PopupControl {

    private double minRadius = 45.0f;
    private double maxRadius = 70.0f;
    private double centerX = 0.0f;
    private double centerY = 0.0f;

    private final ArrayList<ArcItem> items = new ArrayList<>();

    
    public PopArcMenu() {
        super();
        setSkin(createDefaultSkin());
        
       
        setPrefWidth(400);
        setPrefHeight(400);
        
        
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
    public double getCenterX() {
        return centerX;
    }

    /**
     *
     * @param centerX
     */
    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    /**
     *
     * @return
     */
    public double getCenterY() {
        return centerY;
    }

    /**
     *
     * @param centerY
     */
    public void setCenterY(double centerY) {
        this.centerY = centerY;
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
        
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        setPrefWidth(visualBounds.getWidth());
        setPrefHeight(visualBounds.getHeight());
        double x = event.getScreenX();
        double y = event.getScreenY();
        System.out.println(getHeight());
        System.out.println(y);
        x-= getWidth()/2;
        y-= getHeight()/2;
        x = 0;
        y = 0;
        
        setCenterX(event.getScreenX());
        setCenterY(event.getScreenY());
        
        super.show(((Node)event.getTarget()).getScene().getWindow(),x,y);
    }
    
    
}
