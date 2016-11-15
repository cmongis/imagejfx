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
package ijfx.ui.plugins;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.core.imagedb.ImageLoaderService;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.view.IconView;
import ijfx.ui.main.Localization;
import ijfx.ui.widgets.ExplorableButton;
import ijfx.ui.widgets.FileExplorableWrapper;
import ijfx.ui.widgets.PopoverToggleButton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import mongis.utils.panecell.PaneCell;
import mongis.utils.panecell.SimplePaneCell;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "same-folder-button", context = "imagej+image-open", localization = Localization.TOP_LEFT)
public class SameFolderButton extends ToggleButton implements UiPlugin {

    @Parameter
    Context context;

    IconView iconView = new IconView();

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    ImageLoaderService imageLoaderService;

    @Parameter
    ExplorerService explorerService;

    int radius = 2;

    PopOver popOver;

    public SameFolderButton() {
        iconView.setPrefWidth(830);
        iconView.setPrefHeight(200);
        iconView.setCellFactory(this::createIcon);
        setText("Quick open...");
        //iconView.setCellFactory(RecentFilePanel.FileIconCell::new);
        popOver = PopoverToggleButton
                .bind(this, iconView, PopOver.ArrowLocation.TOP_LEFT);
        popOver
                .showingProperty()
                .addListener(this::onPopOverShowing);
        iconView.setTileDimension(150, 150, 0,0);
    }

    private void onPopOverShowing(Observable obs, Boolean oldValue, Boolean showing) {
        if (showing) {
            update();
        }
    }

    private PaneCell<Explorable> createIcon() {

        return new SimplePaneCell<Explorable>()
                .setTitleFactory(Explorable::getTitle)
                .setImageFactory(Explorable::getImage)
                .setWidth(120)
                .setOnMouseClicked(this::open);

    }

    private void open(Explorable explorable) {
        popOver.hide();

        explorerService.open(explorable);
    }

    private void update() {

        Dataset dataset = imageDisplayService.getActiveDataset();

        File file = new File(dataset.getSource());

        List<File> toShow = new ArrayList<>();

        if (file.exists()) {

            File parent = file.getParentFile();

            List<File> files = new ArrayList(imageLoaderService.getAllImagesFromDirecoty(parent, false));

            if (files.size() > radius * 2) {
                int filePosition = indexOf(files, file);
                int maxIndex = files.size() - 1;
                int minIndex = 0;
                int start = filePosition - radius;
                if (filePosition - radius < minIndex) {
                    start = minIndex;
                }
                if (filePosition + radius > maxIndex) {
                    start = maxIndex - (radius * 2) - 1;
                }

                for (int i = start; i != start + (radius * 2) + 1; i++) {

                    //if (files.get(i).equals(file) != true) {
                    toShow.add(files.get(i));
                    //}
                    //else {
                    //   toShow.add();
                    //}

                }

            } else {
                files
                        .stream()
                        .forEach(toShow::add);
            }

        }

        iconView.setItem(
                toShow
                        .stream()
                        .map(f -> {
                            if (f.equals(file)) {
                                return new ExplorableButton("<- Current file ->", "", FontAwesomeIcon.SQUARE);
                            } else {
                                return new FileExplorableWrapper(context, f);
                            }
                        })
                        .collect(Collectors.toList())
        );

    }

    private <T> int indexOf(List<T> list, T t) {

        for (int i = 0; i != list.size(); i++) {

            if (t.equals(list.get(i))) {
                return i;
            }
        }
        return -1;

    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {

        context.inject(iconView);

        return this;
    }

}
