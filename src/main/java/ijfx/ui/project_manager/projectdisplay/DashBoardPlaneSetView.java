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
import ijfx.core.project.Project;
import ijfx.ui.context.animated.Animations;
import ijfx.ui.project_manager.projectdisplay.card.ProjectCard;
import ijfx.ui.project_manager.projectdisplay.card.ProjectCardContainer;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.Animation;
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.TilePane;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;

/**
 *
 * @author cyril
 */
public class DashBoardPlaneSetView extends TilePane implements PlaneSetView {

    ArrayList<ProjectCard> projectCardList = new ArrayList<>();

    PlaneSet planeSet;

    @Parameter
    Context context;

    @Parameter
    PluginService pluginService;

    ScrollPane scrollPane = new ScrollPane();

    private int column = 2;

    Project project;

    public DashBoardPlaneSetView(Context context) {
        super();

        context.inject(this);

        /*
        projectCardList.add(new ProjectCardContainer(new DummyCard()));
        projectCardList.add(new ProjectCardContainer(new StatisticCard()));
        projectCardList.add(new ProjectCardContainer(new HierarchyProjectCard()));
        projectCardList.add(new ProjectCardContainer(new RulesCard()));
        ;*/
        for (ProjectCard card : pluginService.createInstancesOfType(ProjectCard.class)) {

            projectCardList.add(new ProjectCardContainer(card));

        }

        /*
        for (ProjectCard node : projectCardList) {
            context.inject(node.getContent());
        }*/
        getStyleClass().add("project-card-container");

        widthProperty().addListener(this::onWidthModified);

        scrollPane.setContent(this);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        prefWidthProperty().bind(scrollPane.widthProperty());
        
        

    }

    public void bindProject(Project project) {

        if (this.project == null) {
            
            project.hasChangedProperty().addListener(this::onProjectChanged);
            this.project = project;
            
        }

    }

    public void onProjectChanged(Observable obs, Boolean oldValue, Boolean newValue) {
       updateCards();
    }

    public void onWidthModified(Observable obs, Number oldValue, Number newValue) {

        column = newValue.intValue() / 380;
        System.out.println(column);
        System.out.println(projectCardList.size());
        if (column >= projectCardList.size()) {
            column = projectCardList.size();
        }

        double tileWidth = (newValue.doubleValue() / column) - (hgapProperty().getValue() * (column - 1)) - 15 / column;
        System.out.println(String.format("Columns : %d, width : %.0f", column, tileWidth));
        System.out.println(tileWidth);
        setPrefColumns(column);
        setPrefTileWidth(tileWidth);

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
        bindProject(planeSet.getProjectDisplay().getProject());
        updateCards();
    }

    
    
    
    private void updateCards() {

        for (ProjectCard card : projectCardList) {
            if (getChildren().contains(card.getContent()) == false && card.dismissed().getValue() == false) {
                getChildren().add(card.getContent());
            }
            new Thread(card.update(project)).start();
            
            card.dismissed().addListener((obs,oldValue,newValue)->{
                
                if(newValue) {
                    Animation t = Animations.ZOOMOUT.configure(card.getContent(), 300);
                    t.setOnFinished(event->{
                        getChildren().remove(card.getContent());
                    });
                    t.play();
                }
                else {
                    getChildren().add(card.getContent());
                    Animations.ZOOMIN.configure(card.getContent(), 300).play();
                }
            
            });
            
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

        return scrollPane;
    }

}
