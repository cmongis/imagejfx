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
package ijfx.service.ui.choice;

import com.sun.javafx.tk.Toolkit;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.utils.FontAwesomeIconUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.ALT;
import static javafx.scene.input.KeyCode.CONTROL;
import static javafx.scene.input.KeyCode.META;
import static javafx.scene.input.KeyCode.SHIFT;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongis.utils.BindingsUtils;
import mongis.utils.FXUtilities;

/**
 *
 * @author cyril
 */
public class FXChoiceDialog<T> implements ChoiceDialog<T> {

    Dialog<List<T>> dialog;

    BorderPane borderPane;

    @FXML
    ListView<Choice<T>> listView;

    @FXML
    Label messageLabel;

    BooleanProperty emptyAllowedProperty = new SimpleBooleanProperty(true);

    public FXChoiceDialog() throws IOException {

        dialog = FXUtilities.runAndWait(Dialog<List<T>>::new);

        FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setRoot(borderPane);
        loader.setLocation(getClass().getResource("FXChoiceDialog.fxml"));
        loader.load();

        dialog.getDialogPane().getStylesheets().add(ImageJFX.getStylesheet());
        dialog.getDialogPane().setContent(loader.getRoot());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        FXUtilities.makeListViewMultipleSelection(listView);
        listView.setCellFactory(list -> new ChoiceCell());
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        dialog.setResultConverter(this::convertResult);
    }

    private List<T> convertResult(ButtonType type) {
        if (type == ButtonType.OK) {
            return listView
                    .getSelectionModel()
                    .getSelectedItems()
                    .stream()
                    .map(item -> item.getData())
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<T>();
        }
    }

    private class SelectableChoice<R> implements Choice<R> {

        Choice<R> value;

        BooleanProperty selectedProperty = new SimpleBooleanProperty();

        public SelectableChoice(Choice<R> wrapperValue) {
            this.value = wrapperValue;
        }

        public Choice<R> create(R r) {
            return null;
        }

        @Override
        public String getTitle() {
            return value.getTitle();
        }

        @Override
        public String getDescription() {
            return value.getDescription();
        }

        @Override
        public PixelRaster getPixelRaster() {
            return value.getPixelRaster();
        }

        @Override
        public R getData() {
            return value.getData();
        }

        public Choice<R> getWrappedValue() {
            return value;
        }

        public BooleanProperty selectedProperty() {
            return selectedProperty;
        }

    }

    @Override
    public ChoiceDialog<T> setTitle(String title) {
        dialog.setTitle(title);
        return this;
    }

    @Override
    public ChoiceDialog<T> setMessage(String message) {
        messageLabel.setText(message);
        return this;
    }

    @Override
    public ChoiceDialog<T> addChoice(Choice<T> choice) {
        listView.getItems().add(new SelectableChoice<T>(choice));
        return this;
    }

    @Override
    public ChoiceDialog<T> addChoices(List<? extends Choice<T>> choices) {

        List<SelectableChoice<T>> selection = choices
                .stream()
                .map(choice -> new SelectableChoice<T>(choice))
                .collect(Collectors.toList());

        listView
                .getItems()
                .addAll(selection);

        return this;
    }

    @Override
    public ChoiceDialog<T> setEmptyAllowed(boolean emptyAllowed) {
        emptyAllowedProperty.setValue(emptyAllowed);
        return this;
    }

    @Override
    public List<T> showAndWait() {
        return FXUtilities
                .runAndWait(dialog::showAndWait)
                .orElse(new ArrayList<T>());
    }

    @FXML
    public ChoiceDialog<T> selectAll() {
        listView.getSelectionModel().selectAll();
        return this;
    }

    @FXML
    public void deselectAll() {
        listView.getSelectionModel().clearSelection();
    }

   

    private final static Image NO_IMAGE = FontAwesomeIconUtils.FAItoImage(new FontAwesomeIconView(FontAwesomeIcon.CHECK), 64);

    private class ChoiceCell extends ListCell<Choice<T>> {

        BorderPane borderPane = new BorderPane();
        Label titleLabel = new Label();
        Label descriptionLabel = new Label();
        ImageView imageView = new ImageView();

        BooleanProperty selectedProperty = new SimpleBooleanProperty();

        public ChoiceCell() {

            // Adding main class
            this.getStyleClass().add("choice-cell");

            // title
            titleLabel.getStyleClass().add("title");
            descriptionLabel.getStyleClass().add("description");
            imageView.getStyleClass().add("thumb");

            BindingsUtils.bindNodeToClass(this, descriptionLabel.textProperty().isNull(), "no-description");
            BindingsUtils.bindNodeToClass(this, imageView.imageProperty().isEqualTo(NO_IMAGE), "no-image");

            VBox vBox = new VBox();
            borderPane.setLeft(imageView);
            borderPane.setCenter(vBox);
            vBox.getChildren().addAll(titleLabel, descriptionLabel);

            itemProperty().addListener(this::onItemChanged);

            addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);

        }

        private void onMouseClicked(MouseEvent event) {

        }

        private void onItemChanged(Observable obs, Choice oldValue, Choice choice) {

            if (choice == null) {
                setGraphic(null);
            } else {
                setGraphic(borderPane);
                System.out.println("hello");
                titleLabel.setText(choice.getTitle());
                descriptionLabel.setText(choice.getDescription());

                Image image = PixelRasterUtils.toImage(choice.getPixelRaster());
                imageView.setImage(image == null ? NO_IMAGE : image);
            }

        }

        
    }

}
