/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.ui.plugin;

import ijfx.ui.UiPlugin;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.ui.IjfxCss;
import ijfx.ui.main.ImageJFX;
import java.util.concurrent.Future;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public abstract class AbstractContextButton implements UiPlugin {

    private Button button;

    FontAwesomeIcon icon;
    
    public AbstractContextButton() {
        button = new Button();
        button.setOnAction(this::onAction);
    }

    public AbstractContextButton(FontAwesomeIcon icon) {
        this(null,icon);
    }
    
    public AbstractContextButton(String text, FontAwesomeIcon icon) {
        
        setIcon(icon);
        
        if(text == null) {
            button = GlyphsDude.createIconButton(icon);
            button.getStylesheets().addAll(IjfxCss.ICON_BUTTON,IjfxCss.ICON_WITH_SPACING);
        }
        else
             button = GlyphsDude.createIconButton(icon, text);
        button.setOnAction(this::onAction);

    }

    abstract public void onAction(ActionEvent event);

    public Node getUiElement() {
        return button;
    }
    
    public Button getButton() {
        return button;
    }
    
    public UiPlugin init() {
        return this;
    }

    public FontAwesomeIcon getIcon() {
        return icon;
    }

    public void setIcon(FontAwesomeIcon icon) {
        this.icon = icon;
    }
    
    protected void showProcessingIcon() {
        getButton().setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.SPINNER));
    }
    
    protected void hideProcessingIcon() {
        getButton().setGraphic(GlyphsDude.createIcon(getIcon()));
    }
    
    protected <T> void submitFuture(final Future<T> future) {
        
        showProcessingIcon();
        Task<T> task = new Task() {
            @Override
            protected T call() throws Exception {
               return future.get();
            }
            
        };
        
        task.setOnSucceeded(event->hideProcessingIcon());
        
        ImageJFX.getThreadPool().submit(task);
        
                
    }
    
    
}
