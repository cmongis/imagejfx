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
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.context.PaneContextualView;
import ijfx.ui.utils.FontAwesomeIconUtils;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

/**
 *
 * @author Tuan anh TRINH
 */
public class LabelCategory extends Label implements ContextualViewWrapper{

    private final PaneContextualView contextualView;
    private final FontAwesomeIconView fontAwesomeIconView;
    private Pane pane;
    private final String context;

    public LabelCategory(String s, String icon, UiContextService contextService, String context) {
        super(s);
        fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.valueOf(icon));
        WritableImage wi = FontAwesomeIconUtils.FAItoImage(fontAwesomeIconView, (int) Double.parseDouble(fontAwesomeIconView.getSize()));
        this.setGraphic(new ImageView(wi));
        pane = new FlowPane();
        contextualView = new PaneContextualView(contextService, pane, this.getText());
        this.context = context;
    }

    @Override
    public PaneContextualView getContextualView() {
        return contextualView;
    }

    public void setPane(Pane p) {
        pane = p;
    }

    public Pane getPane() {
        return pane;
    }
    
    public String getContext()
    {
        return context;
    }

}
