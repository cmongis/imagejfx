package ijfx.service.ui.angular;
import ijfx.service.ui.AppService;
import ijfx.ui.activity.Activity;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

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
/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Activity.class,name="webapp")
public class WebAppActivityContainer implements Activity{

    WebView view;
    
    BorderPane borderPane = new BorderPane();
    
    @Parameter
    AppService appService;
    
    String currentApp;
    
    @Override
    public Node getContent() {
        
        
        
        if(view == null) {
            Platform.runLater(()->{
                view = new WebView();  
                view.prefWidthProperty().bind(borderPane.widthProperty());
                view.prefHeightProperty().bind(borderPane.heightProperty());
                borderPane.setCenter(view);
                appService.bindWebView(view);
                
                updateOnShow();
            });
        }
        return borderPane;
        
    }
    
    @Override
    public Task updateOnShow() {
        if(currentApp != null) {
            Platform.runLater(()-> appService.getHelper().loadAppOnView(currentApp, view));
        }
        return null;
        
    }

    public String getCurrentApp() {
        return currentApp;
    }

    public void setCurrentApp(String currentApp) {
        this.currentApp = currentApp;
    }
    
    
    
    
}
