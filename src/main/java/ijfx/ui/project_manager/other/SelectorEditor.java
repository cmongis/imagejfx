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
package ijfx.ui.project_manager.other;

import ijfx.core.project.query.Selector;
import ijfx.core.project.query.DefaultSelector;
import mongis.utils.FXUtilities;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.controlsfx.control.PopOver;
import org.scijava.Context;

/**
 *
 * @author Cyril Quinton
 */
public class SelectorEditor extends RuleEditor {
    private  Selector selector;
    
    PopOver popover = new PopOver();
    
    public SelectorEditor(Context context) {
        super(context);
        selector = null; //new DefaultSelector();
        
        
        codeArea.setTooltip(new Tooltip(FXUtilities.getResourceBundle().getString("queryShortDirection")));

        if(popover == null) popover = new PopOver();
        popover.setContentNode(new Label("Ex : \"Well\""
                + " = \"12\""));
        popover.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        codeArea.focusedProperty().addListener((event,oldValue,newValue)->{
            if(newValue)
            
            popover.show(this);
        });
        
    }

    @Override
    protected void handleCodeChange(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        selector.parse(newValue);
        //highlighter.highLight(codeArea, selector.getWordPositions());
        
    }
    public Selector getSelector() {
        return selector;
    }
    public void setSelector(Selector selector) {
        this.selector = selector;
    }
    
}
