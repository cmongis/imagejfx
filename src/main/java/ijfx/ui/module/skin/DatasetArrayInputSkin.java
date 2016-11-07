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
package ijfx.ui.module.skin;

import ijfx.ui.module.InputSkinPlugin;
import ijfx.ui.module.input.Input;
import ijfx.ui.module.widget.SelectionList;
import java.util.stream.Collectors;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type=InputSkinPlugin.class)
public class DatasetArrayInputSkin extends AbstractInputSkinPlugin<Dataset[]> {

    Property<Dataset[]> datasetArray = new SimpleObjectProperty<>();

    SelectionList<Dataset> listView = new SelectionList();
    
    @Parameter
    ImageDisplayService imageDisplayService;

    public DatasetArrayInputSkin() {

        super();

       // listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

       // listView.getSelectionModel().getSelectedItems().addListener(this::onSelectionChanged);
        
        listView.prefHeight(100);
        listView.getSelectedItems().addListener(this::onSelectionChanged);
       // listView.getStyleClass().add("list-view-fx");
        
    }

    @Override
    public Property valueProperty() {
        return datasetArray;
    }

    @Override
    public Node getNode() {
        return listView;
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return clazz == Dataset[].class;
    }

    @Override
    public void init(Input<Dataset[]> input) {

        listView
                .getItems()
                .addAll(
                        imageDisplayService
                        .getImageDisplays()
                        .stream()
                        .map(display -> imageDisplayService.getActiveDataset(display))
                        .collect(Collectors.toList())
                );

    }

    private void onSelectionChanged(ListChangeListener.Change<? extends Dataset> change) {
        while (change.next()) {
            
        }
        
        Dataset[] array = new Dataset[listView.getSelectedItems().size()];
            datasetArray.setValue(listView.getSelectedItems().toArray(array));
    }

}
