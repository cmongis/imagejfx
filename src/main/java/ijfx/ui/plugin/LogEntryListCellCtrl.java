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
package ijfx.ui.plugin;

import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import mercury.core.LogEntry;
import mercury.core.LogEntryType;
import org.controlsfx.control.PopOver;
import mongis.utils.FXUtilities;
import mongis.utils.ListCellController;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class LogEntryListCellCtrl extends BorderPane implements ListCellController<LogEntry> {

    @FXML
    Label titleLabel;

    @FXML
    Label descriptionLabel;

    @FXML
    Button infoButton;

    PopOver more;

    public final static String LOG_TYPE_CLASS = "type-log";
    public final static String ERROR_TYPE_CLASS = "type-error";
    public final static String WARNING_TYPE_CLASS = "type-warning";

    LogEntry entry;

    public LogEntryListCellCtrl() {
        try {
            FXUtilities.injectFXML(this, "LogEntryListCell.fxml");
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }

        more = new PopOver();
        more.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);

    }

    public void setItem(LogEntry entry) {
        this.entry = entry;
        titleLabel.setText(entry.getType().toString());

        getStyleClass().removeAll(LOG_TYPE_CLASS, ERROR_TYPE_CLASS, WARNING_TYPE_CLASS);

        if (entry.getType() == LogEntryType.ERROR) {
            getStyleClass().add(ERROR_TYPE_CLASS);
        } else if (entry.getType() == LogEntryType.WARNING) {
            getStyleClass().add(WARNING_TYPE_CLASS);
        } else {
            getStyleClass().add(LOG_TYPE_CLASS);
        }

        titleLabel.setText(entry.getTitle());
        if (entry.getText() != null) {
            descriptionLabel.setText(entry.getText());
        } else {
            descriptionLabel.setText("");
        }

    }

    public LogEntry getItem() {
        return entry;
    }

    @FXML
    public void showMore() {
        more.show(infoButton);
    }

    public void sendEmailToDeveloper() {

    }

}
