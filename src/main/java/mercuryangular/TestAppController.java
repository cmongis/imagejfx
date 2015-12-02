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
package mercuryangular;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import mercury.core.AngularMethod;
import mercury.core.LogEntry;
import mercury.helper.MercuryHelper;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class TestAppController implements Initializable {
    
    @FXML
    private WebView webView;
    
    @FXML
    private WebView docView;
    
    
   @FXML
   private BorderPane borderPane;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
      
        
      MercuryHelper helper = new MercuryHelper();
      helper.registerService("ControllerService", this);
      helper.getAppLoader().scanDirectory("../ijfx-web/");
      helper.bindWebView(webView);
      helper.bindWebView(docView);
      helper.loadAppOnView("intro", webView);
      helper.loadAppOnView("documentation", docView);
       
      helper.getBinder().getJsMessageList().addListener(new ListChangeListener<LogEntry>() {
          @Override
          public void onChanged(ListChangeListener.Change<? extends LogEntry> c) {
              
              c.next();
              c.getAddedSubList().forEach(msg->System.out.println(msg));
              
          }
      });
        
     // webView.getEngine().load("http://localhost:8000");
        
       
    }    
    
    @FXML
    @AngularMethod(sync = true)
    public void reload(ActionEvent e) {
        webView.getEngine().reload();
        docView.getEngine().reload();
    }
    
}
