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
package ijfx.ui.plugin;

import ijfx.ui.UiPlugin;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.ui.main.ImageJFX;
import java.util.HashMap;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import net.imagej.ImageJService;
import org.scijava.command.CommandService;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugins.commands.io.OpenFile;
import ijfx.ui.UiConfiguration;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.explorer.ExplorerActivity;
import ijfx.ui.explorer.FolderManagerService;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "open-image-button", localization = "topLeftHBox", context = "imagej segment segmentation explorerActivity explorer")
public class OpenImageBar extends HBox implements UiPlugin {

    MenuButton openButton;

    Button previousButton;

    Button nextButton;

    static String OPEN_BUTTON_TXT = "Open image";

    static String PREVIOUS_BUTTON_TXT = "Previous image";

    static String NEXT_BUTTON_TXT = "Next image";//

    @Parameter
    ImageJService imageJService;

    @Parameter
    ModuleService moduleService;

    @Parameter
    CommandService commandService;

    @Parameter
    ActivityService activityService;

    @Parameter
    FolderManagerService folderManagerService;
    
    public OpenImageBar() {
        super();

        setSpacing(ImageJFX.MARGIN);

        previousButton = GlyphsDude
                .createIconButton(FontAwesomeIcon.ARROW_CIRCLE_LEFT);
        previousButton.setTooltip(new Tooltip(PREVIOUS_BUTTON_TXT));
        previousButton.getStyleClass().add("icon");
        previousButton.setOnAction(event -> activityService.back());
        
        
        openButton = new MenuButton(null, new FontAwesomeIconView(FontAwesomeIcon.FOLDER_ALT));
        
        addAction("Open image",FontAwesomeIcon.PICTURE_ALT,this::openImage);
        addAction("Explore folder",FontAwesomeIcon.COMPASS,this::explorerFolder);
        openButton.setTooltip(new Tooltip(OPEN_BUTTON_TXT));
        //openButton.setText(" ");
        openButton.getStyleClass().add("icon");
        openButton.setId("open-button");
       
        openButton.setPopupSide(Side.BOTTOM);
        
        nextButton = GlyphsDude.createIconButton(FontAwesomeIcon.ARROW_CIRCLE_RIGHT);
        nextButton.setTooltip(new Tooltip(NEXT_BUTTON_TXT));
        nextButton.getStyleClass().add("icon");
        nextButton.setOnAction(event -> nextImage());

        getChildren().addAll(previousButton, nextButton, openButton);
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {

        return this;
    }

    private void openImage(ActionEvent event) {
        commandService.run(OpenFile.class, true, new HashMap<String, Object>());
    }

    private void nextImage() {

    }

    private void previousImage() {
        activityService.forward();
    }
    
    private void addAction(String action, FontAwesomeIcon icon, EventHandler<ActionEvent> event) {
        MenuItem item = new MenuItem(action, new FontAwesomeIconView(icon));
        item.setOnAction(event);
        openButton.getItems().add(item);
    }
    
    
    private void explorerFolder(ActionEvent event) {
        
        
        File openFolder = FXUtilities.openFolder("Explorer folder", null);
        if(openFolder!=null) {
            folderManagerService.addFolder(openFolder);
            activityService.openByType(ExplorerActivity.class);
        }
    }
    
}
