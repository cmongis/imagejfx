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
package ijfx.ui.tool;

import ijfx.ui.canvas.FxImageCanvas;
import javafx.scene.Cursor;
import javafx.scene.Node;
import org.scijava.plugin.SciJavaPlugin;

/**
 * @FxTool determines an interface for adding tools during ImageJ editing.
 * 
 * FxTool are JavaFX provide JavaFX components and actions on the JavaFX Canvas.
 * Each a tool is selected, the the method @FxTool.subscribe is called, allowing the tool
 * to add events to the canvas. When an other tool is selected, the @FxTool.unsubscribe is called
 * and should remove the listeners from the canvas. 
 * 
 * I suggest you to check the @AbstractPathTool to get a better insight of the
 * possibilities.
 * 
 * 
 * @author Cyril MONGIS, 2015
 */
public interface FxTool extends SciJavaPlugin {

    public void update(FxTool currentTool);
    
    public void subscribe(FxImageCanvas canvas);

    public void unsubscribe(FxImageCanvas canvas);

    public Node getNode();
    
    default public Cursor getDefaultCursor() {
        return Cursor.DEFAULT;
    }
    
}
