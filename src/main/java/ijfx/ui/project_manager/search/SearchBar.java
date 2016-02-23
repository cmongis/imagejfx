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
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.query.QueryService;
import ijfx.core.project.query.Selector;
import ijfx.ui.RichMessageDisplayer;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.project_manager.projectdisplay.DefaultPlaneSet;
import ijfx.ui.project_manager.projectdisplay.PlaneSet;
import ijfx.ui.project_manager.projectdisplay.ProjectDisplayService;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.PopupWindow;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class SearchBar extends HBox implements SearchHandler {

    SearchByTagPanel searchByTagPanel;

    SearchByMetaDataPanel searchByMetaDataPanel;

    TextField searchTextField = new TextField();

    Button searchButton = new Button(null, new FontAwesomeIconView(FontAwesomeIcon.SEARCH));

    @Parameter
    Context context;

    @Parameter
    QueryService queryService;

    @Parameter
    ProjectManagerService projectService;

    @Parameter
    ProjectDisplayService projectDisplayService;

    Label statusLabel = new Label();

    ObjectProperty<List<PlaneDB>> lastSearch = new SimpleObjectProperty();

    BooleanProperty isSearching = new SimpleBooleanProperty();

    WebView webView;

    PopOver searchPopOver;

    RichMessageDisplayer richMessageDisplayer;

    public SearchBar(Context context) {

        context.inject(this);

        searchByTagPanel = new SearchByTagPanel(context);
        searchByTagPanel.setSearchHandler(this);

        searchByMetaDataPanel = new SearchByMetaDataPanel(context);
        searchByMetaDataPanel.setSearchHandler(this);

        statusLabel.setVisible(false);

        searchTextField.setPromptText("Advanded search : e.g. file name contains: \"stack\" and z = 0 or tagged with: modified");
        searchTextField.textProperty().addListener(this::onTextFieldTyping);
        searchTextField.setOnAction(this::onSearchButtonClicked);
        searchButton.setOnAction(this::onSearchButtonClicked);
        lastSearch.addListener(this::onLastSearchChanged);

        getChildren().addAll(
                new Label("Search by"), new PopoverToggleButton(searchByTagPanel, PopOver.ArrowLocation.TOP_CENTER)
                .setButtonText("Tag")
                .setIcon(FontAwesomeIcon.TAG), new Label("or"), new PopoverToggleButton(searchByMetaDataPanel, PopOver.ArrowLocation.TOP_CENTER)
                .setButtonText("Metadata")
                .setIcon(FontAwesomeIcon.KEY), new Label("or"), searchTextField, searchButton
        );
        getStyleClass().add("hbox");

        /*
            
            Configure webview
        
         */
        Platform.runLater(() -> {
            webView = new WebView();
            richMessageDisplayer = new RichMessageDisplayer(webView);
            richMessageDisplayer.addStringProcessor(RichMessageDisplayer.BACKLINE_ANDS_AND_ORS);
            richMessageDisplayer.addStringProcessor(RichMessageDisplayer.COLOR_IMPORTANT_WORDS);
            searchPopOver = new PopOver(webView);
            webView.getStyleClass().add("rich-message");
            webView.setPrefWidth(500);
            webView.setPrefHeight(100);

        });

        //webView.setFill(null);
    }

    public void createButton() {

    }

    @Override
    public void search(Selector selector) {

        QueryTask task = new QueryTask(selector);

        disableProperty().bind(task.runningProperty());

        statusLabel.visibleProperty().bind(task.runningProperty());

        task.setOnSucceeded(event -> {
            // Creating a new plane set
            PlaneSet planeSet = new DefaultPlaneSet("Query : " + task.getValue().size(), projectDisplayService.getProjectDisplay(projectService.getCurrentProject()), FXCollections.observableArrayList(task.getValue()));

            // adding the plane set to the current project display
            projectDisplayService.getProjectDisplay(projectService.getCurrentProject()).getPlaneSetList().add(planeSet);
            projectDisplayService.getActiveProjectDisplay().setCurrentPlaneSet(planeSet);
        });

        new Thread(task).start();

    }

    public void onTextFieldTyping(Observable obs, String oldValue, String newValue) {

        if (newValue == null || newValue.equals("")) {
            lastSearch.setValue(null);
            searchPopOver.hide(ImageJFX.getAnimationDuration());

            return;
        }

        Selector selector = queryService.getSelector(newValue);

        //searchPopOver.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_BOTTOM_LEFT);
        searchPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        if (searchPopOver.showingProperty().getValue()== false) {
            searchPopOver.show(searchTextField);
        }
       
        richMessageDisplayer.setMessage("Find all planes where :<br><br>"+selector.phraseMe());
        QueryTask task = new QueryTask(selector);
        task.setOnSucceeded(event -> {

            lastSearch.setValue(task.getValue());
        });

        new Thread(task).start();

    }

    public void onLastSearchChanged(Observable obs, List<PlaneDB> oldValue, List<PlaneDB> newValue) {

        if (newValue == null) {
            searchButton.setText(null);

        } else {
            searchButton.setText("" + newValue.size());

            searchPopOver.show(searchTextField);

        }
    }

    public void onSearchButtonClicked(ActionEvent event) {

        search(queryService.getSelector(searchTextField.getText()));

    }

    public class QueryTask extends Task<List<PlaneDB>> {

        private final Selector selector;

        public QueryTask(Selector selector) {
            this.selector = selector;
        }

        public Selector getSelector() {
            return selector;
        }

        @Override
        protected List<PlaneDB> call() throws Exception {
            return queryService.query(projectService.getCurrentProject(), selector, false);
        }

    }

}
