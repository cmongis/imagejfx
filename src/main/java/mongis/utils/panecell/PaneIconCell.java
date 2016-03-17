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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import mongis.utils.AsyncCallback;
import mongis.utils.FXUtilities;


/**
 *
 * @author cyril
 */
public class PaneIconCell<T> extends BorderPane implements PaneCell<T>{
    
    
    @FXML
    private ImageView imageView;
    
    @FXML
    private FontAwesomeIconView titleIconView;
    
    @FXML
    private Label titleLabel;
    
    @FXML
   private  Label subtitleLabel;
    
    private  final ObjectProperty<T> item = new SimpleObjectProperty<T>();
    
    
    private Callback<T,String> titleFactory = T->"No title factory";
    
    private Callback<T,String> subtitleFactory = T->"No subtitle factory";
    
    private Callback<T,String> additionalInfoFactory =  T-> "you could benefit from **Awesome display**\n";
    
    private Callback<T,Image> imageFactory;
    
    
    
    public PaneIconCell() {
        try {
            FXUtilities.injectFXML(this,"/ijfx/ui/explorer/ImageIconItem.fxml");
            imageView.fitWidthProperty().bind(widthProperty());
            imageView.fitHeightProperty().bind(widthProperty());
            item.addListener(this::onItemChanged);
            
        } catch (IOException ex) {
            Logger.getLogger(PaneIconCell.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public PaneIconCell<T> setIcon(FontAwesomeIcon icon) {
        titleIconView.setIcon(icon);
        return this;
    }
    
    public void setItem(T item) {
        this.item.setValue(item);
      
       
         new AsyncCallback<T,Image>()
                .setInput(item)
                .run(imageFactory)
                .then(this::setImage)
                .start();
        
    }
    
    public T getItem() {
        return this.item.getValue();
    }
    
    public Node getContent() {
        return this;
    }
    
    public Property<T> itemProperty() {
        return item;
    }
    
    public void setTitle(String title) {
        titleLabel.setText(title);
    }
    
    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }
    
    public void setAdditionalData(String text) {
        
    }
    
    public void setImage(Image image) {
        
         
        imageView.setImage(image);
        
      
    }
    
    public void onItemChanged(Observable obs, T oldItem, T newItem) {
        
        new AsyncCallback<T, String>()
                .setInput(newItem)
                .run(titleFactory)
                .then(this::setTitle)
                .queue();
        
        new AsyncCallback<T,String>()
                .setInput(newItem)
                .run(subtitleFactory)
                .then(this::setSubtitle)
                .queue();
        
        /*
        new AsyncCallback<T,Image>()
                .setInput(newItem)
                .run(imageFactory)
                .then(this::setImage)
                .start();
        */
        new AsyncCallback<T,String>()
                .setInput(newItem)
                .run(additionalInfoFactory)
                .then(this::setAdditionalData)
                .start();
    }

    public PaneIconCell<T> setAdditionalInfoFactory(Callback<T, String> additionalInfoFactory) {
        this.additionalInfoFactory = additionalInfoFactory;
        return this;
    }

    public PaneIconCell<T> setTitleFactory(Callback<T, String> titleFactory) {
        this.titleFactory = titleFactory;
        return this;
    }

    public PaneIconCell<T> setSubtitleFactory(Callback<T, String> subtitleFactory) {
        this.subtitleFactory = subtitleFactory;
        return this;
    }

    public PaneIconCell<T> setImageFactory(Callback<T, Image> imageFactory) {
        this.imageFactory = imageFactory;
        return this;
    }
    
    
   
    
    
    
    
    
    
    
}
