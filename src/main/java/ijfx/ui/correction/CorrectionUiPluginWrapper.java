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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.ui.RichMessageDisplayer;
import java.util.function.Consumer;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import mongis.utils.FluidWebViewWrapper;

/**
 *
 * @author cyril
 */
class CorrectionUiPluginWrapper extends TitledPane {
    
    private final CorrectionUiPlugin plugin;
    private final Button deleteButton = new Button(null, new FontAwesomeIconView(FontAwesomeIcon.REMOVE));
    private final ScrollPane contentScrollPane = new ScrollPane();
    private final BorderPane headerBorderPane = new BorderPane();
    private final StackPane headerStackPane = new StackPane();
    private RichMessageDisplayer displayer;
    private static final String HEADER_FORMAT = "<h4>%s <small>%s</small></h4>";
    private final StackPane webViewStackPane = new StackPane();
    private final StackPane contentStackPane = new StackPane();
    private Consumer<CorrectionUiPlugin> deleteHandler;
    
    private FluidWebViewWrapper webViewWrapper;
    
    public CorrectionUiPluginWrapper(CorrectionUiPlugin plugin) {
        this.plugin = plugin;
        getStyleClass().add("correction-plugin");
        
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        
        // creating the header web view
        webViewWrapper = new FluidWebViewWrapper()
                .withHeight(30);
        displayer = webViewWrapper
                .getDisplayer()
                .addStringProcessor(RichMessageDisplayer.FORMAT_SECTION_TITLES)
                .addStringProcessor(RichMessageDisplayer.COLOR_IMPORTANT_WORDS)
                .addCss("body { overflow-x:hidden;overflow-y:hidden; background:#2c3240; padding:0; }")
                .addCss("\nh4 small { font-size:16px;}")
                .addCss("\nh4 { margin-top:0;padding-top:0;");
        displayer.messageProperty().bind(Bindings.createStringBinding(this::updateHeader, plugin.explanationProperty()));
       
       
        
         updateHeader();
        // setting the content
        contentStackPane.getChildren().add(plugin.getContent());
        contentScrollPane.setContent(contentStackPane);
        
        // setting the header
        headerBorderPane.setCenter(webViewWrapper);
        headerBorderPane.setRight(deleteButton);
        headerBorderPane.setMaxWidth(Double.MAX_VALUE);
        
        headerBorderPane.prefWidthProperty().bind(Bindings.createDoubleBinding(this::getHeaderWidth,widthProperty(),deleteButton.widthProperty()));
        
        
        webViewWrapper.setOnMouseClicked(event->setExpanded(true));
        
        
         setGraphic(headerBorderPane);
        headerBorderPane.widthProperty().addListener(this::onHeaderBorderPaneChanged);
        deleteButton.setOnAction(this::delete);
        deleteButton.getStyleClass().add("danger");
        setContent(contentScrollPane);
    }

    
    public Double getHeaderWidth() {
        return getWidth() - deleteButton.getWidth() - 70;
    }
    
    public CorrectionUiPluginWrapper deleteUsing(Consumer<CorrectionUiPlugin> deleteHandler) {
        this.deleteHandler = deleteHandler;
        return this;
    }
    
    private void delete(ActionEvent event) {
        if(deleteHandler != null) deleteHandler.accept(plugin);
      
    }

    public void onHeaderBorderPaneChanged(Observable obs, Number oldValue, Number newValue) {
        System.out.println(newValue);
    }
    
    public CorrectionUiPlugin getPlugin() {
        return plugin;
    }

    private void tiesWebView(WebView webView) {
        webViewStackPane.getChildren().add(webView);
        webViewStackPane.getStyleClass().add("-bg-darker");
        setGraphic(webViewStackPane);
        webView.setOnMouseClicked((event) -> setExpanded(!isExpanded()));
       // displayer = new RichMessageDisplayer(webView)
    }

    private String updateHeader() {
        return String.format(HEADER_FORMAT, plugin.getName(), plugin.explanationProperty().getValue());
    }
    
}
