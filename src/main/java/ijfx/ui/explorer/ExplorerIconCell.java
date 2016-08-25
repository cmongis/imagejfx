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

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javax.swing.UIManager;
import mongis.utils.panecell.PaneIconCell;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class ExplorerIconCell extends PaneIconCell<Iconazable>{
    
    @Parameter
    ExplorerService explorerService;
    
    public ExplorerIconCell() {
        super();
        showIconProperty().setValue(false);
        setTitleFactory(this::getTitle);
        setSubtitleFactory(this::getSubtitle);
        setImageFactory(this::getImage);
        setLoadImageOnChange(false);
        
    }
    
    @Override
    public void setItem(Iconazable icon) {
        
        
        if(icon == getItem()) return;
            
        
        // we must bind the selected property
        if(getItem() != null) {
            getItem().selectedProperty().unbindBidirectional(selectedProperty());
        }
        
        super.setItem(icon);
        if(icon != null) {
            selectedProperty().bindBidirectional(icon.selectedProperty());
        }
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
        
        //explorerService.toggleSelection((Explorable)getItem());
        //getItem().selectedProperty().setValue(!getItem().selectedProperty().getValue());
    }
    
    @Override
    public void onDoubleClick() {
        explorerService.open(getItem());
    }
    
}
