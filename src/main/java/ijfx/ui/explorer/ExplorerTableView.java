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
package ijfx.ui.explorer;

import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.ui.batch.MetaDataSetTableHelper;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.TableView;

/**
 *
 * @author cyril
 */
public class ExplorerTableView implements ExplorerView{

    TableView<MetaDataSet> tableView = new TableView<>();
    
    MetaDataSetTableHelper helper = new MetaDataSetTableHelper(tableView);
    
    public ExplorerTableView() {
        
       helper.setPriority(MetaData.FILE_NAME,MetaData.FILE_SIZE);
        
    }
    
    @Override
    public Node getNode() {
        return tableView;
    }

    @Override
    public void setItem(List<? extends Explorable> items) {
        List<MetaDataSet> mList = items.stream().map(e->e.getMetaDataSet()).collect(Collectors.toList());
        helper.setColumnsFromItems(mList);
        helper.setItem(mList);
    }

    @Override
    public List<? extends Explorable> getSelectedItems() {
        return null;
    }
    
}
