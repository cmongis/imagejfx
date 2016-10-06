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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import mongis.utils.TextFileUtils;

/**
 *
 * @author cyril
 */
public class RichMessageDisplayer {

    private final WebView webView;

    private static final String CSS_URL = ImageJFX.class.getResource("/web/css/bijou.min.css").toExternalForm();
    private static final String CSS_HEADER = "<html><head><link rel='stylesheet' href='%s'/></head><body style='padding:10px;margin:0'><style>%s</style>%s</body></html>";
    private final  List<String> CSS_ADDITIONAL = new ArrayList<>();
    List<Callback<String, String>> stringProcessor = new ArrayList<>();

    private final StringProperty messageProperty = new SimpleStringProperty();
    
    public RichMessageDisplayer(WebView webView) {
        this.webView = webView;
        messageProperty.addListener(this::onMessageChanged);
    }

    /**
     * Load the content of a file located in the jar (same as the reference class)
     * @param Reference class which should belong to the same jar as the file
     * @param Path relative path (to the reference) or absolute path of the loaded
     * @throws IOException 
     */
    public void setContent(Class clazz,String path) throws IOException {
        
        setMessage(TextFileUtils.readFileFromJar(clazz, path));
        
    }
    
    private void onMessageChanged(Observable obs, String oldValue, String newValue) {
        setMessage(newValue);
    }
    
    public StringProperty messageProperty() {
        return messageProperty;
    }
    
    public void setMessage(String text) {
        if (webView == null) {
            return;
        }
        if (text == null) {
            return;
        }
        for (Callback<String, String> processor : stringProcessor) {
            
            if(processor == null) continue;
            try {
                
                String result = processor.call(text);
                if (result != null) {
                    text = result;
                }
            } catch (Exception e) {
                ImageJFX.getLogger().log(Level.WARNING, "Error when trying to process text", e);

            }
           
        }
        webView
                .getEngine()
                .loadContent(String.format(CSS_HEADER, ImageJFX.class.getResource("/web/scss/bijou.min.css").toExternalForm(),CSS_ADDITIONAL.stream().collect(Collectors.joining(";\n")), text));
    }

    public RichMessageDisplayer addStringProcessor(Callback<String, String> callback) {
        stringProcessor.add(callback);
        return this;
    }
    
    public RichMessageDisplayer addCss(String... css) {
        for(String s : css) CSS_ADDITIONAL.add(s);
        return this;
    }
    
    public static Callback<String, String> BACKLINE_ANDS_AND_ORS = txt -> txt.replaceAll(" (and|or) ", "<br><br><span style='text-transform:uppercase'>$1</span><br><br> - ");

    public static Callback<String, String> FORMAT_SECTION_TITLES = txt -> txt.replaceAll("^== (.*)$", "<h3>$1</h3>");
    public static Callback<String, String> FORMAT_BOLD = txt -> txt.replaceAll("\\*\\*([^\\*]*)\\*\\*", "<b>$1</b>");
    public static Callback<String, String> COLOR_IMPORTANT_WORDS = txt -> txt.replaceAll("\\*([^\\*\\n]*)\\*", "<span class='warning'>$1</span>");

    public static Callback<String, String> BIGGER = txt -> String.format("<span style='font-size:1.2em'>%s</span>", txt);

    public static Callback<String, String> REPLACE_BACKLINE_WITH_BR = txt -> txt.replaceAll("\n", "<br>");

}
