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
/**
 * 
 * @author Tuan anh TRINH
 */

public class DefaultWidgetGroup implements WidgetGroup {
    private ItemWidget[] items;
    private String name;
    private String context;
    private String icon;
    public DefaultWidgetGroup(ItemWidget[] items, String name, String context) {
        this.items = items;
        this.name = name;
        this.context = context;
    }
    public DefaultWidgetGroup(){
        
    }
    @Override
    public ItemWidget[] getItems() {
        return items;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public String getIcon() {
        return icon;
    }
    
}
