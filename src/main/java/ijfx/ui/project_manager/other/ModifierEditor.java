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

import ijfx.core.project.query.Modifier;
import ijfx.core.project.query.ModifierFactory;
import ijfx.core.project.query.DefaultModifier;
import mongis.utils.FXUtilities;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tooltip;
import org.scijava.Context;

/**
 *
 * @author Cyril Quinton
 */
public class ModifierEditor extends RuleEditor {

    private Modifier modifier;

    public ModifierEditor(Context context) {
        super(context);
        modifier = new DefaultModifier();
        codeArea.setTooltip(new Tooltip(FXUtilities.getResourceBundle().getString("modifyShortDirection")));

    }

    @Override
    protected void handleCodeChange(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        modifier = null; //ModifierFactory.create(newValue);
        modifier.parse(newValue);
        highlighter.highLight(codeArea, modifier.getWordPositions());
    }

    public Modifier getModifier() {
        
        return modifier;
    }
    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

}
