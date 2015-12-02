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
package ijfx.bridge;

import ijfx.ui.main.ImageJFX;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.scijava.Context;

import org.scijava.menu.AbstractMenuCreator;
import org.scijava.menu.ShadowMenu;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;

/**
 * This class generates allows generation of JavaFX Menu by ImageJ
 *
 * @author MONGIS Cyril
 */
public class FxMenuCreator extends AbstractMenuCreator<MenuBar, Menu> {

    
    @Parameter
    ThreadService threadService;
    
    @Parameter
    Context context;
    
    @Override
    protected void addLeafToMenu(ShadowMenu sm, Menu m) {
        MenuItem item = new MenuItem(sm.getName());
        
        item.setOnAction(event->{
            //ImageJFX.getThreadPool().submit(sm);
            
//            context.inject(sm);
            threadService.run(sm);
            
            
        });
        m.getItems().add(item);
    }

    @Override
    protected void addLeafToTop(ShadowMenu sm, MenuBar t) {
        t.getMenus().add(new Menu(sm.getName()));
    }

    @Override
    protected Menu addNonLeafToMenu(ShadowMenu sm, Menu m) {
        Menu newMenu = new Menu(sm.getName());
        m.getItems().add(newMenu);
        return newMenu;

    }

    @Override
    protected Menu addNonLeafToTop(ShadowMenu sm, MenuBar t) {

        Menu newMenu = new Menu(sm.getName());
        t.getMenus().add(newMenu);
        return newMenu;

    }

    @Override
    protected void addSeparatorToMenu(Menu m) {
        m.getItems().add(new SeparatorMenuItem());
    }

    @Override
    protected void addSeparatorToTop(MenuBar t) {
        ImageJFX.getLogger().warning("A seperator should have been inserted but was not.");
    }

}
