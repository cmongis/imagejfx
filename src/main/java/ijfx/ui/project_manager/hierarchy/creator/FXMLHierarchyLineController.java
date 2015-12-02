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
package ijfx.ui.project_manager.hierarchy.creator;

import ijfx.ui.main.ImageJFX;
import mongis.utils.FXUtilities;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;

/**
 * FXML Controller class
 *
 * @author Cyril Quinton
 */
public class FXMLHierarchyLineController extends BorderPane implements Initializable {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Separator line;
    private final FXMLHierarchyCreatorController controller;
    private int index;
    private boolean lineVisible = true;

    public FXMLHierarchyLineController(FXMLHierarchyCreatorController controller) {
        this.controller = controller;
        FXUtilities.loadView(getClass().getResource("FXMLHierarchyLine.fxml"), this, true);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.setOnDragOver((DragEvent event) -> {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });

        this.setOnDragEntered((DragEvent event) -> {

            line.setVisible(false);
            event.consume();
        });
        this.setOnDragExited((DragEvent event) -> {
            if (lineVisible) {
                line.setVisible(true);
            }
        });
        this.setOnDragDropped((DragEvent event) -> {
            String content = (String) event.getDragboard().getContent(DataFormat.PLAIN_TEXT);

            String[] lines = content.split("\n");
            if (lines.length == 2) {
                String droppedKey = lines[0];
                int droppedIndex = Integer.parseInt(lines[1]);
                if (droppedIndex != index) {
                    boolean removed1 = removeHierarchyKeyInList(droppedKey, droppedIndex);
                    if (!removed1) {
                        ImageJFX.getLogger().info("Something was removed ...");
                    }
                    HierarchyKey newHierarchyKey = new HierarchyKey(controller).setKey(droppedKey).setIndex(droppedIndex > index ? index : index - 1);
                    controller.getHieararchyKeyList().add(newHierarchyKey);
                }

            }
        });

    }

    public FXMLHierarchyLineController setIndex(int index) {
        this.index = index;
        return this;
    }

    public int getIndex() {
        return index;
    }

    private boolean removeHierarchyKeyInList(String objKey, int objIndex) {
        HierarchyKey objectToRemove = null;
        for (HierarchyKey hierarchyKey : controller.getHieararchyKeyList()) {
            String comparedKey = hierarchyKey.getKey();
            if (comparedKey != null && comparedKey.equals(objKey) && hierarchyKey.getIndex() == objIndex) {
                objectToRemove = hierarchyKey;
                break;
            }
        }
        if (objectToRemove != null) {
            controller.getHieararchyKeyList().remove(objectToRemove);
            return true;
        }
        return false;
    }

    public FXMLHierarchyLineController setVisibleLine(boolean visible) {
        line.setVisible(visible);
        lineVisible = visible;
        return this;
    }

}
