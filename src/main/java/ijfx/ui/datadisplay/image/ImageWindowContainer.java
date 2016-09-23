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
package ijfx.ui.datadisplay.image;

import ij.gui.ImageWindow;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import jfxtras.scene.control.window.Window;


/**
 *
 * @author Cyril MONGIS, 2015
 */
public class ImageWindowContainer extends AnchorPane {

    public static ImageWindowContainer displayContainer;

    protected ImageWindowContainer() {
        super();

        getChildren().addListener((ListChangeListener.Change<? extends Node> change) -> {

            while (change.next()) {
                change.getAddedSubList()
                        .stream()
                        .filter(node -> node instanceof Window)
                        .map(node -> (Window) node)
                        .forEach(window -> {
                            double move = getChildren().size() * 15;
                            window.relocate(move, move);
                        });
            };

            if (getChildren().size() == 0) {
                return;
            }
            Node onFront = getChildren().get(getChildren().size() - 1);

           
        });

    }

    public static ImageWindowContainer getInstance() {
        if (displayContainer == null) {
            displayContainer = new ImageWindowContainer();
        }

        return displayContainer;
    }

}
