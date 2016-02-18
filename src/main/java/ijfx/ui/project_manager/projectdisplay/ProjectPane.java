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
import ijfx.ui.context.animated.AnimationPlus;
import ijfx.ui.context.animated.Animations;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.project_manager.singleimageview.SingleImageViewPane;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
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
    ProjectDisplayService projectDisplayService;

    Logger logger = ImageJFX.getLogger();

    @FXML
    HBox viewHBox;

    @FXML
    HBox planeSetHBox;

    @FXML
    ToggleButton dashboardToggleButton;

    ToggleGroup viewToggleGroup = new ToggleGroup();

    ToggleGroup planeSetToggleGroup = new ToggleGroup();

    HashMap<Class<?>, Toggle> toggleMap = new HashMap<>();

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

            this.project.getHierarchy().addListener((obs, oldValue, newValue) -> {
                updateHierarchy();
            });

            // binding properties
            currentView.addListener(this::onCurrentViewChanged);
            currentItem.addListener(this::onCurrentItemChanged);
            currentPlaneSet.bind(projectDisplay.currentPlaneSetProperty());

            // adding listeners for project display variables
            currentPlaneSet.addListener(this::onCurrentPlaneSetChanged);

            // to create toggle buttons for each plane set created
            projectDisplay.getPlaneSetList().addListener(this::onPlaneSetListChanged);

            // binding toggleGroups
            viewToggleGroup.selectedToggleProperty().addListener(this::onViewToggleSelectionChanged);
            planeSetToggleGroup.selectedToggleProperty().addListener(this::onPlaneSetToggleSelectionChanged);

            // initializing the different dashboard view
            registerPlaneSetView(new DashBoardPlaneSetView(context), dashboardToggleButton);

            viewToggleGroup.selectToggle(dashboardToggleButton);

            // initialising the toggles button for the different types of view
            for (PlaneSetView view : new PlaneSetView[]{
                new IconPlaneSetView(context),
                new TablePlaneSetView(context),
                new SingleImageViewPane(context)
            }) {
                ToggleButton toggleButton = new ToggleButton(null, view.getIcon());
                registerPlaneSetView(view, toggleButton);
                viewHBox.getChildren().add(toggleButton);
            }

            // initializing toggle button for the PlaneSets
            for (PlaneSet added : projectDisplay.getPlaneSetList()) {
                createPlaneSetToggleButton(added);
            }

            currentItem.bind(projectDisplay.getCurrentPlaneSet().currentItemProperty());

            updateHierarchy();

        } catch (IOException ex) {
            Logger.getLogger(ProjectPane.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    private void registerPlaneSetView(PlaneSetView view, ToggleButton button) {
        button.setUserData(view.getClass());
        toggleMap.put(view.getClass(), button);
        viewMap.put(view.getClass(), view);
        viewToggleGroup.getToggles().add(button);
    }

    private Toggle getToggle(Class<?> clazz) {
        return toggleMap.get(clazz);
    }

    private ToggleButton createPlaneSetToggleButton(PlaneSet added) {

        ToggleButton toggleButton = new ToggleButton(added.getName());
        toggleButton.setUserData(added);

        if (added.getName().equals(ProjectDisplay.ALL_IMAGES)) {
            toggleButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.BUILDING));
        } else if (added.getName().equals(ProjectDisplay.SELECTED_IMAGES)) {
            toggleButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.CHECK_SQUARE));
        } else {

            Node node = new FontAwesomeIconView(FontAwesomeIcon.CLOSE);
            toggleButton.setGraphic(node);
            toggleButton.setContentDisplay(ContentDisplay.LEFT);
            node.setOnMouseClicked(event -> {
                projectDisplay.getPlaneSetList().remove(added);
                event.consume();
            });

        }

        planeSetToggleGroup.getToggles().add(toggleButton);
        planeSetHBox.getChildren().add(toggleButton);
        Animations.QUICK_EXPAND.configure(toggleButton, 500).play();
        return toggleButton;

    }

    // when the toggle button representing a view is clicked on
    public void onViewToggleSelectionChanged(Observable obs, Toggle oldValue, Toggle newValue) {
        if (newValue == null) {
            return;
        }

        if (planeSetToggleGroup.getSelectedToggle() == null && planeSetToggleGroup.getToggles().size() > 0) {
            planeSetToggleGroup.selectToggle(planeSetToggleGroup.getToggles().get(0));
        }

        // chanding the current view
        currentView.setValue((Class<? extends PlaneSetView>) newValue.getUserData());

    }

    // when a toggle button representing a PlaneSet is clicked
    public void onPlaneSetToggleSelectionChanged(Observable obs, Toggle oldValue, Toggle newValue) {
        if (newValue == null) {
            return;
        }
        PlaneSet newPlaneSet = (PlaneSet) newValue.getUserData();

        if (newPlaneSet == null) {
            logger.warning("newPlaneSet is null");
            return;
        }
        //currentPlaneSet.setValue(newPlaneSet);
        projectDisplay.setCurrentPlaneSet(newPlaneSet);

        if (currentView.getValue() == DashBoardPlaneSetView.class) {
            viewToggleGroup.selectToggle(toggleMap.get(IconPlaneSetView.class));

        }
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
            return;
        };
        
        
        
        // getting the new for the new value
        PlaneSetView planeSetView = getPlaneSetView(newValue);
        logger.info("Changing PlaneSet view " + planeSetView.getClass().getSimpleName());
        if (planeSetView == null) {
            logger.warning("PlaneSetView not found !");
            return;
        }

        if (oldValue != null) {

            Animation a1 = AnimationPlus.FADE_OUT_LEFT.configure(getPlaneSetView(oldValue).getNode(), null);
            Animation a2 = AnimationPlus.FADE_IN_FROM_LEFT.configure(getPlaneSetView(newValue).getNode(), null);

            a1.setOnFinished(event -> {
                setCenter(planeSetView.getNode());
                updateView(planeSetView, projectDisplay.getCurrentPlaneSet());
                a2.play();
            });
            a1.play();
            //AnimationPl //
        } else {
            setCenter(planeSetView.getNode());

            updateView(planeSetView, projectDisplay.getCurrentPlaneSet());
        }
        
        viewToggleGroup.selectToggle(getToggle(newValue)); //getToggle(oldValue)

    }

    // when the current item changed,
    public void onCurrentItemChanged(Observable obs, TreeItem<PlaneOrMetaData> oldValue, TreeItem<PlaneOrMetaData> newValue) {

        logger.info("Changing current item");
        // the current view is updated
        if (newValue != null) {
            if (getCurrentView() == null) {
                return;
            }
            if(newValue.getValue() != null && newValue.getValue().isPlane()) {
                currentView.setValue(SingleImageViewPane.class);
            }
            
            getCurrentView().setCurrentItem(newValue);
        }
    }

    public void onCurrentPlaneSetChanged(Observable obs, PlaneSet oldValue, PlaneSet newValue) {

        updateView(getCurrentView(), newValue);

    }

    public void updateView(PlaneSetView planeSetView, PlaneSet planeSet) {

        if (planeSetView.getCurrentItem() == null) {
            logger.info("Setting the current item as root");
            planeSet.setCurrentItem(planeSet.getRoot());
        }

        // setting the current plane set
        if (planeSetView.getCurrentPlaneSet() != planeSet) {
            logger.info(String.format("Changing the current PlaneSet from %s to %s of the current view : %s", planeSetView.getCurrentPlaneSet(), planeSet, planeSetView));
            planeSetView.setCurrentPlaneSet(planeSet);
        }

        // setting the view to the current item
        planeSetView.setCurrentItem(planeSet.getCurrentItem());
        logger.info("Setting currentItem to " + planeSet.getCurrentItem());

        // binding the listener to the current item of the new view
        currentItem.bind(planeSet.currentItemProperty());

    }

    // when the list of plane set changed
    public void onPlaneSetListChanged(ListChangeListener.Change<? extends PlaneSet> change) {

        while (change.next()) {

            // adding new toggle buttons to the toggle group and to the hbox
            for (PlaneSet added : change.getAddedSubList()) {

                createPlaneSetToggleButton(added);

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

    private void updateHierarchy() {

        viewMap.values().forEach(view -> {
            view.setHirarchy(project.getHierarchy());
        });

    }

}
