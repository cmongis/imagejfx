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
package ijfx.ui;

import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.web.WebView;
import javafx.util.Callback;

/**
 *
 * @author cyril
 */
public class RichMessageDisplayer {
    private final WebView webView;

     private static final String CSS_URL = ImageJFX.class.getResource("/web/css/bijou.min.css").toExternalForm();
    private static final String CSS_HEADER = "<html><head><link rel='stylesheet' href='%s'/></head><body style='padding:10px;margin:0'>%s</body></html>";

    
    List<Callback<String,String>> stringProcessor = new ArrayList<>();
    
    public RichMessageDisplayer(WebView webView) {
        this.webView = webView;
    }
    
    public void setMessage(String text) {
        if(webView == null) return;
        if(text == null) return;
        for(Callback<String,String> processor : stringProcessor) {
            String result = processor.call(text);
            if(result != null) text = result;
            //text = processor.call(text);
        }
         webView
                 .getEngine()
                 .loadContent(String.format(CSS_HEADER, ImageJFX.class.getResource("/web/scss/bijou.min.css").toExternalForm(),text));
    }
    
    public RichMessageDisplayer addStringProcessor(Callback<String,String> callback) {
        stringProcessor.add(callback);
        return this;
    }
    public static Callback<String,String> BACKLINE_ANDS_AND_ORS = txt->txt.replaceAll(" (and|or) ", "<br><br><span style='text-transform:uppercase'>$1</span><br><br> - ");
    
    public static Callback<String,String> COLOR_IMPORTANT_WORDS = txt->txt.replaceAll("\\*([^\\*]*)\\*","<span class='warning'>$1</span></b>");
    
    public static Callback<String,String> BIGGER = txt->String.format("<span style='font-size:1.2em'>%s</span>",txt);
    
    
}

