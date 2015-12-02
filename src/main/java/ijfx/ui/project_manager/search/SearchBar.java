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
package ijfx.ui.project_manager.search;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.query.QueryService;
import ijfx.core.project.query.Selector;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class SearchBar extends HBox implements SearchHandler{
    
    
    SearchByTagPanel searchByTagPanel;
    
    SearchByMetaDataPanel searchByMetaDataPanel;
    
    @Parameter
    Context context;
    
    @Parameter
    QueryService queryService;
    
    @Parameter
    ProjectManagerService projectService;
    
    public SearchBar(Context context) {
        
        context.inject(this);
        
        searchByTagPanel = new SearchByTagPanel(context);
        searchByTagPanel.setSearchHandler(this);
        
        searchByMetaDataPanel = new SearchByMetaDataPanel(context);
        searchByMetaDataPanel.setSearchHandler(this);
        
        getChildren().addAll(
                new Label("Search by")
                ,new PopoverToggleButton(searchByTagPanel, PopOver.ArrowLocation.TOP_CENTER)
                .setButtonText("Tag")
                .setIcon(FontAwesomeIcon.TAG)
                ,new Label("or")
                ,new PopoverToggleButton(searchByMetaDataPanel, PopOver.ArrowLocation.TOP_CENTER)
                        .setButtonText("Metadata")
                .setIcon(FontAwesomeIcon.KEY)
                
        
        );
        getStyleClass().add("hbox");
        
        
        
    }
    
    public void createButton() {
        
        
    }

    @Override
    public void search(Selector selector) {

        queryService.query(projectService.getCurrentProject(), selector, false);    
    }
}
