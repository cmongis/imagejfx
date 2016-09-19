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
package ijfx.ui.correction;

import com.github.rjeschke.txtmark.Processor;
import ijfx.service.ui.HintService;
import ijfx.ui.RichMessageDisplayer;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.datadisplay.image.ImageDisplayPane;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.action.BackAction;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Tuan anh TRINH
 */
public class CorrectionFlow {
    
    @Parameter
    HintService hintService;
    
    @Parameter
    ActivityService activityService;
    
    @FXML
    BorderPane borderPane;
    
    @FXML
    @BackAction
    protected Button backButton;
    
    @FXML
    @ActionTrigger("nextAction")
    protected Button nextButton;
    
    @FXML
    @ActionTrigger("explainMe")
    protected Button explainMeButton;
    
    @FXML
    @ActionTrigger("finishAction")
    protected Button finishButton;
    
    public void bindPaneProperty(List<ImageDisplayPane> imageDisplayPanes) {
        imageDisplayPanes.stream().filter(e -> e != imageDisplayPanes.get(0)).forEach(imageDisplayPane -> {
            ImageDisplayPane first = imageDisplayPanes.get(0);
            first.getCanvas().getCamera().zoomProperty().bindBidirectional(imageDisplayPane.getCanvas().getCamera().zoomProperty());
            
            first.getCanvas().getCamera().xProperty().bindBidirectional(imageDisplayPane.getCanvas().getCamera().xProperty());
            
            first.getCanvas().getCamera().yProperty().bindBidirectional(imageDisplayPane.getCanvas().getCamera().yProperty());
        });
    }


    public void setCellFactory(ListView<File> listView) {
        listView.setCellFactory((ListView<File> param) -> {
            ListCell<File> cell = new ListCell<File>() {
                @Override
                protected void updateItem(File file, boolean b) {
                    super.updateItem(file, b);
                    if (file != null) {
                        setText(file.getName());
                    } else {
                        setText("");
                    }
                }
                
            };
            return cell;
        });
    }
    
    @ActionMethod("explainMe")
    protected void help() {
        hintService.displayHints(this.getClass(), true);
    }
    
    
    protected RichMessageDisplayer displayer;
    
    public void initWebView(WebView webView,String resourceUrl) {
        try {
            
            webView.setPrefHeight(150);
            displayer = new RichMessageDisplayer(webView).addStringProcessor(Processor::process);
            displayer.setContent(this.getClass(), resourceUrl);
        } catch (IOException ex) {
            Logger.getLogger(FolderSelection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
