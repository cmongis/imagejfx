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
import ijfx.service.uicontext.UiContextService;
import javafx.application.Platform;
import mongis.utils.panecell.PaneIconCell;
import mongis.utils.panecell.PaneIconCellPreview;

/**
 *
 * @author Tuan anh TRINH
 */
public class DefaultFactoryPaneCell implements FactoryPaneCell {

    public LabelCategory generateLabel(ItemCategory itemCategory, UiContextService contextService) {
        LabelCategory labelCategory = new LabelCategory(itemCategory.getName(),itemCategory.getIcon(), contextService, itemCategory.getContext());
        return labelCategory;
    }

    public PaneIconCellPreview generate(ItemWidget itemWidget, PreviewService previewService) {
       PaneIconCellPreview<ItemWidget> paneIconCellPreview = new PaneIconCellPreview();
        paneIconCellPreview.setImageFactory(i -> i.getImage(previewService,120));
        paneIconCellPreview.setSubtitleVisible(false);
        paneIconCellPreview.setTitleFactory(f -> f.getLabel());
        paneIconCellPreview.setLoadImageOnChange(false);
        paneIconCellPreview.setItem(itemWidget);

        return paneIconCellPreview;

    }
}
