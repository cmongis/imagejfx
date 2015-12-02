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
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class ArcItemCircleSkin extends ArcItemSkin {

    public ArcItemCircleSkin(ArcItem item) {
        super(item);

    }

    Label label = new Label();

    @Override
    public void setSkinnable(ArcItem item) {
        super.setSkinnable(item);

        double minRadius = getArcMenu().getMinRadius();
        double maxRadius = getArcMenu().getMaxRadius();
        double radius = maxRadius - minRadius;

        Circle circle = new Circle(maxRadius - minRadius);
        circle.getStyleClass().add(CSS_ARC_ITEM_SHAPE);
        StackPane group = new StackPane();
        group.setMaxSize(radius, radius);
        group.getChildren().add(circle);
        Node icon = item.getIcon();
        icon.setScaleX(1.4);
        icon.setScaleY(1.5);

        icon.getStyleClass().add(CSS_ARC_ITEM_ICON);
        group.getChildren().add(icon);

        group.setOnMouseEntered(event -> group.getStyleClass().add(CSS_ARC_ITEM_HOVER));
        group.setOnMouseExited(event -> group.getStyleClass().remove(CSS_ARC_ITEM_HOVER));
        if (item.getOnMouseClicked() != null);
        group.setOnMouseReleased(item.getOnMouseClicked());


        setNode(group);
    }

    @Override
    public void dispose() {
    }

    @Override
    public Node createChoiceBox() {
        double minRadius = getArcMenu().getMinRadius();
        double maxRadius = getArcMenu().getMaxRadius();
        double radius = maxRadius - minRadius;
        radius *= 1.1;

        Circle circle = new Circle(radius);
        circle.getStyleClass().addAll(CSS_ARC_ITEM_CHOICE_BOX);
        circle.onDragEnteredProperty().addListener(event -> {

            circle.getStyleClass().add(CSS_ARC_ITEM_CHOICE_BOX_HOVER);
        });
        circle.onDragExitedProperty().addListener(event -> circle.getStyleClass().remove(CSS_ARC_ITEM_CHOICE_BOX_HOVER));

        return circle;

    }

}
