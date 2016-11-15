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

import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import ijfx.service.ui.FxImageService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplayService;
import net.imagej.lut.LUTService;
import net.imglib2.display.ColorTable;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class LUTComboBox extends ComboBox<LUTView> {

    @Parameter
    LUTService lutService;

    @Parameter
    DisplayService displayService;

    Map<String, ColorTable> luts = new HashMap<>();
    ObservableList<LUTView> colorTableList = FXCollections.observableArrayList();

    @Parameter
    DatasetService datasetService;

    @Parameter
    CommandService commandService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    FxImageService fxImageService;

    public LUTComboBox() {
        super();
        //setSkin(createDefaultSkin());
        setItems(colorTableList);
        setCellFactory(callback);
        //setSkin(new LUTComboBoxSkin(this, new LUTComboBoxBehaviour(this, null)));
        //setStyle(getStyle() + "-fx-skin: \"ijfx.ui.plugin.LUTComboBoxSkin\";");
        setButtonCell(new ColorTableCell().setDimension(150, 20));
        setPrefWidth(150);
        setMaxWidth(150);

      

    }

    public Node getThis() {
        return this;
    }

    public void init() {
       

        return;
    }

  

    Callback<ListView<LUTView>, ListCell<LUTView>> callback = new Callback<ListView<LUTView>, ListCell<LUTView>>() {
        @Override
        public ListCell<LUTView> call(ListView<LUTView> param) {
            return new ColorTableCell();
        }
    };


    class ColorTableCell extends ListCell<LUTView> {

        ImageView imageView = new ImageView();
        
        public ColorTableCell() {
            super();
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            itemProperty().addListener(this::onItemChanged);
        }
        
       public ColorTableCell setDimension(double width, double height) {
           imageView.setFitHeight(height);
           imageView.setFitWidth(width);
           return this;
       }
       
        protected void onItemChanged(Observable obs, LUTView odValue, LUTView newValue) {
            if(newValue == null) setGraphic(null);
            else {
                imageView.setImage(newValue.getImageView());
                setGraphic(imageView);
            }         
        }
        
        
        /*
        @Override
        protected void updateItem(LUTView item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setGraphic(null);
            } else {
                try {

                    setGraphic(item.getImageView());
                } catch (Exception e) {
                    setGraphic(null);
                }
            }

        }*/

    }

    public void synchronizedWidthCurrentDisplay() {

    }


    public class LUTComboBoxBehaviour extends ComboBoxBaseBehavior<LUTView> {

        public LUTComboBoxBehaviour(ComboBoxBase<LUTView> comboBox, List<KeyBinding> bindings) {
            super(comboBox, COMBO_BOX_BASE_BINDINGS);
        }

    }

}
