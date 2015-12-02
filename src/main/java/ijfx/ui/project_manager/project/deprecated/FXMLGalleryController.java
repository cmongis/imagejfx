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
package ijfx.ui.project_manager.project.deprecated;

import ijfx.core.metadata.MetaData;

import ijfx.core.project.imageDBService.PlaneDB;
import mongis.utils.FXUtilities;
import ijfx.core.listenableSystem.Listening;
import ijfx.core.project.Project;
import ijfx.service.thumb.ThumbService;
import ijfx.ui.project_manager.ImageDisplayerJavaFXService;

import ijfx.ui.project_manager.singleimageview.ImageLoadedController;
import ijfx.ui.project_manager.project.DefaultProjectViewModelService;
import ijfx.ui.project_manager.project.ProjectViewModel;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril Quinton
 */
@Deprecated
public class FXMLGalleryController extends BorderPane implements Initializable, Listening {

    public static int cellsize = 200;
    @FXML
    private GridPane gridPane;
    @FXML
    private Button upFolderButton;
    @FXML
    private Button leftButton;
    @FXML
    private Button rightButton;
    
   
    @Parameter
    Context context;
    
    @Parameter
    private ImageDisplayerJavaFXService displayerService;
    
    @Parameter
    DefaultProjectViewModelService projectViewModelManager;
    
    @Parameter
    private ThumbService thumbService;
    
    ProjectViewModel projectSpecificViewModel;
    
    private final ListChangeListener<CheckBoxTreeItem> itemContentListener;
    private final ChangeListener<CheckBoxTreeItem> currentItemListener;
    private ObservableList content;

    private final Project project;
    private final List<Pane> controllerList;
    private final List<Pane> displayedControllerList;
    private final IntegerProperty nbItem;
    private final IntegerProperty nbDisplayedItem;
    private final IntegerProperty nbSlide;
    private final IntegerProperty currentSlide;
    private final BooleanProperty leftSlideAvailable;
    private final BooleanProperty rightSlideAvailable;
    private int nbCol;
    private int nbRow;

    
  

