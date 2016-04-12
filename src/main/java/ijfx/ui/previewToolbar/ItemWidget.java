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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ijfx.service.preview.PreviewService;
import java.util.Map;
import javafx.scene.image.Image;

/**
 *
 * @author Tuan anh TRINH
 */
@JsonDeserialize(as=DefaultWidget.class)
public interface ItemWidget extends Item{
    public String getType();
    public String getAction();
    public String getLabel();
    public void addContext(String s);
    public void removeSpaceContext();
    public Map<String, Object> getParameters();
    public Image getImage(PreviewService previewService);
    
}
