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
package ijfx.ui.datadisplay.text;

import ijfx.core.Handles;
import ijfx.ui.datadisplay.DisplayPanePlugin;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import mongis.utils.FXUtilities;
import org.scijava.display.TextDisplay;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = DisplayPanePlugin.class)
@Handles(type = TextDisplay.class)
public class TextDisplayView extends BorderPane  implements DisplayPanePlugin<TextDisplay>{

    @FXML
    Text text;

    TextDisplay textDisplay;


    final Logger logger = ImageJFX.getLogger();

    StringProperty titleProperty = new SimpleStringProperty();

    public TextDisplayView() {
    
       
        logger.info("Injecting FXML");
        try {
            // inject TextDisplayView.fxml from the class name
            FXUtilities.injectFXML(this, "/ijfx/ui/text/TextDisplayView.fxml");
            logger.info("FXML injected");
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
        logger.info("Creating text model");
        logger.info("Text created");

    }

    TextDisplayView(TextDisplay textDisplay) {
        this();
       
    }

    @Override
    public void display(TextDisplay display) {
         this.textDisplay = textDisplay;
        text.setText(this.textDisplay.get(0));
    }

    @Override
    public void dispose() {
    }

    @Override
    public StringProperty titleProperty() {
        return titleProperty();
    }

    @Override
    public Pane getPane() {
        return this;
    }
}
