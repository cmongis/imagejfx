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
package ijfx.ui.datadisplay.table;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.main.Localization;
import ijfx.ui.utils.FontAwesomeIconUtils;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "imagej-top-toolbar-table-display", context = "imagej+table-open", order = 1000.0, localization = Localization.TOP_TOOLBAR)
public class TableDisplayToolbar extends BorderPane implements UiPlugin {

    @Parameter
    CommandService commandService;

    public TableDisplayToolbar() {
        super();
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {
        addElement();
        return this;
    }

    //To Change
    public void addElement() {
        Button button = new Button("save");
        FontAwesomeIconView fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
        Image image = FontAwesomeIconUtils.FAItoImage(fontAwesomeIconView, 50);
        button.setGraphic(new ImageView(image));
        button.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            CommandInfo commandInfo = new CommandInfo(SaveCSV.class);
            commandService.run(commandInfo, true);
        });
        this.setTop(button);
    }

}
