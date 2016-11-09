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
package ijfx.ui.plugin.panel;

import ijfx.service.log.DefaultLoggingService;
import ijfx.service.thumb.ThumbService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.explorer.AbstractExplorable;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.Iconazable;
import ijfx.ui.explorer.view.IconView;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import mongis.utils.FileUtils;
import mongis.utils.panecell.PaneCell;
import mongis.utils.panecell.PaneIconCell;
import net.imagej.Dataset;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.io.RecentFileService;
import org.scijava.plugin.Parameter;
import org.scijava.plugins.commands.io.OpenFile;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class RecentFilePanel extends BorderPane{
    private final Context context;

    
    
    private final IconView iconView;
    
    @Parameter
    RecentFileService recentFileService;
    
    @Parameter
    ThumbService thumbService;
    
    @Parameter
    DefaultLoggingService logService;
    
    @Parameter
    CommandService commandService;
    
    @Parameter
    LoadingScreenService loadingScreenService;
    
    @Parameter
    ExplorerService explorerService;
    
    public RecentFilePanel(Context context) {
        this.context = context;
        context.inject(this);
        
        iconView = new IconView();
        context.inject(iconView);
        
       Label title = new Label("Recent files");
       title.getStyleClass().add("h2");
       title.getStyleClass().add("with-top-padding");
       getStyleClass().add("with-padding");
        setTop(title);
        setCenter(iconView);
         iconView.setCellFactory(this::createIcon);
        update();
       
    }
    
    
    private PaneCell<Iconazable> createIcon() {
        return new FileIconCell();
    }
    
    public void update() {
         iconView.setItem(
                recentFileService.getRecentFiles().stream()
                .map(str-> new FileExplorableWrapper(new File(str)))
                .collect(Collectors.toList())
        );
    }
    
    private class FileExplorableWrapper extends AbstractExplorable {

        
        private final File file;

        public FileExplorableWrapper(File file) {
            this.file = file;
        }
        
        
        @Override
        public String getTitle() {
            return file.getName();
        }

        @Override
        public String getSubtitle() {
            return FileUtils.readableFileSize(file.length());
        }

        @Override
        public String getInformations() {
            return null;
        }

        @Override
        public Image getImage() {
            if(file.getName().endsWith("png") || file.getName().endsWith("jpg")) {
                return new Image(file.getAbsolutePath());
            }
            else {
                try {
                    return thumbService.getThumb(file, 0, null,100, 100);
                } catch (Exception ex) {
                    logService.warn(ex,"Couldn't load file %s",file.getAbsolutePath());
                }
            }
            return null;
        }

        @Override
        public void open() throws Exception {
            Future<CommandModule> run = commandService.run(OpenFile.class, true, "inputFile",file);
            run.get();
        }

        @Override
        public Dataset getDataset() {
            return null;
        }
        
        public void dispose() {
            
        }
    }
    
    private class FileIconCell extends PaneIconCell<Iconazable> {
        public FileIconCell() {
            super();
            
            setImageFactory(item->item.getImage());
            setTitleFactory(item->item.getTitle());
            setSubtitleFactory(item->item.getSubtitle());
            setPrefWidth(150);
            showIconProperty().setValue(false);
            
        }
        
        @Override
        public void onSimpleClick() {
          
            explorerService.open(getItem());
            
        }
    }
    
}
