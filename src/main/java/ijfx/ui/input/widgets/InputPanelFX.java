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
package ijfx.ui.input.widgets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.imagej.widget.HistogramWidget;
import org.scijava.widget.ButtonWidget;
import org.scijava.widget.InputPanel;
import org.scijava.widget.InputWidget;

/**
 *
 * @author cyril
 */
public class InputPanelFX implements InputPanel<Node, Node> {

    @FXML
    GridPane gridPane;

    @FXML
    HBox buttonHBox;

    @FXML
    VBox graphicsVBox;

    Parent root;

    Map<String, InputWidget<?, Node>> widgetList = new HashMap<>();

    // number of fields added to the pane
    protected int fieldCount = -1;

    // not sure if it's still used...
    protected final int fieldColumn = 1;

    // same...
    protected int labelColumn = 0;

    public InputPanelFX() {

        try {
            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(this.getClass().getResource("InputPanelFX.fxml"));
            loader.setController(this);
            loader.load();

            root = loader.getRoot();
        } catch (IOException ex) {
            Logger.getLogger(InputPanelFX.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public boolean supports(InputWidget<?, ?> widget) {
        return widget.getComponentType().isAssignableFrom(Node.class);
    }

    public void addWidgetInGridPane(InputWidget<?, Node> widget) {

        fieldCount++;
        Node component = widget.getComponent();
        Label label = new Label();
        label.getStyleClass().add("input-label");
        label.setText(widget.get().getWidgetLabel());
        gridPane.add(label, labelColumn, fieldCount);
        gridPane.add(component, fieldColumn, fieldCount);

    }

    @Override
    public void addWidget(InputWidget<?, Node> widget) {
        String name = widget.get().getItem().getName();
        
        widgetList.put(name, widget);

        if (widget instanceof ButtonWidget) {
            buttonHBox.getChildren().add(widget.getComponent());
        }
        else if( widget instanceof HistogramWidget) {
            graphicsVBox.getChildren().add(((HistogramWidget<Node>) widget).getComponent());
        }
        else {
            addWidgetInGridPane(widget);
        }
    }

    @Override
    public Object getValue(String name) {
        return widgetList.get(name).getValue();
    }

    @Override
    public int getWidgetCount() {
        return widgetList.size();
    }

    @Override
    public boolean hasWidgets() {
        return widgetList.size() > 0;
    }

    @Override
    public boolean isMessageOnly() {
        return false;
    }

    @Override
    public void refresh() {
        widgetList.values().forEach(this::refresh);
    }

    public void refresh(InputWidget<?, Node> widget) {
        Platform.runLater(() -> widget.refreshWidget());
    }

    @Override
    public Class<Node> getWidgetComponentType() {
        return Node.class;
    }

    @Override
    public InputWidget<?, Node> getWidget(String name) {

        return widgetList.get(name);

    }

    @Override
    public Parent getComponent() {
        return root;
    }

    @Override
    public Class<Node> getComponentType() {
        return Node.class;
    }

}
