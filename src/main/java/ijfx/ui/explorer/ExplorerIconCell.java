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
package ijfx.ui.explorer;

import javafx.scene.image.Image;
import mongis.utils.panecell.PaneIconCell;

/**
 *
 * @author cyril
 */
public class ExplorerIconCell extends PaneIconCell<Iconazable>{
    
    public ExplorerIconCell() {
        super();
        
        setTitleFactory(this::getTitle);
        setSubtitleFactory(this::getSubtitle);
        setImageFactory(this::getImage);
        
    }
    
    @Override
    public void setItem(Iconazable icon) {
        
        // we must bind the selected property
        if(getItem() != null) {
            getItem().selectedProperty().unbindBidirectional(selectedProperty());
        }
        
        super.setItem(icon);
        icon.selectedProperty().bindBidirectional(selectedProperty());
    }
    
    public String getTitle(Iconazable iconazable) {
        return iconazable.getTitle();
    }
    
    public String getSubtitle(Iconazable iconazable) {
        return iconazable.getSubtitle();
    }
    
    public Image getImage(Iconazable iconazable) {
        return iconazable.getImage();
    }
    
    @Override
    public void onSimpleClick() {
        super.onSimpleClick();
        getItem().selectedProperty().setValue(selectedProperty().getValue());
    }
    
    @Override
    public void onDoubleClick() {
        getItem().open();
    }
    
}
