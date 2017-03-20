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
package ijfx.ui.input.widgets;

import java.io.File;
import javafx.scene.Node;
import javafx.scene.control.Button;
import mongis.utils.FileButtonBinding;
import org.scijava.plugin.Plugin;
import org.scijava.widget.FileWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

/**
 *
 * @author cyril
 */
@Plugin(type = InputWidget.class)
public class FileWidgetFX extends AbstractFXInputWidget<File> implements FileWidget<Node> {

    Button button = new Button();

    FileButtonBinding binding;

    @Override
    public void set(WidgetModel model) {

        binding = new FileButtonBinding(button);

        if (model.isStyle(DIRECTORY_STYLE)) {
            binding.setMode(FileButtonBinding.Mode.FOLDER);
        } else if (model.isStyle(SAVE_STYLE)) {
            binding.setMode(FileButtonBinding.Mode.SAVE);
        } else {
            binding.setMode(FileButtonBinding.Mode.OPEN);
        }

    }

    @Override
    public Node getComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
