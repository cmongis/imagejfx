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
package ijfx.ui.widgets;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.metadata.MetaDataSet;
import ijfx.ui.explorer.AbstractExplorable;
import ijfx.ui.utils.FontAwesomeIconUtils;
import javafx.beans.property.BooleanProperty;
import javafx.scene.image.Image;
import mongis.utils.FailableRunnable;
import net.imagej.Dataset;

/**
 *
 * @author cyril
 */
public class ExplorableButton extends AbstractExplorable{

    
    private String title;
    
    private String subTitle;
    
    private FontAwesomeIcon icon;

    private Image image;
    
    private FailableRunnable action;
    
    public ExplorableButton(String title, String subTitle, FontAwesomeIcon icon) {
        this.title = title;
        this.subTitle = subTitle;
        this.icon = icon;
    }
    
    
    
    public ExplorableButton setAction(FailableRunnable runnable) {
        this.action = runnable;
        return this;
    }
   
    
    
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSubtitle() {
        return subTitle;
    }

    @Override
    public String getInformations() {
        return null;
    }

    @Override
    public Image getImage() {
       if(image == null) {
        return FontAwesomeIconUtils.FAItoImage(new FontAwesomeIconView(icon), 200);
       }
       return image;
    }

    @Override
    public void open() throws Exception {
       if(action == null) return;
       action.run();
    }

    @Override
    public Dataset getDataset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    
    
}
