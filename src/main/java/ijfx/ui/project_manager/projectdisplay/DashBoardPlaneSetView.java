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
import ijfx.ui.project_manager.projectdisplay.card.DummyCard;
import ijfx.ui.project_manager.projectdisplay.card.HierarchyProjectCard;
import ijfx.ui.project_manager.projectdisplay.card.ProjectCard;
import ijfx.ui.project_manager.projectdisplay.card.ProjectCardContainer;
import ijfx.ui.project_manager.projectdisplay.card.RulesCard;
import ijfx.ui.project_manager.projectdisplay.card.StatisticCard;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.TilePane;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class DashBoardPlaneSetView extends TilePane implements PlaneSetView {

    ArrayList<ProjectCard> projectCardList = new ArrayList<>();

    PlaneSet planeSet;

    @Parameter
    Context context;

    ScrollPane scrollPane = new ScrollPane();

    private int column = 2;

    public DashBoardPlaneSetView(Context context) {
        super();

        context.inject(this);

        projectCardList.add(new ProjectCardContainer(new DummyCard()));
        projectCardList.add(new ProjectCardContainer(new StatisticCard()));
        projectCardList.add(new ProjectCardContainer(new HierarchyProjectCard()));
        projectCardList.add(new ProjectCardContainer(new RulesCard()));
        ;

        for (ProjectCard node : projectCardList) {
            context.inject(node);
        }

        getStyleClass().add("project-card-container");

        widthProperty().addListener(this::onWidthModified);

        scrollPane.setContent(this);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        prefWidthProperty().bind(scrollPane.widthProperty());

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

        getChildren().clear();

        for (ProjectCard card : projectCardList) {
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

        return scrollPane;
    }

}
