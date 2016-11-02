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
package ijfx.ui.explorer;

import ijfx.core.imagedb.ImageRecord;
import ijfx.core.imagedb.ImageRecordService;
import ijfx.service.thumb.ThumbService;
import ijfx.ui.activity.Activity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import mongis.utils.FileUtils;
import mongis.utils.panecell.PaneCellController;
import mongis.utils.panecell.PaneIconCell;
import mongis.utils.panecell.ScrollBinder;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Activity.class, name = "file-filter-activity")
public class FileFilterActivity extends BorderPane implements Activity {

    private ScrollPane scrollPane = new ScrollPane();

    private TilePane tilePane = new TilePane();

    @Parameter
    private ThumbService thumbService;

    @Parameter
    private ImageRecordService imageRecordService;

    private PaneCellController<ImageRecord> cellPaneCtrl = new PaneCellController<>(tilePane);

    private ScrollBinder binder;
    
    public FileFilterActivity() {

        setCenter(scrollPane);
        scrollPane.setContent(tilePane);
        scrollPane.setPrefWidth(400);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tilePane.prefWidthProperty().bind(scrollPane.widthProperty());
        tilePane.setPrefTileWidth(170);
        tilePane.setPrefTileHeight(270);
        //tilePane.setPrefTileHeight(Control.USE_PREF_SIZE);
        tilePane.setVgap(5);
        tilePane.setHgap(5);
        binder = new ScrollBinder(scrollPane);
        cellPaneCtrl.setCellFactory(this::createIcon);

    }

    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public Task updateOnShow() {
        
        
       
        cellPaneCtrl.setCellFactory(this::createIcon);
        cellPaneCtrl.update(new ArrayList<>(imageRecordService.getRecords()));
        return null;
    }

    private ImageReferenceIcon createIcon() {
        return new ImageReferenceIcon();
    }

    private class ImageReferenceIcon extends PaneIconCell<ImageRecord> {

        public ImageReferenceIcon() {
            super();
            setTitleFactory(this::fetchTitle);
            setSubtitleFactory(this::fetchSubTitle);
            setImageFactory(this::fetchThumb);
        }

        public String fetchTitle(ImageRecord imageRecord) {
            return imageRecord.getFile().getName();
        }

        public String fetchSubTitle(ImageRecord imageRecord) {
            long size = imageRecord.getFile().length();
            return FileUtils.readableFileSize(size);
        }

        public Image fetchThumb(ImageRecord imageRecord) {
            try {
                return thumbService.getThumb(imageRecord.getFile(), null, 100, 100);
            } catch (IOException ex) {
                Logger.getLogger(FileFilterActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

    }

}
