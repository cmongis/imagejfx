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

import ijfx.ui.RichMessageDisplayer;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import mongis.utils.FXUtilities;

/**
 *
 * @author cyril
 */
class CorrectionUiPluginWrapper extends TitledPane {
    
    private final CorrectionUiPlugin plugin;
    private final Button deleteButton = new Button("Delete");
    private final BorderPane borderPane = new BorderPane();
    private RichMessageDisplayer displayer;
    private static final String HEADER_FORMAT = "<h4>%s</h4>\n%s";
    private final StackPane webViewStackPane = new StackPane();

    private Consumer<CorrectionUiPlugin> deleteHandler;
    
    public CorrectionUiPluginWrapper(CorrectionUiPlugin plugin) {
        this.plugin = plugin;
        getStyleClass().add("correction-plugin");
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        FXUtilities.createWebView().then(this::tiesWebView);
        borderPane.setCenter(plugin.getContent());
        borderPane.setBottom(deleteButton);
        deleteButton.setOnAction(this::delete);
        setContent(borderPane);
    }

    public CorrectionUiPluginWrapper deleteUsing(Consumer<CorrectionUiPlugin> deleteHandler) {
        this.deleteHandler = deleteHandler;
        return this;
    }
    
    private void delete(ActionEvent event) {
        if(deleteHandler != null) deleteHandler.accept(plugin);
      
    }

    public CorrectionUiPlugin getPlugin() {
        return plugin;
    }

    private void tiesWebView(WebView webView) {
        webViewStackPane.getChildren().add(webView);
        webViewStackPane.getStyleClass().add("-bg-darker");
        setGraphic(webViewStackPane);
        webView.setOnMouseClicked((event) -> setExpanded(!isExpanded()));
        displayer = new RichMessageDisplayer(webView).addStringProcessor(RichMessageDisplayer.FORMAT_SECTION_TITLES).addStringProcessor(RichMessageDisplayer.COLOR_IMPORTANT_WORDS).addCss("body { overflow-x:hidden;overflow-y:hidden; background:#2c3240; padding:0; }");
        updateHeader();
        displayer.messageProperty().bind(Bindings.createStringBinding(this::updateHeader, plugin.explanationProperty()));
    }

    private String updateHeader() {
        return String.format(HEADER_FORMAT, plugin.getName(), plugin.explanationProperty().getValue());
    }
    
}
