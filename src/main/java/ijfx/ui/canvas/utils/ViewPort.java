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
package ijfx.ui.canvas.utils;

import java.util.function.Consumer;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 *
 * @author cyril
 */
public interface ViewPort {

    void addListener(Consumer<CanvasCamera> listener);

    // height of the part extracted from the picture after taking account the zoom effect
    double getEffectiveHeight();

    // width of the part extracted from the picture after taking account the zoom effect
    double getEffectiveWidth();

    Point2D getPositionOnCamera(Point2D point);

    Point2D getPositionOnImage(Point2D point);

    Rectangle2D getSeenRectangle();

    double getZoom();

    void setZoom(double zoom);
    
}
