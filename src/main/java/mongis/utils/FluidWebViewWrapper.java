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
package mongis.utils;

import com.github.rjeschke.txtmark.Processor;
import ijfx.ui.RichMessageDisplayer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class FluidWebViewWrapper extends StackPane{
    
    
    WebView webView;
    
    RichMessageDisplayer displayer = new RichMessageDisplayer();
    
    StringProperty content = new SimpleStringProperty();
    
    public FluidWebViewWrapper() {
        FXUtilities.createWebView().then(this::setWebView);
        //setMaxWidth(Double.MAX_VALUE);
        //setPrefWidth(USE_PREF_SIZE);

    }
    
    public FluidWebViewWrapper withHeight(double height) {
        setPrefHeight(height);
        return this;
    }
    
    public FluidWebViewWrapper forMDFiles() {
        displayer.addStringProcessor(Processor::process);
        return this;
    }
    public FluidWebViewWrapper withNoOverflow() {
        displayer.addCss("body { overflow-y:hidden; }");
        return this;
    }
    
    protected void setWebView(WebView webView) {
      
        
        double width = widthProperty().getValue();
       webView.prefHeightProperty().bind(heightProperty());
       webView.prefWidthProperty().bind(widthProperty());
       getChildren().add(webView);
         displayer.setWebView(webView);
    }
    
    
    public FluidWebViewWrapper addCssCode(String... css) {
        displayer.addCss(css);
        return this;
    }
    
    public RichMessageDisplayer getDisplayer() {
       return displayer;
    }
    
    public FluidWebViewWrapper display(Object root, String mdFile)  {
        
        try {
            displayer.setContent(root.getClass(), mdFile);
        } catch (IOException ex) {
            Logger.getLogger(FluidWebViewWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
        
    }
    
    
}
