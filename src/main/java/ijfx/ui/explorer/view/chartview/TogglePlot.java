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
package ijfx.ui.explorer.view.chartview;

import javafx.collections.ObservableList;
import javafx.scene.control.ToggleButton;

/**
 *
 * @author Tuan anh TRINH
 */
public class TogglePlot extends ToggleButton {

    static String DEFAULT_COLOR = "-fx-background-color: blue";
    String colorBackGround;

    public TogglePlot() {
        super();
        this.setStyle("-fx-background: black");
    }

    public TogglePlot(ObservableList<String> l) {
        this.getStyleClass().addAll(l);
    }

    public TogglePlot(TogglePlot togglePlot) {
        super();
        this.getStyleClass().clear();
        this.getStyleClass().addAll(togglePlot.getStyleClass());
    }
    
    public void bind(TogglePlot togglePlot){
        this.selectedProperty().addListener((obs, old, n) -> {
            togglePlot.selectedProperty().setValue(n);
            this.setStyle(togglePlot.getStyle());
            System.out.println("rer"+togglePlot.getStyle());
        });
    }

}
