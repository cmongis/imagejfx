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

import ijfx.service.ui.hint.Hint;
import java.util.Collection;
import net.imagej.ImageJService;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public interface HintService extends ImageJService{
    
    public void displayHint(Hint hint, boolean force);
    public void displayHints(Collection<? extends Hint> hint, boolean force);
    public void displayHints(String url, boolean force);
    public void displayHints(Class clazz, boolean force);

    public void resetConfiguration();
    public void saveConfiguration();
    
    
}
