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
package ijfx.ui.project_manager.search;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.ui.RichMessageDisplayer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import mongis.utils.TextFileUtils;
import org.controlsfx.control.PopOver;

/**
 *
 * @author cyril
 */
public class SearchSyntaxButton extends ToggleButton {

    WebView webView = new WebView();

    RichMessageDisplayer richMessageDisplayer = new RichMessageDisplayer(webView);

    PopOver popOver = new PopOver();

    VBox vBox = new VBox();

    public static final String DETACH_ME = "Detach me for convenient reading";
    public static final String CLOSE = "Close";

    Button detachButton = new Button("Detach me for convenient reading", new FontAwesomeIconView(FontAwesomeIcon.BOOK));

    StringBinding buttonText = Bindings.createStringBinding(() -> popOver.isDetached() ? CLOSE : DETACH_ME, popOver.detachedProperty());

    public SearchSyntaxButton() {
        setText("Syntax");
        setGraphic(new FontAwesomeIconView(FontAwesomeIcon.INFO_CIRCLE));

        getStyleClass().add("success");
        vBox.getStyleClass().add("vbox");

        
        
        //setting the popover
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.setDetachable(true);

        //setting the webview
        webView.setPrefSize(500, 300);
        richMessageDisplayer
                .addStringProcessor(RichMessageDisplayer.FORMAT_BOLD)
                .addStringProcessor(RichMessageDisplayer.FORMAT_SECTION_TITLES)
                .addStringProcessor(RichMessageDisplayer.COLOR_IMPORTANT_WORDS)
                .addStringProcessor(RichMessageDisplayer.REPLACE_BACKLINE_WITH_BR);

        // detach button
        detachButton.setMaxWidth(Double.POSITIVE_INFINITY);
        detachButton.setOnAction(this::onDetachButtonClicked);
        detachButton.textProperty().bind(buttonText);

        // addint elements to the vbox
        vBox.getChildren().addAll(webView, detachButton);

        popOver.setContentNode(vBox);

        // binding the whole thing
        PopoverToggleButton.bind(this, popOver);

        updateText();
       

    }

    public void onDetachButtonClicked(ActionEvent event) {
        updateText();
        if (popOver.isDetached()) {
            popOver.hide();
        } else {
            popOver.setDetached(true);
        }

    }

    public void updateText() {
         try {
            richMessageDisplayer.setMessage(TextFileUtils.readFileFromJar("/ijfx/core/project/query/syntax.md"));
        } catch (IOException ex) {
            Logger.getLogger(SearchSyntaxButton.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
