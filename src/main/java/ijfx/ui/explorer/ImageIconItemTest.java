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

import java.io.File;
import java.util.Arrays;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import mercury.core.MercuryTimer;
import mongis.utils.panecell.PaneCellController;
import mongis.utils.panecell.PaneIconCell;

/**
 *
 * @author cyril
 */
public class ImageIconItemTest extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        ScrollPane scrollPane = new ScrollPane();
        TilePane tilePane = new TilePane();
        tilePane.prefWidthProperty().bind(scrollPane.widthProperty());
        scrollPane.setContent(tilePane);
        scrollPane.setPrefWidth(400);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ListView<File> fileView = new ListView();
        
        PaneCellController<File> files = new PaneCellController<>(tilePane);
        
        tilePane.setPrefTileHeight(200);
        tilePane.setPrefTileWidth(200);
        //tilePane.setPrefSize(700, 400);
        
        Scene scene = new Scene(scrollPane);
        
       
        files.setCellFactory(ImageIconItemTest::createIconItem);
         files.update(Arrays.asList(new File("/Users/cyril/Pictures").listFiles()));
        primaryStage.setScene(scene);
        primaryStage.show();
        
        
        
    }
    
    public static void main(String... args) {
        launch(args);
    }
    
    public static PaneIconCell<File> createIconItem() {
        
        MercuryTimer timer = new MercuryTimer("ImageIconItem");
        timer.start();
        PaneIconCell<File> imageIconItem =
                new PaneIconCell<File>()
                .setTitleFactory(f->f.getName())
                .setSubtitleFactory(f->f.isDirectory() ? String.format("%d files",f.listFiles().length) : String.format("%d M",f.length() / 1000 / 1000))
                .setImageFactory(f->f.getName().endsWith("png") ? new Image("file:"+f.getAbsolutePath()) : null);
        timer.elapsed("Icon item creation");
        return imageIconItem;
        
    }
    
    
}
