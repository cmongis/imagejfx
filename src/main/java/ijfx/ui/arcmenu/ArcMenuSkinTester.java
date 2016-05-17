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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.ui.utils.AbstractApp;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author cyril
 */
public class ArcMenuSkinTester extends AbstractApp {

    PopArcMenu arcMenu = new PopArcMenu();

    Rectangle rectangle = new Rectangle();

    public ArcMenuSkinTester() {
        super();

        rectangle.setFill(Color.DARKCYAN);
        rectangle.setWidth(400);
        rectangle.setHeight(400);
        
        setContent(rectangle);
        
        
        
    }

    @Override
    public void initApp() {

        addAction("Toggle", this::toggle);
        addAction("Reset menu", this::reset);
        reset();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void toggle() {
        /*
        if (arcMenu.isShowing()) {
            arcMenu.hide();
        } else {
            arcMenu.show(label);
        }*/

    }

    public void reset() {
        
        
        
        
        arcMenu = new PopArcMenu();
        rectangle.setOnMouseClicked(arcMenu::show);
        arcMenu.addItem(new ArcItem("Icon 1", FontAwesomeIcon.COG));
        arcMenu.addItem(new ArcItem("Icon 2", FontAwesomeIcon.ANGELLIST, new String[]{"Choice 1", "Choice 2", "Choice 3"}));
        arcMenu.addItem(new ArcItem("Icon 3", FontAwesomeIcon.AMAZON, 2d, 15d, 2d));
        arcMenu.build();
    }

}
