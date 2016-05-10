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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.metadata.MetaData;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerIconCell;
import ijfx.ui.explorer.ExplorerView;
import ijfx.ui.explorer.Iconazable;
import ijfx.ui.module.InputEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mongis.utils.panecell.PaneCell;
import mongis.utils.panecell.PaneCellController;
import mongis.utils.panecell.ScrollBinder;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = ExplorerView.class)
public class GridIconView extends BorderPane implements ExplorerView {

    private VBox vBox = new VBox();
    private ScrollPane scrollPane;
    private ScrollBinder binder;
    private HBox hBox;
    private GroupExplorable groupExplorable;
    private ComboBoxMetadata comboBoxMetadata;
    private ComboBoxMetadata comboBoxMetadata2;
    private ComboBoxMetadata comboBoxMetadata3;

    private final PaneCellController<Iconazable> cellPaneCtrl = new PaneCellController<>(vBox);

    public GridIconView() {
        groupExplorable = new GroupExplorable();
        hBox = new HBox();
        comboBoxMetadata = new ComboBoxMetadata();
        comboBoxMetadata2 = new ComboBoxMetadata();
                comboBoxMetadata3 = new ComboBoxMetadata();

        groupExplorable.getSortListExplorable().setFirstMetaData(comboBoxMetadata.getSelectionModel().getSelectedItem());
        groupExplorable.getSortListExplorable().setSecondMetaData(comboBoxMetadata2.getSelectionModel().getSelectedItem());
                groupExplorable.setThirdMetaData(comboBoxMetadata2.getSelectionModel().getSelectedItem());

        initComboBox();
        hBox.getChildren().addAll(comboBoxMetadata, comboBoxMetadata2, comboBoxMetadata3);
        scrollPane = new ScrollPane();
        scrollPane.setContent(vBox);
        this.setTop(hBox);

        this.setCenter(scrollPane);
//setContent(gridPane);
        setPrefWidth(400);
        //setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);


        cellPaneCtrl.setCellFactory(this::createIcon);
        binder = new ScrollBinder(scrollPane);

        addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClick);

    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void setItem(List<? extends Explorable> items) {

        groupExplorable.setListItems(new CopyOnWriteArrayList(items));
    }

    public void initComboBox() {

        comboBoxMetadata.metaDataProperty().addListener((obs, old, newValue) -> {
            groupExplorable.getSortListExplorable().firstMetaData = newValue;
            sortItems();
        });
        comboBoxMetadata2.metaDataProperty().addListener((obs, old, newValue) -> {
            groupExplorable.getSortListExplorable().secondMetaData = newValue;
            sortItems();
        });

    }

    private void sortItems() {
        groupExplorable.process();
        cellPaneCtrl.update3DList(new CopyOnWriteArrayList<>(groupExplorable.getList3D()), groupExplorable.getSizeList3D());
    }

    private PaneCell<Iconazable> createIcon() {
        return new ExplorerIconCell();
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

    public List<Field> getStaticStringFields() {
        Field[] declaredFields = MetaData.class.getDeclaredFields();
        List<Field> fields = new ArrayList<Field>();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                fields.add(field);
            }
        }
        return fields;
    }

}
