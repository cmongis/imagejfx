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
package ijfx.service.ui;

import ijfx.service.ui.choice.ChoiceDialog;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.widgets.FXRichTextDialog;
import java.io.IOException;
import java.util.logging.Level;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class DefaultUIExtraService extends AbstractService implements UIExtraService {

    @Override
    public <T> ChoiceDialog<T> promptChoice(Class<T> clazz) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RichTextDialog createRichTextDialog() {
        try {
            return new FXRichTextDialog();
        } catch (IOException ex) {

            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
