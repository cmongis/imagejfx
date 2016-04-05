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
import ijfx.service.preview.PreviewService;
import ijfx.ui.main.LoadingIcon;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import mongis.utils.AsyncCallback;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Tuan anh TRINH
 */
public class PaneLabelCell<T> extends BorderPane implements PaneCell<T>{


    @FXML
    private FontAwesomeIconView titleIconView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;
    
    private final ObjectProperty<T> item = new SimpleObjectProperty<T>();

    // Callback 
    private Callback<T, String> titleFactory = T -> "No title factory";

    private Callback<T, String> subtitleFactory = T -> "No subtitle factory";

    private Callback<T, String> additionalInfoFactory = T -> "you could benefit from **Awesome display**\n";

    private Callback<T, Image> imageFactory;

    private Task currentImageSearch;

    private boolean loadImageOnlyWhenVisible = true;
    
    private boolean isInsideScrollWindow = false;
    
    Image currentImage = null;
    
    
    private final static FXMLLoader LOADER = new FXMLLoader(PaneLabelCell.class.getResource("/ijfx/ui/explorer/LabelIconItem.fxml"));
    
    
    boolean subtitleVisible = true;
    boolean showIcon = true;
    
   
    BooleanProperty showIconProperty;
    BooleanProperty loadImageOnlyWhenVisibleProperty;

    public PaneLabelCell() {
                try {
            //FXUtilities.injectFXML(this, "/ijfx/ui/explorer/ImageIconItem.fxml");
            
            synchronized(LOADER) {
                                
                LOADER.setController(this);
                LOADER.setRoot(this);
                LOADER.load();
            }

            item.addListener(this::onItemChanged);
                   // setImage(previewService.getPreview());
            addEventHandler(ScrollWindowEvent.SCROLL_WINDOW_ENTERED, this::onScrollWindowEntered);
            addEventHandler(ScrollWindowEvent.SCROLL_WINDOW_EXITED,event->isInsideScrollWindow = false);
            
            
          
            
        } catch (IOException ex) {
            Logger.getLogger(PaneLabelCell.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

        public PaneLabelCell<T> setIcon(FontAwesomeIcon icon) {
        titleIconView.setIcon(icon);
        return this;
    }
        public void onScrollWindowEntered(ScrollWindowEvent event) {
        isInsideScrollWindow = true;
        if(currentImage == null && currentImageSearch == null) {
            updateImageAsync(getItem());
        }
    }
    
    /**
     * Set the Model item contained by the PaneLabelCell. The PaneCell will call the different callback to update
     * the view.
     * @param item
     */
    @Override
    public void setItem(T item) {
        this.item.setValue(item);
    }

    /**
     * 
     * @return the item displayed the cell
     */
    @Override
    public T getItem() {
        return this.item.getValue();
    }

    /**
     *
     * @return the Node representing the PaneCell
     */
    @Override
    public Node getContent() {
        return this;
    }

    
    public Property<T> itemProperty() {
        return item;
    }

    /**
     * Set directly the title of the Icon. The title may be overridden later by the title callback
     * @param title
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    public void setAdditionalData(String text) {

    }


    
   
    
    public void onItemChanged(Observable obs, T oldItem, T newItem) {
        
        
        
        // cancelling the possible image search
        if (currentImageSearch != null) {
            currentImageSearch.cancel();
        }
        
        // setting the current image to null
        currentImage = null;
        currentImageSearch = null;

        // avoiding other task to start
        if (newItem == null) {
            return;
        }

        forceUpdate(newItem);

    }
    
     public void forceUpdate(T newItem) {
        //otherwise starting to charge everything
        new AsyncCallback<T, String>()
                .setInput(newItem)
                .run(titleFactory)
                .then(this::setTitle)
                .queue();

        new AsyncCallback<T, String>()
                .setInput(newItem)
                .run(subtitleFactory)
                .then(this::setSubtitle)
                .queue();

        /*
        
         */
        new AsyncCallback<T, String>()
                .setInput(newItem)
                .run(additionalInfoFactory)
                .then(this::setAdditionalData)
                .start();

        if(loadImageOnlyWhenVisible == false || isInsideScrollWindow) updateImageAsync(newItem);
        
        
    }

    private void updateImageAsync(T newItem) {

        if (newItem == null) {
            return;
        }
      
        
        currentImageSearch = new AsyncCallback<T, Image>()
                .setInput(newItem)
                .run(imageFactory)
                .start();
    }

    public PaneLabelCell<T> setAdditionalInfoFactory(Callback<T, String> additionalInfoFactory) {
        this.additionalInfoFactory = additionalInfoFactory;
        return this;
    }

    /**
     * 
     * @param titleFactory Callback that takes a model item as input and return a string representing the title of the icon
     * @return the PaneLabelCell for convenient reasons
     */
    public PaneLabelCell<T> setTitleFactory(Callback<T, String> titleFactory) {
        this.titleFactory = titleFactory;
        return this;
    }

     /**
     * 
     * @param subtitleFactory Callback that takes a model item as input and return a string representing the title of the icon
     * @return the PaneIconcell for convenient reasons
     */
    public PaneLabelCell<T> setSubtitleFactory(Callback<T, String> subtitleFactory) {
        this.subtitleFactory = subtitleFactory;
        return this;
    }

     /**
     * 
     * @param imageFactory Callback that takes a model item as input and return an Image as icon for the model. The callback is always executed in a separated thread in order to
     * avoid blocking the display of the icon.
     * @return the PaneLabelCell for convenient reasons
     */
    public PaneLabelCell<T> setImageFactory(Callback<T, Image> imageFactory) {
        this.imageFactory = imageFactory;
        return this;
    }

    /**
     * When false, the image is loaded whenever the item is updated. When true, the image is loaded
     * only if the Ui element appears on the scroll window.
     * @param loadImageOnlyWhenVisible
     */
    public void setLoadImageOnlyWhenVisible(boolean loadImageOnlyWhenVisible) {
        this.loadImageOnlyWhenVisible = loadImageOnlyWhenVisible;
    }

    
    public BooleanProperty subtibleVisibleProperty() {
       return subtitleLabel.visibleProperty();
    }

    public void setSubtitleVisible(boolean subtitleVisible) {
       subtibleVisibleProperty().setValue(subtitleVisible);
         
       
    }

    public boolean isSubtitleVisible() {
        return subtibleVisibleProperty().getValue();
    }
    
    
}
