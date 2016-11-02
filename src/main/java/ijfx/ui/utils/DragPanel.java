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
package ijfx.ui.utils;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class DragPanel extends StackPane {
    
    
    private FontAwesomeIcon icon;
    
    private final FontAwesomeIconView view = new FontAwesomeIconView();
    
    private final Label label = new Label();
    
    private double margin = 10;
    
    private Group vBox = new Group();
    
    public DragPanel(String text, FontAwesomeIcon icon) {
        
        getStyleClass().add("drag-panel");
        
        view.getStyleClass().add("glyph");
        getChildren().addAll(view,label);
        
        
        setIcon(icon);
        setLabel(text);
        
        //view.setGlyphSize(300);
        
        //vBox.getChildren().addAll(view,label);
        //vBox.maxHeight(USE_COMPUTED_SIZE);
        
        //getChildren().add(vBox);
        
        label.translateYProperty().bind(Bindings.createDoubleBinding(this::getLabelTranslate, view.glyphSizeProperty()));
        view.translateYProperty().bind(Bindings.createDoubleBinding(this::getIconTranslate, view.glyphSizeProperty()));
        
        
    }
    
    public DragPanel setLabel(String label) {
        
        
        this.label.setText(label);
        
        return this;
    }
    
    public DragPanel setIcon(FontAwesomeIcon icon) {
        this.icon = icon;
        
        view.setIcon(icon);
        
        return this;
        
    }
    
    public double getCenterTranslate() {
        return 40;
    }
    
    public double getLabelTranslate() {
        return label.getHeight() / 2 + margin + getCenterTranslate();
    }
    
    public double getIconTranslate() {
        return -view.getGlyphSize().doubleValue() / 2 + getCenterTranslate();
    }
    
    
    
}
