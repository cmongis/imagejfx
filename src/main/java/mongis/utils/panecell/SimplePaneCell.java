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
package mongis.utils.panecell;

import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import mongis.utils.CallbackTask;
import mongis.utils.FailableCallback;
import mongis.utils.panecell.PaneCell;

/**
 *
 * @author cyril
 */
public class SimplePaneCell<T> implements PaneCell<T> {
    
    BorderPane borderPane = new BorderPane();
    Label title = new Label();
    ImageView imageView = new ImageView();
    Callback<T, String> titleFactory = (s) -> "No name";
    FailableCallback<T, Image> imageFactory = (i) -> null;
    T item;
    BooleanProperty booleanProperty = new SimpleBooleanProperty();
    BooleanProperty onScreenProperty = new SimpleBooleanProperty();
    Consumer<T> onClick;

    public SimplePaneCell() {
        borderPane.setCenter(imageView);
        borderPane.setBottom(title);
        borderPane.getStyleClass().add("simple-pane-cell");
        borderPane.setOnMouseClicked(this::onMouseClicked);
        imageView.setOnMouseClicked(this::onMouseClicked);
    }

    @Override
    public void setItem(T item) {
        title.setText(titleFactory.call(item));
        this.item = item;
        new CallbackTask<T, Image>().setInput(item).run(imageFactory).then(imageView::setImage).start();
    }

    @Override
    public T getItem() {
        return item;
    }

    @Override
    public Node getContent() {
        return borderPane;
    }

    @Override
    public BooleanProperty selectedProperty() {
        return selectedProperty();
    }

    private void onMouseClicked(MouseEvent event) {
        event.consume();
        onClick.accept(item);
    }

    public SimplePaneCell<T> setTitleFactory(Callback<T, String> factory) {
        this.titleFactory = factory;
        return this;
    }

    public SimplePaneCell<T> setImageFactory(FailableCallback<T, Image> factory) {
        this.imageFactory = factory;
        return this;
    }

    public SimplePaneCell<T> setOnMouseClicked(Consumer<T> onClick) {
        this.onClick = onClick;
        return this;
    }
    public SimplePaneCell<T> setWidth(double width) {
        imageView.setFitWidth(width);
        imageView.setPreserveRatio(true);
        return this;
    }
    
    public BooleanProperty onScreenProperty() {
        return onScreenProperty;
    }
    
}
