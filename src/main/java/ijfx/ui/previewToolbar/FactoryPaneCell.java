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
package ijfx.ui.previewToolbar;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.service.preview.PreviewService;
import mongis.utils.panecell.PaneIconCell;
import mongis.utils.panecell.PaneLabelCell;

/**
 *
 * @author Tuan anh TRINH
 */
public class FactoryPaneCell {

    public static PaneLabelCell generate(ItemCategory itemCategory) {

        PaneLabelCell<ItemCategory> paneLabelCell = new PaneLabelCell();
        paneLabelCell.setIcon(FontAwesomeIcon.valueOf(itemCategory.getIcon()));
        paneLabelCell.setTitleFactory(f -> f.getName());
        paneLabelCell.setLoadImageOnlyWhenVisible(false);
        paneLabelCell.setItem(itemCategory);

        return paneLabelCell;
    }

    public static PaneIconCell generate(ItemWidget itemWidget, PreviewService previewService) {
        PaneIconCell<ItemWidget> paneIconCell = new PaneIconCell();
        try
        {
        FontAwesomeIconView fontAwesomeIconView =new FontAwesomeIconView(FontAwesomeIcon.valueOf(itemWidget.getIcon()));
        fontAwesomeIconView.getStyleClass().add("icon-toolbar");
        
        paneIconCell.setIcon(fontAwesomeIconView);
        paneIconCell.setSubtitleVisible(false);
        }
        catch(Exception e)
        {
        }
     
        paneIconCell.setTitleFactory(f -> f.getLabel());
        paneIconCell.setLoadImageOnlyWhenVisible(false);
        paneIconCell.setItem(itemWidget);

        return paneIconCell;

    }
}
