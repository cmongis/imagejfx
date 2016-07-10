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
package ijfx.ui.explorer.view;

import mongis.utils.panecell.ScrollBinderChildren;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.service.cluster.ClustererService;
import ijfx.ui.batch.WorkflowStepTitlePaneController;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerIconCell;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.ExplorerView;
import ijfx.ui.explorer.Iconazable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import mongis.utils.FXUtilities;
import mongis.utils.panecell.PaneCell;
import mongis.utils.panecell.PaneCellController;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = ExplorerView.class)
public class GridIconView extends AnchorPane implements ExplorerView {

    @Parameter
    UIService uIService;

    @Parameter
    Context context;

    @Parameter
    ClustererService clustererService;

    @Parameter
    ExplorerService explorerService;

    @FXML
    private VBox vBox;

    @FXML
    private ScrollPane scrollPane;

    private ScrollBinderChildren scrollBinderChildren;

    @FXML
    private GridPane topBar;

    @FXML
    ComboBox rowsComboBox;
    @FXML
    ComboBox columnsComboBox;
    @FXML
    ComboBox groupComboBox;
    private GroupExplorable groupExplorable;
    private List<Label> listLabel;

    private List<ComboBox<String>> comboBoxList;

    @FXML
    private CheckBox clusterCheckbox;
    private PaneCellController<Iconazable> cellPaneCtrl;

    //TODO FXML
    public GridIconView() {
        super();
        comboBoxList = new ArrayList<>();
        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/explorer/view/GridIconView.fxml");
        } catch (IOException ex) {
            Logger.getLogger(GridIconView.class.getName()).log(Level.SEVERE, null, ex);
        }
        cellPaneCtrl = new PaneCellController<>(vBox);
//        topBar = new GridPane();
        comboBoxList.add(rowsComboBox);
        comboBoxList.add(columnsComboBox);
        comboBoxList.add(groupComboBox);

        groupExplorable = new GroupExplorable(clusterCheckbox.selectedProperty());
        clusterCheckbox.selectedProperty().addListener((obs, old, n) -> {
//            System.out.println("ijfx.ui.explorer.view.GridIconView.<init>()");
            sortItems();
        });
//        this.setCenter(scrollPane);
//        setPrefWidth(400);

        cellPaneCtrl.setCellFactory(this::createIcon);
        scrollBinderChildren = new ScrollBinderChildren(scrollPane);

        addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClick);

    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void setItem(List<? extends Explorable> items) {
        List<String> metadataList = explorerService.getMetaDataKey(items);

        comboBoxList.stream().forEach(c -> {
            c.getItems().clear();
            c.getItems().addAll(metadataList);
        });
        initComboBox();
        groupExplorable.setClustererService(clustererService);
        groupExplorable.setListItems(new CopyOnWriteArrayList(items));
        sortItems();
    }

    public void initComboBox() {
        IntStream.range(0, comboBoxList.size())
                .forEach(i -> {
                    comboBoxList.get(i)
                            .getSelectionModel()
                            .selectedItemProperty()
                            .addListener((obs, old, newValue) -> {
                                if (groupExplorable.checkNumber(newValue)) {
                                    groupExplorable.getMetaDataList()
                                            .set(i, newValue);
                                    sortItems();
                                } else {
                                    groupExplorable.getMetaDataList().set(i, old);
                                    uIService.showDialog(newValue + " contains to much different values", DialogPrompt.MessageType.ERROR_MESSAGE);
                                    return;
                                }
                            });
                });
    }

    private void sortItems() {
        groupExplorable.process();
        cellPaneCtrl.update3DList(new CopyOnWriteArrayList<>(groupExplorable.getList3D()), groupExplorable.getSizeList3D(), groupExplorable.getMetaDataList());
    }

    private PaneCell<Iconazable> createIcon() {
        PaneCell<Iconazable> cell = new ExplorerIconCell();
        context.inject(cell);
        return cell;
    }

    @Override
    public List<? extends Explorable> getSelectedItems() {
        return cellPaneCtrl
                .getItems()
                .stream()
                .map(item -> (Explorable) item)
                .filter(item -> item.selectedProperty().getValue())
                .collect(Collectors.toList());
    }

    public void onMouseClick(MouseEvent event) {
        System.out.println(event);
        if (event.getTarget() == vBox) {
            cellPaneCtrl.getItems().forEach(item -> item.selectedProperty().setValue(false));
        }
    }

    public void onMouseDrag(DragEvent event) {
        System.out.println(event);

    }

    @Override
    public Node getIcon() {
        return new FontAwesomeIconView(FontAwesomeIcon.ARCHIVE);
    }

    @Override
    public void setSelectedItem(List<? extends Explorable> items) {

    }

}
