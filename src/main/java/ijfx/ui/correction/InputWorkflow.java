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

import ijfx.core.imagedb.ImageLoaderService;
import io.datafx.controller.ViewController;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Tuan anh TRINH
 */
@ViewController(value = "InputWorkflow.fxml")
public class InputWorkflow extends CorrectionFlow {

    @Inject
    WorkflowModel workflowModel;

    @Parameter
    ImageLoaderService imageLoaderService;

    @FXML
    Button folderButton;

    @FXML
    ListView<File> filesListView;

    public InputWorkflow() {
        CorrectionActivity.getStaticContext().inject(this);

    }

    @PostConstruct
    public void init() {
        folderButton.setOnAction(this::onClick);
//        filesListView.setCellFactory(List);
    }

    private void onClick(ActionEvent e) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(null);
        List<File> files = (List<File>) imageLoaderService.getAllImagesFromDirectory(file);
        folderButton.setText("Folder : " + file.getName());
        filesListView.setItems(FXCollections.observableArrayList(files));
    }
}
