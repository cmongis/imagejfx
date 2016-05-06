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
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerIconCell;
import ijfx.ui.explorer.ExplorerView;
import ijfx.ui.explorer.Iconazable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import mongis.utils.panecell.PaneCell;
import mongis.utils.panecell.PaneCellController;
import mongis.utils.panecell.ScrollBinder;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = ExplorerView.class)
public class GridIconView extends BorderPane implements ExplorerView {

    @Parameter
        private final GridPane gridPane = new GridPane();

    private ScrollBinder binder;
    private HBox hBox;
    private SortExplorable sortExplorable;

    private final PaneCellController<Iconazable> cellPaneCtrl = new PaneCellController<>(gridPane);
        public GridIconView() {
            sortExplorable = new SortExplorable();
            hBox = new HBox();
            hBox.getChildren().add(new Button("r"));
            this.setTop(hBox);
            this.setCenter(gridPane);
//setContent(gridPane);
        setPrefWidth(400);
        //setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        gridPane.prefWidthProperty().bind(widthProperty());
        gridPane.prefHeightProperty().bind(heightProperty());
ColumnConstraints column1 = new ColumnConstraints(100);
    ColumnConstraints column2 = new ColumnConstraints(50, 150, 300);
        //tilePane.setPrefTileHeight(Control.USE_PREF_SIZE);
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        //binder = new ScrollBinder(this);
        cellPaneCtrl.setCellFactory(this::createIcon);
        
        addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClick);
        
    }
        
    @Override
    public Node getNode() {
        return this;
    }

@Override
    public void setItem(List<? extends Explorable> items) {
        sortExplorable.setItems(items);
        //TODO
        cellPaneCtrl.update(new ArrayList<Iconazable>(items));
    }

    private PaneCell<Iconazable> createIcon() {
        return new ExplorerIconCell();
    }

    @Override
    public List<? extends Explorable> getSelectedItems() {
        return cellPaneCtrl
                .getItems()
                .stream()
                .map(item->(Explorable)item)
                .filter(item->item.selectedProperty().getValue())
                .collect(Collectors.toList());
    }

    
    public void onMouseClick(MouseEvent event){
        System.out.println(event);
        if(event.getTarget() == gridPane) {
            cellPaneCtrl.getItems().forEach(item->item.selectedProperty().setValue(false));
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