    public FXMLGalleryController(Project project, Context context) {
        this.project = project;
       context.inject(this);
        controllerList = new ArrayList<>();
        displayedControllerList = new ArrayList<>();
        projectSpecificViewModel = projectViewModelManager
                .getViewModel(project);
       
        itemContentListener = new ListChangeListener<CheckBoxTreeItem>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends CheckBoxTreeItem> c) {
                handleCurrentItemContentChange(c);
            }
        };

        currentItemListener = (ObservableValue<? extends CheckBoxTreeItem> observable, CheckBoxTreeItem oldValue, CheckBoxTreeItem newValue) -> {
            handleCurrentItemChange(oldValue, newValue);
        };
        projectSpecificViewModel.nodeProperty().addListener(currentItemListener);
        content = projectSpecificViewModel.nodeProperty().get().getChildren();
        nbItem = new SimpleIntegerProperty();
        nbItem.addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                updateNbSlide();
            }
        });
        nbSlide = new SimpleIntegerProperty();
        nbSlide.addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                updateArrowProperties();
            }
        });
        leftSlideAvailable = new SimpleBooleanProperty();
        rightSlideAvailable = new SimpleBooleanProperty();
        nbDisplayedItem = new SimpleIntegerProperty(0);
        nbDisplayedItem.addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable observable) {
               updateNbSlide();
               updateDisplayedControllerList();
            }
        });

        currentSlide = new SimpleIntegerProperty(0);
        currentSlide.addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable observable) {
                stopLoadingImages();
                updateDisplayedControllerList();
            }
        });
        FXUtilities.loadView(getClass().getResource("FXMLGallery.fxml"),
                this, true);

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (ReadOnlyDoubleProperty sizeProp : Arrays.asList(gridPane.heightProperty(), gridPane.widthProperty())) {
            sizeProp.addListener(new ChangeListener<Number>() {

                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    calculateNBDisplayItem();
                }
            });
        }
        leftButton.visibleProperty().bind(leftSlideAvailable);
        rightButton.visibleProperty().bind(rightSlideAvailable);
        leftButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                slide(-1);
            }
        });
        rightButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                slide(1);
            }
        });
        upFolderButton.setTooltip(new Tooltip(resources.getString("goToUpFolder")));

        calculateNBDisplayItem();
        initControllerList();
    }

    private void initControllerList() {
        List<Pane> lsPane = createControllers(content);
        controllerList.clear();
        controllerList.addAll(lsPane);
        currentSlide.set(0);
        calculateNBDisplayItem();
        updateDisplayedControllerList();

    }

    private void updateDisplayedControllerList() {
        calculateNBDisplayItem();
        nbItem.set(content.size());
        updateNbSlide();
        updateArrowProperties();
        displayedControllerList.clear();
        int startIndex = nbDisplayedItem.get() * currentSlide.get();
        int endIndex = nbDisplayedItem.get() * (currentSlide.get() + 1);
        if (endIndex >= nbItem.get()) {
            endIndex = nbItem.get();
        }
        try {
        displayedControllerList.addAll(controllerList.subList(startIndex, endIndex));
        } catch (IndexOutOfBoundsException ex) {
            initControllerList();
        }
        displayImages();
    }

    public void createView() {
        FXUtilities.modifyUiThreadSafe(new Runnable() {

            @Override
            public void run() {
                gridPane.getChildren().clear();
                Iterator<Pane> it = displayedControllerList.iterator();
                for (int r = 0; r < nbRow; r++) {
                    for (int c = 0; c < nbCol; c++) {
                        if (it.hasNext()) {
                            gridPane.add(it.next(), c, r);
                        }
                    }
                }
            }
        });
        

        

    }

    //launch a new thread to load images within the displayed controller list
    private void displayImages() {
        final List<ImageLoadedController> ctrList = new ArrayList<>();
        for (Pane controller : displayedControllerList) {
            if (controller instanceof ImageLoadedController) {
                ImageLoadedController imageGallery = (ImageLoadedController) controller;
                if (!imageGallery.loadedProperty().get() && !imageGallery.loadingProperty().get()) {
                    ctrList.add(imageGallery);
                }
            }
        }
        if (!ctrList.isEmpty()) {
            displayerService = new ImageDisplayerJavaFXService(project, ctrList, context);

            displayerService.start();
        }
                createView();


    }

    @FXML
    private void moveToUpNodeAction() {
        projectSpecificViewModel.setCurrent((CheckBoxTreeItem) projectSpecificViewModel.nodeProperty().get().getParent());
    }

    @Override
    public void stopListening() {
        projectSpecificViewModel.nodeProperty().removeListener(currentItemListener);
        stopLoadingImages();
        stopListeningToContent();
    }

    private void stopListeningToContent() {
        content.removeListener(itemContentListener);
    }

    private void listenToContent() {
        content.addListener(itemContentListener);
    }

    private void handleCurrentItemContentChange(ListChangeListener.Change<? extends CheckBoxTreeItem> c) {
        while (c.next()) {
            if (c.wasRemoved()) {
                int index = c.getFrom();
                for (int i = 0; i < c.getRemovedSize(); i++) {
                    controllerList.remove(index);
                }
            }
            if (c.wasAdded()) {
                controllerList.addAll(c.getFrom(), createControllers(c.getAddedSubList()));
            }
        }
        updateDisplayedControllerList();
    }

    private void handleCurrentItemChange(CheckBoxTreeItem oldVal, CheckBoxTreeItem newVal) {

        stopListeningToContent();
        content = newVal.getChildren();
        TreeItem parentItem = newVal.getParent();
        upFolderButton.setVisible(parentItem != null && parentItem.getValue() != null);
        listenToContent();
        initControllerList();
    }

    private List<Pane> createControllers(List<? extends CheckBoxTreeItem> items) {
        List<Pane> lsPane = new ArrayList<>();
        for (Object child : content) {
            if (child instanceof CheckBoxTreeItem) {
                CheckBoxTreeItem childItem = (CheckBoxTreeItem) child;
                if (childItem.getValue() instanceof MetaData) {
                    MetaData metadata = (MetaData) childItem.getValue();
                    lsPane.add(new FXMLMetadataIFolderController(project, metadata, context, childItem));
                } else if (childItem.getValue() instanceof PlaneDB) {
                    CheckBoxTreeItem<PlaneDB> planeItem = (CheckBoxTreeItem<PlaneDB>) childItem;
                    int index = projectSpecificViewModel.planeListProperty().indexOf(planeItem);
                    lsPane.add(new FXMLImageInGalleryController(planeItem, project, index, context));
                }
            }

        }
        return lsPane;
    }

    private FXMLImageInGalleryController createImageController(CheckBoxTreeItem<PlaneDB> item, int index) {
        return (new FXMLImageInGalleryController(item, project, index, context));
    }

    private void calculateNBDisplayItem() {
        int nbDisItem = (int) (gridPane.getWidth() * gridPane.getHeight()) / (cellsize * cellsize);
        nbRow = (int) gridPane.getHeight() / cellsize;
        try {
            nbCol = nbDisItem / nbRow;
        } catch (ArithmeticException ex) {
            nbCol = 1;
        }
        nbDisplayedItem.set(nbDisItem);

    }

    private void updateNbSlide() {
        if (nbDisplayedItem.get() <= 1) {
            nbSlide.set(0);
        } else {
            nbSlide.set(nbItem.get() / nbDisplayedItem.get() - 1);
        }
    }

    private void updateArrowProperties() {
        leftSlideAvailable.set(currentSlide.get() > 0);
        rightSlideAvailable.set(currentSlide.get() < nbSlide.get());

    }

    private void slide(int shift) {
        currentSlide.set(currentSlide.get() + shift);
        updateDisplayedControllerList();
    }

    private void stopLoadingImages() {
        if (displayerService != null && displayerService.isRunning()) {
            displayerService.cancel();
        }
    }

}
