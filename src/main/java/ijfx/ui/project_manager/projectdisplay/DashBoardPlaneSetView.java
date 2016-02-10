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
package ijfx.ui.project_manager.projectdisplay;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.ui.project_manager.projectdisplay.card.HierarchyProjectCard;
import ijfx.ui.project_manager.projectdisplay.card.ProjectCard;
import ijfx.ui.project_manager.projectdisplay.card.ProjectCardContainer;
import ijfx.ui.project_manager.projectdisplay.card.StatisticCard;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.FlowPane;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class DashBoardPlaneSetView extends FlowPane implements PlaneSetView{

    ArrayList<ProjectCard> projectCardList = new ArrayList<>();
    
    PlaneSet planeSet;
    
    @Parameter
    Context context;
    
    public DashBoardPlaneSetView(Context context) {
        super();
        
       context.inject(this);
        
        projectCardList.add(new ProjectCardContainer(new StatisticCard()));
        projectCardList.add(new ProjectCardContainer(new HierarchyProjectCard(context)));
        getStyleClass().add("project-card-container");
        
    }
    
    @Override
    public void setCurrentItem(TreeItem<PlaneOrMetaData> item) {
        
    }

    @Override
    public TreeItem<PlaneOrMetaData> getCurrentItem() {
        return null;
    }

    @Override
    public void setCurrentPlaneSet(PlaneSet planeSet) {
        this.planeSet = planeSet;
        
        getChildren().clear();
        
        for(ProjectCard card : projectCardList) {
            getChildren().add(card.getContent());
            new Thread(card.update(planeSet.getProjectDisplay().getProject())).start();
        }
        
    }

    @Override
    public PlaneSet getCurrentPlaneSet() {
        return null;
    }

    @Override
    public void setHirarchy(List<String> hierarchy) {
        
        
        
    }

    @Override
    public Node getIcon() {
        return new FontAwesomeIconView(FontAwesomeIcon.DASHBOARD);
    }

    @Override
    public Node getNode() {
        return this;
    }
    
    
    
    
    
}
