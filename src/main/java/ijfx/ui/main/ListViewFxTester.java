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
package ijfx.ui.main;

import ijfx.ui.module.widget.SelectionList;
import ijfx.ui.utils.BaseTester;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.CheckBoxListCell;
import jfxtras.scene.control.ToggleGroupValue;

/**
 *
 * @author cyril
 */
public class ListViewFxTester extends BaseTester {

    SelectionList<String> selectionList;
    
    ObservableList<String> selected = FXCollections.observableArrayList();
    
    ToggleGroupValue<String> group = new ToggleGroupValue<String>();
    
    @Override
    public void initApp() {
        
        
        addAction("Reset", this::reset);
        
        reset();
    }
    
    
    public void reset() {
        selectionList  = new SelectionList<>();
        
        
        
        selectionList.getItems().addAll("Hello","It's me");
        selectionList.getStyleClass().add("selection-list");
        //listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        setContent(selectionList);
        System.out.println("Reset...");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    
    ListCell<String> createCell(ListView<String> listView) {
        System.out.println("creating cell...");
        CheckBoxListCell<String> cell = new CheckBoxListCell<>();
        
        return cell;
    }
    
    
    private class CheckListCellFx<T> extends ListCell<T> {
        
        ToggleButton button = new ToggleButton();
        ObservableList<T> list;
        public CheckListCellFx() {
           
        }
        
        
        ListProperty propety = new SimpleListProperty(list);
        
        
    }
    
    
    
    
   
    
    
}
