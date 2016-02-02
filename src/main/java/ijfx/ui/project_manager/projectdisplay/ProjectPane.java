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
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import mongis.utils.FXUtilities;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class ProjectPane extends BorderPane {

    // HashMap containing the different view
    HashMap<Class<? extends PlaneSetView>, PlaneSetView> viewMap = new HashMap<>();

    Property<Class<? extends PlaneSetView>> currentView = new SimpleObjectProperty<>();

    Property<TreeItem<PlaneOrMetaData>> currentItem = new SimpleObjectProperty();

    Property<PlaneSet> currentPlaneSet = new SimpleObjectProperty();

    Project project;

    ProjectDisplay projectDisplay;

    @Parameter
    DefaultProjectDisplayService projectDisplayService;

    Logger logger = ImageJFX.getLogger();

    @FXML
    HBox viewHBox;

    @FXML
    HBox planeSetHBox;

    ToggleGroup viewToggleGroup = new ToggleGroup();

    ToggleGroup planeSetToggleGroup = new ToggleGroup();

    @Parameter
    Context context;

    public ProjectPane(Context context, Project project) {

        try {
            // injecting FXML
            FXUtilities.injectFXML(this);
            // injecting context
            context.inject(this);

            projectDisplay = projectDisplayService.getProjectDisplay(project);

            this.project = project;

            // binding properties
            currentView.addListener(this::onCurrentViewChanged);
            currentItem.addListener(this::onCurrentItemChanged);
            currentPlaneSet.addListener(this::onCurrentPlaneSetChanged);

            // to create toggle buttons for each plane set created
            projectDisplay.getPlaneSetList().addListener(this::onPlaneSetListChanged);

            // binding toggleGroups
            viewToggleGroup.selectedToggleProperty().addListener(this::onViewToggleSelectionChanged);
            planeSetToggleGroup.selectedToggleProperty().addListener(this::onPlaneSetToggleSelectionChanged);

            // initializing the different views
            viewMap.put(TreeTablePlaneSetView.class, new TreeTablePlaneSetView());
            viewMap.put(TablePlaneSetView.class, new TablePlaneSetView());
            viewMap.put(IconPlaneSetView.class, new IconPlaneSetView(context));
            currentView.setValue(TreeTablePlaneSetView.class);

            // initialising the toggles button for the different types of view
            for (PlaneSetView view : viewMap.values()) {
                System.out.println(view.getNode());
                ValueToggleButton<Class<? extends PlaneSetView>> toggleButton = new ValueToggleButton<>(view.getClass(), view.getIcon());

                toggleButton.setSelected(toggleButton.getValue() == currentView.getValue());
                viewToggleGroup.getToggles().add(toggleButton);

                viewHBox.getChildren().add(toggleButton);
            }

            // adding toggle button for the PlaneSets
            for (PlaneSet added : projectDisplay.getPlaneSetList()) {
                ToggleButton toggleButton = new ToggleButton(added.getName());
                toggleButton.setUserData(added);
                
                if(added.getName().equals(ProjectDisplay.ALL_IMAGES)) {
                    toggleButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.BUILDING));
                }
                else if(added.getName().equals(ProjectDisplay.SELECTED_IMAGES)){
                    toggleButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.CHECK_SQUARE));
                }
                else {
                    toggleButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.SEARCH));
                }
                planeSetToggleGroup.getToggles().add(toggleButton);
                planeSetHBox.getChildren().add(toggleButton);

            }

            currentItem.bind(projectDisplay.getCurrentPlaneSet().currentItemProperty());

        } catch (IOException ex) {
            Logger.getLogger(ProjectPane.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    // when the toggle button representing a view is clicked on
    public void onViewToggleSelectionChanged(Observable obs, Toggle oldValue, Toggle newValue) {
        if (newValue == null) {
            return;
        }
        ValueToggleButton<Class<PlaneSetView>> toggleButton = (ValueToggleButton<Class<PlaneSetView>>) newValue;
        // chanding the current view
        currentView.setValue(toggleButton.getValue());

    }

    // when a toggle button representing a PlaneSet is clicked
    public void onPlaneSetToggleSelectionChanged(Observable obs, Toggle oldValue, Toggle newValue) {
        PlaneSet newPlaneSet = (PlaneSet) newValue.getUserData();
        currentPlaneSet.setValue(newPlaneSet);
        projectDisplay.setCurrentPlaneSet(newPlaneSet);
    }

    @FXML
    public void levelUp() {
        TreeItem<PlaneOrMetaData> treeItem = projectDisplay.getCurrentPlaneSet().getCurrentItem();
        if (treeItem.getParent() != null) {
            projectDisplay.getCurrentPlaneSet().setCurrentItem((ProjectTreeItem) treeItem.getParent());
        }
    }

    public PlaneSetView getPlaneSetView(Class<? extends PlaneSetView> clazz) {
        return viewMap.get(clazz);
    }

    // when the view is changed
    public void onCurrentViewChanged(Observable obs, Class<? extends PlaneSetView> oldValue, Class<? extends PlaneSetView> newValue) {

        if (newValue == null) {
            logger.warning("The new view is null");
        };

        // getting the new for the new value
        PlaneSetView planeSetView = getPlaneSetView(newValue);
        logger.info("Changing PlaneSet view " + planeSetView.getClass().getSimpleName());
        if (planeSetView == null) {

            logger.warning("PlaneSetView not found !");
            return;
        }
        System.out.println(projectDisplay.getCurrentPlaneSet().getPlaneList().size());
        setCenter(planeSetView.getNode());

        updateView(planeSetView, projectDisplay.getCurrentPlaneSet());

    }

    // when the current item changed,
    public void onCurrentItemChanged(Observable obs, TreeItem<PlaneOrMetaData> oldValue, TreeItem<PlaneOrMetaData> newValue) {

        logger.info("Changing current item");
        // the current view is updated
        if (newValue != null) {
            getCurrentView().setCurrentItem(newValue);
        }
    }

    public void onCurrentPlaneSetChanged(Observable obs, PlaneSet oldValue, PlaneSet newValue) {

        updateView(getCurrentView(), newValue);

    }

    public void updateView(PlaneSetView planeSetView, PlaneSet planeSet) {

        currentItem.unbind();
        if(planeSetView.getCurrentItem() == null) planeSet.setCurrentItem(planeSet.getRoot());
        
        // setting the view to the current item
        planeSetView.setCurrentItem(planeSet.getCurrentItem());
        
        // binding the listener to the current item of the new view
        currentItem.bind(planeSet.currentItemProperty());
        
        // refreshing the view if the last plane set was different
        if (planeSetView.getCurrentPlaneSet() != planeSet) {
            planeSetView.setCurrentPlaneSet(planeSet);
            planeSetView.setHirarchy(projectDisplay.getProject().getHierarchy());
        }
    }

    // when the list of plane set changed
    public void onPlaneSetListChanged(ListChangeListener.Change<? extends PlaneSet> change) {

        while (change.next()) {

            // adding new toggle buttons to the toggle group and to the hbox
            for (PlaneSet added : change.getAddedSubList()) {
                ToggleButton toggleButton = new ToggleButton(added.getName());
                toggleButton.setUserData(added);
                planeSetToggleGroup.getToggles().add(toggleButton);
                planeSetHBox.getChildren().add(toggleButton);

            }

            // removing the ones removed
            for (PlaneSet removed : change.getRemoved()) {

                ToggleButton toggleButton = (ToggleButton) planeSetHBox.getChildren().stream().filter(node -> node.getUserData() == removed).findFirst().orElse(null);
                planeSetToggleGroup.getToggles().remove(toggleButton);
                planeSetHBox.getChildren().remove(toggleButton);

            }
        }
    }

    private PlaneSetView getCurrentView() {
        return viewMap.get(currentView.getValue());
    }

    private class ValueToggleButton<T> extends ToggleButton {

        private final T view;

        public ValueToggleButton(T view, Node graphics) {

            super(null, graphics);
            this.view = view;
            setUserData(view);

        }

        public T getValue() {
            return view;
        }

        public ValueToggleButton<? extends T> bind(Property<? extends T> property) {
            selectedProperty().bind(Bindings.createBooleanBinding(() -> property.getValue() == view, property));
            return this;
        }

    }

    private class PlaneSetToggleButton extends ToggleButton {

    }

}
