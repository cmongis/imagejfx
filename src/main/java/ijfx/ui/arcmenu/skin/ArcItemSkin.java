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
package ijfx.ui.arcmenu.skin;

import ijfx.ui.arcmenu.ArcItem;
import ijfx.ui.arcmenu.ArcMenu;
import javafx.scene.Node;
import javafx.scene.control.Skin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public abstract class ArcItemSkin implements Skin<ArcItem> {
     
    protected ArcItem skinnable;

    protected Node node;
    
    public final static String CSS_ARC_ITEM_SHAPE = "arc-item-shape";
    public final static String CSS_ARC_ITEM_ICON = "arc-item-icon";
    public final static String CSS_ARC_ITEM_HOVER = "arc-item-hover";
    public final static String CSS_ARC_ITEM_CHOICE_BOX = "arc-item-choice-box";
    public final static String CSS_ARC_ITEM_CHOICE_BOX_HOVER = "arc-item-choice-box-hover";
    public final static String CSS_ARC_MENU = "arc-menu";
    
    public ArcItemSkin(ArcItem item) {
        setSkinnable(item);

    }

    public void setSkinnable(ArcItem item) {
        skinnable = item;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public ArcItem getSkinnable() {
        return skinnable;
    }

    public ArcMenu getArcMenu() {
        return getSkinnable().getArcMenu();
    }

    public abstract Node createChoiceBox();

}
