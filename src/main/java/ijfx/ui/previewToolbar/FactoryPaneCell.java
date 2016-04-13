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

import ijfx.service.preview.PreviewService;
import ijfx.service.uicontext.UiContextService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import mongis.utils.panecell.PaneIconCell;

/**
 *
 * @author Tuan anh TRINH
 */
public class FactoryPaneCell {

    public static LabelCategory generateLabel(ItemCategory itemCategory, UiContextService contextService) {
        LabelCategory labelCategory = new LabelCategory(itemCategory.getName(), contextService);
        return labelCategory;
    }

    public static PaneIconCell generate(ItemWidget itemWidget, PreviewService previewService) {
        PaneIconCell<ItemWidget> paneIconCell = new PaneIconCell();
        paneIconCell.setImageFactory(i -> i.getImage(previewService,200));
//        Platform.runLater(() -> paneIconCell.setImage(itemWidget.getImage(previewService)));
        paneIconCell.setSubtitleVisible(false);
        paneIconCell.setTitleFactory(f -> f.getLabel());
        paneIconCell.setLoadImageOnlyWhenVisible(false);
        paneIconCell.setItem(itemWidget);

        return paneIconCell;

    }
}
